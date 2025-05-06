import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm") version "2.1.20"
  id("com.github.gmazzo.buildconfig") version "5.6.5"
  id("com.gradleup.shadow") version "9.0.0-beta13"
  id("com.diffplug.spotless") version "7.0.3"
}

version = "0.3.0-SNAPSHOT"
val baseName = "string-res-exporter"

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
  compilerOptions.jvmTarget = JvmTarget.JVM_1_8
}

tasks.withType<Jar>().configureEach {
  archiveBaseName = baseName
  archiveVersion = version.toString()

  manifest {
    attributes["Main-Class"] = "io.github.goooler.exporter.MainKt"
    attributes["Implementation-Version"] = version.toString()
  }
}

tasks.shadowJar {
  mergeServiceFiles()

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

buildConfig {
  buildConfigField("VERSION_NAME", version.toString())
  packageName = "io.github.goooler.exporter"
}

spotless {
  kotlin {
    ktlint()
    target("**/src/**/*.kt")
  }
  kotlinGradle {
    ktlint()
  }
}

val r8: Configuration by configurations.creating

dependencies {
  implementation("org.apache.poi:poi:5.4.1")
  implementation("org.jdom:jdom2:2.0.6.1")
  implementation("com.github.ajalt.clikt:clikt:5.0.3")

  r8("com.android.tools:r8:8.7.18")

  testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
  testImplementation("com.ginsberg:junit5-system-exit:1.1.2")
  testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
