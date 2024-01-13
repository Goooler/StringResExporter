plugins {
  kotlin("jvm") version "1.9.22"
  id("com.diffplug.spotless") version "6.23.3"
}

version = "0.1.1-SNAPSHOT"
val archivesBaseName = "string-res-exporter"

java {
  toolchain.languageVersion = JavaLanguageVersion.of(8)
}

tasks.jar {
  archiveBaseName = archivesBaseName

  manifest {
    attributes["Main-Class"] = "io.github.goooler.exporter.MainKt"
  }
}

val fatJar by tasks.registering(Jar::class) {
  dependsOn(configurations.runtimeClasspath)
  dependsOn(tasks.jar)

  archiveClassifier = "fat"

  manifest {
    attributes["Main-Class"] = "io.github.goooler.exporter.MainKt"
    attributes["Implementation-Version"] = archiveVersion
  }

  from(files(sourceSets.main.map { it.output.classesDirs }))
  from(configurations.runtimeClasspath.get().asFileTree.files.map { zipTree(it) })

  exclude(
    "**/*.kotlin_metadata",
    "**/*.kotlin_builtins",
    "**/*.kotlin_module",
    "**/module-info.class",
    "META-INF/maven/**",
    "META-INF/proguard/**",
    "META-INF/*.version",
    "META-INF/DEPENDENCIES",
    "**/*.proto",
    "**/*.dex",
    "**/LICENSE**",
    "**/NOTICE**",
    "r8-version.properties",
    "migrateToAndroidx/*",
  )
}

val r8File = layout.buildDirectory.file("libs/$archivesBaseName-$version-r8.jar").get().asFile
val rulesFile = project.file("src/main/rules.txt")
val r8Jar by tasks.registering(JavaExec::class) {
  val fatJarFile = fatJar.get().archiveFile
  dependsOn(fatJar)
  inputs.file(fatJarFile)
  inputs.file(rulesFile)
  outputs.file(r8File)

  classpath(r8)
  mainClass = "com.android.tools.r8.R8"
  args(
    "--release",
    "--classfile",
    "--output", r8File.toString(),
    "--pg-conf", rulesFile.path,
    "--lib", System.getProperty("java.home"),
    fatJarFile.get().toString(),
  )
}

val binaryFile = layout.buildDirectory.file("libs/$archivesBaseName-$version-binary.jar").get().asFile
val binaryJar by tasks.registering(Task::class) {
  dependsOn(r8Jar)

  inputs.file(r8File)
  outputs.file(binaryFile)

  doLast {
    binaryFile.parentFile.mkdirs()
    binaryFile.delete()
    binaryFile.writeText("#!/bin/sh\n\nexec java \$JAVA_OPTS -jar \$0 \"\$@\"\n\n")
    r8File.inputStream().use { binaryFile.appendBytes(it.readBytes()) }

    binaryFile.setExecutable(true, false)
  }
}

tasks.test {
  dependsOn(binaryJar)
  systemProperty("CLI_PATH", binaryFile.absolutePath)

  useJUnitPlatform()
}

spotless {
  kotlin {
    ktlint()
    target("**/src/*.kt")
  }
  kotlinGradle {
    ktlint()
  }
}

val r8: Configuration by configurations.creating

dependencies {
  implementation("org.apache.poi:poi:5.2.5")
  implementation("org.jdom:jdom2:2.0.6.1")
  implementation("org.apache.logging.log4j:log4j-core:2.22.1")

  r8("com.android.tools:r8:8.2.42")

  testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
  testImplementation("com.willowtreeapps.assertk:assertk:0.28.0")
}
