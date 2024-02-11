plugins {
  kotlin("jvm") version "1.9.22"
  id("com.github.gmazzo.buildconfig") version "5.3.5"
  id("com.diffplug.spotless") version "6.25.0"
  id("com.android.lint") version "8.2.2"
}

version = "0.3.0-SNAPSHOT"
val baseName = "string-res-exporter"

java {
  toolchain.languageVersion = JavaLanguageVersion.of(8)
}

tasks.withType<Jar>().configureEach {
  archiveBaseName = baseName
  archiveVersion = version.toString()

  manifest {
    attributes["Main-Class"] = "io.github.goooler.exporter.MainKt"
    attributes["Implementation-Version"] = version.toString()
  }
}

val fatJar by tasks.registering(Jar::class) {
  dependsOn(configurations.runtimeClasspath)
  dependsOn(tasks.jar)

  from(sourceSets.main.map { it.output.classesDirs + it.output.resourcesDir })
  from(configurations.runtimeClasspath.map { it.asFileTree.files.map(::zipTree) })

  archiveClassifier = "fat"

  exclude(
    "**/*.kotlin_metadata",
    "**/*.kotlin_builtins",
    "**/*.kotlin_module",
    "**/module-info.class",
    "META-INF/maven/**",
    "META-INF/proguard/**",
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
  dependsOn(fatJar)

  val fatJarFile = fatJar.get().archiveFile
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
    "--lib", System.getProperty("java.home"),
    fatJarFile.get().toString(),
  )
}

val binaryFile = layout.buildDirectory.file("libs/$baseName-$version-binary.jar").map { it.asFile }
val binaryJar by tasks.registering(Task::class) {
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
}

buildConfig {
  buildConfigField("VERSION_NAME", version.toString())
  buildConfigField("CLI_NAME", baseName)
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
  implementation("org.apache.poi:poi:5.2.5")
  implementation("org.jdom:jdom2:2.0.6.1")
  implementation("com.github.ajalt.clikt:clikt:4.2.2")

  r8("com.android.tools:r8:8.2.42")

  testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
  testImplementation("com.willowtreeapps.assertk:assertk:0.28.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  lintChecks("com.jzbrooks:assertk-lint:1.1.1")
}
