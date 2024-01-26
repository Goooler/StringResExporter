pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.enterprise") version "3.16.2"
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val isCI = providers.environmentVariable("CI").isPresent

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    publishAlwaysIf(isCI)
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
  }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
