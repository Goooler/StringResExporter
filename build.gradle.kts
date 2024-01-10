plugins {
  kotlin("jvm") version "1.9.21"
  id("com.diffplug.spotless") version "6.23.3"
}

version = "0.1.0-SNAPSHOT"

java {
  toolchain.languageVersion = JavaLanguageVersion.of(8)
}

tasks.jar {
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
    "**/*.proto",
    "**/*.dex",
    "**/LICENSE**",
    "**/NOTICE**",
    "r8-version.properties",
    "migrateToAndroidx/*",
  )
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


dependencies {
  implementation("org.apache.poi:poi:5.2.0")
  implementation("org.jdom:jdom2:2.0.6.1")

  testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
  useJUnitPlatform()
}
