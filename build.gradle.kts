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
  compilerOptions.jvmTarget = JvmTarget.fromTarget(libs.versions.jdkRelease.get())
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

val r8: Configuration by configurations.creating

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

val r8File = layout.buildDirectory.file("libs/$baseName-$version-r8.jar").map { it.asFile }
val rulesFile = project.file("src/main/rules.pro")
val r8Jar by tasks.registering(JavaExec::class) {
  dependsOn(tasks.shadowJar)

  val fatJarFile = tasks.shadowJar.get().archiveFile
  inputs.file(fatJarFile)
  inputs.file(rulesFile)
  outputs.file(r8File)

  classpath(r8)
  mainClass = "com.android.tools.r8.R8"
  args(
    "--release",
    "--classfile",
    "--output", r8File.get().path,
    "--pg-conf", rulesFile.path,
    "--lib", providers.systemProperty("java.home").get(),
    fatJarFile.get().toString(),
  )
}

val binaryFile = layout.buildDirectory.file("libs/$baseName-$version-binary.jar").map { it.asFile }
val binaryJar by tasks.registering {
  dependsOn(r8Jar)

  val r8FileProvider = layout.file(r8File)
  val binaryFileProvider = layout.file(binaryFile)
  inputs.files(r8FileProvider)
  outputs.file(binaryFileProvider)

  doLast {
    val r8File = r8FileProvider.get().asFile
    val binaryFile = binaryFileProvider.get().asFile

    binaryFile.parentFile.mkdirs()
    binaryFile.delete()
    binaryFile.writeText("#!/bin/sh\n\nexec java \$JAVA_OPTS -jar \$0 \"\$@\"\n\n")
    binaryFile.appendBytes(r8File.readBytes())

    binaryFile.setExecutable(true, false)
  }
}

tasks.test {
  dependsOn(binaryJar)
  systemProperty("CLI_PATH", binaryFile.get().absolutePath)

  useJUnitPlatform()
  maxParallelForks = Runtime.getRuntime().availableProcessors()

  // https://github.com/tginsberg/junit5-system-exit/issues/10
  systemProperty("java.security.manager", "allow")
}
