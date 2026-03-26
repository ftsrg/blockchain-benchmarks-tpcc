/* Originally based on https://github.com/mingyang91/openjml-template */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import hu.bme.mit.ftsrg.openjmlhelper.*
import org.gradle.api.tasks.testing.logging.TestLogEvent

val openJMLDir = layout.projectDirectory.dir(".openjml")
val openJMLJavaHomeDir = openJMLDir.dir("jdk")
val downloadDir = layout.buildDirectory.dir("tmp/download")

val jmlavac = openJMLJavaHomeDir.file("bin/jmlavac")
val jmlava = openJMLJavaHomeDir.file("bin/jmlava")

val withoutOpenJML: String? by project
val noOpenJML: Boolean = withoutOpenJML != null && withoutOpenJML.toBoolean()

plugins {
  application
  id("com.gradleup.shadow") version "9.4.0"
  id("com.diffplug.spotless") version "8.4.0"
  id("io.freefair.lombok") version "9.2.0"
  id("io.freefair.aspectj.post-compile-weaving") version "9.2.0"
}

// java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

group = "hu.bme.mit.ftsrg.chaincode.tpcc"

version = "0.1.0"

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  implementation("org.slf4j:slf4j-api:2.0.17")
  implementation("org.slf4j:slf4j-simple:2.0.17")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.21.2")
  implementation("com.jcabi:jcabi-aspects:0.26.0")
  implementation("org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim:2.5.8")
  implementation("org.hyperledger.fabric:fabric-protos:0.3.7")
  implementation(files("libs/hypernate-0.1.0-alpha.jar"))
  implementation(files("$openJMLDir/jmlruntime.jar"))

  aspect("com.jcabi:jcabi-aspects:0.26.0")

  testImplementation("org.assertj:assertj-core:4.0.0-M1")
  testImplementation("org.junit.jupiter:junit-jupiter:6.1.0-M1")
  testImplementation("org.mockito:mockito-core:5.23.0")
  testImplementation(files("$openJMLDir/jmlruntime.jar"))
}

application { mainClass.set("org.hyperledger.fabric.contract.ContractRouter") }

tasks.named<ShadowJar>("shadowJar") {
  archiveBaseName.set("chaincode")
  archiveClassifier.set("")
  archiveVersion.set("")
}

tasks.register("initOpenJML") {
  val openJMLVersion: String by project

  val zipFile: File = downloadDir.get().file("openjml.zip").asFile
  downloadOpenJML(openJMLVersion, zipFile, logger)
  extractOpenJML(zipFile, openJMLDir, logger)

  // `jmlavac' is what we call `javac' that is actually
  // OpenJML's javac; likewise, `jmlava' is a wrapper for `java' with
  // OpenJML already in the classpath
  generateJmlavac(jmlavac.asFile, openJMLJavaHomeDir, logger)
  replaceJavac(openJMLJavaHomeDir, jmlavac.asFile, logger)
  generateJmlava(jmlava.asFile, openJMLJavaHomeDir, logger)
  replaceJava(openJMLJavaHomeDir, jmlava.asFile, logger)
  logger.lifecycle("✅ OpenJML successfully initialized in $openJMLDir")
}

if (!noOpenJML) {
  tasks.named<ShadowJar>("shadowJar") { dependsOn(tasks.named("initOpenJML")) }

  tasks.test {
    java {
      executable = "$openJMLDir/bin/jmlava"
      jvmArgs = listOf("-Dorg.jmlspecs.openjml.rac=exception")
    }
  }

  tasks.withType<JavaCompile>().configureEach {
    dependsOn(tasks.named("initOpenJML"))
    // Only when not compiling because of Spotless
    if (!gradle.startParameter.taskNames.any { it.contains("spotlessApply") }) {
      val mode =
          when (System.getenv("JML_MODE")) {
            "esc" -> "esc"
            else -> "rac"
          }
      options.isFork = true
      options.compilerArgs.addAll(
          listOf(
              "-jml",
              "-$mode",
              "-timeout",
              "30",
              "--nullable-by-default",
              "--specs-path",
              "specs/"))
      options.forkOptions.javaHome = openJMLJavaHomeDir.asFile
    }
  }
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    showExceptions = true
    events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
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
