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

val r8 by configurations.registering

dependencies {
  implementation(libs.poi)
  implementation(libs.jdom2)
  implementation(libs.clikt)

  r8(libs.r8)

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.systemExit)
  testImplementation(libs.assertk)
  testRuntimeOnly(libs.junit.platform.launcher)
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
}

val r8Jar by tasks.registering(JavaExec::class) {
  group = LifecycleBasePlugin.BUILD_GROUP

  val rulesFile = file("src/main/rules.pro")
  val r8File = file("build/libs/$baseName-$version-r8.jar")
  val binaryFile = file("build/libs/$baseName-$version-binary.jar")

  inputs.files(tasks.shadowJar, rulesFile)
  outputs.file(binaryFile)

  classpath(r8)
  mainClass = "com.android.tools.r8.R8"
  args(
    "--release",
    "--classfile",
    "--output", r8File.path,
    "--pg-conf", rulesFile.path,
    "--lib", providers.systemProperty("java.home").get(),
    tasks.shadowJar.get().archiveFile.get().asFile.path,
  )

  doLast("binaryJar") {
    with(binaryFile) {
      writeText($$"#!/bin/sh\n\nexec java $JAVA_OPTS -jar $0 \"$@\"\n\n")
      appendBytes(r8File.readBytes())
      setExecutable(true, false)
    }
  }
}

tasks.test {
  dependsOn(r8Jar)

  systemProperty("CLI_PATH", r8Jar.get().outputs.files.singleFile.path)
  // https://github.com/tginsberg/junit5-system-exit/issues/10
  systemProperty("java.security.manager", "allow")

  useJUnitPlatform()
}
