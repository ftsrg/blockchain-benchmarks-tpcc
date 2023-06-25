plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(gradleApi())
  implementation("org.apache.commons:commons-compress:1.22")
  implementation("net.lingala.zip4j:zip4j:2.11.5")
}

group = "edu.lsbf"
version = "1.0"
