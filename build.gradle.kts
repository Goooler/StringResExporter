import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.shadow)
  alias(libs.plugins.spotless)
}

version = "0.3.0-SNAPSHOT"
val baseName = "string-res-exporter"

buildConfig {
  buildConfigField("VERSION_NAME", version.toString())
  packageName = "io.github.goooler.exporter"
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.fromTarget(libs.versions.jdkRelease.get())
    freeCompilerArgs.add("-Xjdk-release=${libs.versions.jdkRelease.get()}")
  }
}

spotless {
  kotlin {
    ktlint(libs.ktlint.get().version)
    target("**/src/**/*.kt")
  }
  kotlinGradle {
    ktlint(libs.ktlint.get().version)
  }
}

dependencies {
  implementation(libs.poi)
  implementation(libs.jdom2)
  implementation(libs.clikt)

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.systemExit)
  testImplementation(libs.assertk)
  testRuntimeOnly(libs.junit.platform.launcher)

  shadowR8(libs.r8)
}

tasks.withType<JavaCompile>().configureEach {
  options.release = libs.versions.jdkRelease.get().toInt()
}

tasks.jar {
  // We don't need the standard jar.
  enabled = false
}

tasks.shadowJar {
  archiveBaseName = baseName
  archiveVersion = version.toString()
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
  failOnDuplicateEntries = true
  mergeServiceFiles()

  manifest {
    attributes["Main-Class"] = "io.github.goooler.exporter.MainKt"
  }

  exclude(
    "**/*.kotlin_metadata",
    "**/*.kotlin_builtins",
    "**/*.kotlin_module",
    "**/module-info.class",
    "assets/**",
    "font_metrics.properties",
    "META-INF/AL2.0",
    "META-INF/DEPENDENCIES",
    "META-INF/jdom-info.xml",
    "META-INF/LGPL2.1",
    "META-INF/maven/**",
    "META-INF/native-image/**",
    "META-INF/*.version",
    "**/*.proto",
    "**/*.dex",
    "**/LICENSE**",
    "**/NOTICE**",
    "r8-version.properties",
    "migrateToAndroidx/*",
  )

  minimize {
    r8 {
      enableOptimization()
      keepRuleFiles.from(file("src/main/rules.pro"))
    }
  }
}

val binaryJar = tasks.register("binaryJar") {
  group = LifecycleBasePlugin.BUILD_GROUP
  description = "Builds an executable binary JAR."

  inputs.file(tasks.shadowJar.flatMap { it.archiveFile })
  outputs.file(layout.buildDirectory.file("libs/$baseName-$version-binary.jar"))

  doFirst {
    with(outputs.files.singleFile) {
      writeText($$"#!/bin/sh\n\nexec java $JAVA_OPTS -jar $0 \"$@\"\n\n")
      appendBytes(inputs.files.singleFile.readBytes())
      setExecutable(true, false)
    }
  }
}

tasks.test {
  dependsOn(binaryJar)

  systemProperty("CLI_PATH", binaryJar.map { it.outputs.files.singleFile.path })
  // https://github.com/tginsberg/junit5-system-exit/issues/10
  systemProperty("java.security.manager", "allow")

  useJUnitPlatform()
}
