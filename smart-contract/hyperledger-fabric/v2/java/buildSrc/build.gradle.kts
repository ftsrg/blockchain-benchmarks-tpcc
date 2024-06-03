plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(gradleApi())
  implementation("net.lingala.zip4j:zip4j:2.11.5")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

kotlin {
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

group = "hu.bme.mit.ftsrg.gradle"
version = "0.1.0"