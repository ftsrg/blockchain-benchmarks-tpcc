import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("com.diffplug.spotless") version "6.19.0"
  id("io.freefair.lombok") version "8.6"
  id("io.freefair.aspectj.post-compile-weaving") version "8.6"
  id("hu.bme.mit.ftsrg.gradle.openjml.openjml")
  id("hu.bme.mit.ftsrg.gradle.major.major")
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

group = "hu.bme.mit.ftsrg.chaincode.tpcc"

version = "0.1.0"

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  implementation("org.slf4j:slf4j-api:2.0.13")
  implementation("org.slf4j:slf4j-simple:2.0.13")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
  implementation("com.jcabi:jcabi-aspects:0.26.0")
  implementation("org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim:2.5.0")
  implementation("org.hyperledger.fabric:fabric-protos:0.3.0")
  implementation(files("libs/hypernate-0.1.0-alpha-java8.jar"))

  aspect("com.jcabi:jcabi-aspects:0.26.0")

  testImplementation("org.assertj:assertj-core:3.26.0")
  testImplementation("org.junit.jupiter:junit-jupiter:5.11.0-M2")
  testImplementation("org.mockito:mockito-core:5.12.0")
  testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}

application { mainClass.set("org.hyperledger.fabric.contract.ContractRouter") }

tasks.named<ShadowJar>("shadowJar") {
  archiveBaseName.set("chaincode")
  archiveClassifier.set("")
  archiveVersion.set("")
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    showExceptions = true
    events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
  }

  doFirst {
    jvmArgs = listOf("-Dorg.jmlspecs.openjml.rac=exception")
  }
}

spotless {
  java {
    importOrder()
    removeUnusedImports()
    googleJavaFormat()
    formatAnnotations()
    toggleOffOn()
    licenseHeader("/* SPDX-License-Identifier: Apache-2.0 */")
  }
  kotlin {
    target("src/*/kotlin/**/*.kt", "buildSrc/src/*/kotlin/**/*.kt")
    ktfmt()
    licenseHeader("/* SPDX-License-Identifier: Apache-2.0 */")
  }
  kotlinGradle { ktfmt() }
}

tasks.named<JavaExec>("run") {
  doFirst {
    jvmArgs = listOf("-Dorg.jmlspecs.openjml.rac=exception")
    args = listOf("-a", "127.0.0.1:8541", "-i", "tpcc:0.1.0")
  }
}