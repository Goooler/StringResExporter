plugins {
  kotlin("jvm") version "1.9.21"
  id("com.diffplug.spotless") version "6.23.3"
}

version = "0.1.0-SNAPSHOT"

java {
  toolchain.languageVersion = JavaLanguageVersion.of(8)
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
