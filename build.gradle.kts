plugins {
  kotlin("jvm") version "1.9.21"
}

version = "0.1.0-SNAPSHOT"

java {
  toolchain.languageVersion = JavaLanguageVersion.of(8)
}

dependencies {
  implementation("org.apache.poi:poi:5.2.0")
  implementation("org.jdom:jdom2:2.0.6")

  testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
  useJUnitPlatform()
}
