/* Originally based on https://github.com/mingyang91/openjml-template */

import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.File

val openJMLDir = layout.projectDirectory.dir(".openjml")
val openJMLJavaHomeDir = openJMLDir.dir("java")
val downloadDir = layout.buildDirectory.dir(".tmp/download")

val jmlavac = openJMLJavaHomeDir.file("bin/jmlavac")
val jmlava = openJMLJavaHomeDir.file("bin/jmlava")

plugins {
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("com.diffplug.spotless") version "6.13.0"
}

group = "org.example"

version = "0.1.0"

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  implementation("org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim:2.5.0")
  implementation("org.json:json:20230227")
  implementation("com.google.code.gson:gson:2.10.1")
  // Included also as implementation dependency so shadow will package it
  implementation(files("$openJMLDir/jmlruntime.jar"))

  testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation("org.mockito:mockito-core:2.28.2")
  testImplementation(files("$openJMLDir/jmlruntime.jar"))
}

application { mainClass.set("org.hyperledger.fabric.contract.ContractRouter") }

tasks.named<ShadowJar>("shadowJar") {
  archiveBaseName.set("chaincode")
  archiveClassifier.set("")
  archiveVersion.set("")
}

tasks.getByName<Test>("test") { useJUnitPlatform() }

tasks.test {
  java {
    executable = "$openJMLDir/bin/jmlava"
    jvmArgs = listOf("-Dorg.jmlspecs.openjml.rac=exception")
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile>().configureEach {
  dependsOn(tasks.named("initOpenJML"))

  val mode =
      when (System.getenv("JML_MODE")) {
        "esc" -> "esc"
        else -> "rac"
      }
  if (name == "compileJava") {
    options.isFork = true
    options.compilerArgs.addAll(listOf("-jml", "-$mode", "-timeout", "30"))
    options.forkOptions.javaHome = openJMLJavaHomeDir.asFile
  }
}

configure<SpotlessExtension> {
  java {
    importOrder()
    removeUnusedImports()
    googleJavaFormat()
    formatAnnotations()
  }
  kotlin {
    target("src/*/kotlin/**/*.kt", "buildSrc/src/*/kotlin/**/*.kt")
    ktfmt()
  }
  kotlinGradle { ktfmt() }
}

tasks.register("initOpenJML") {
  val openJMLVersion: String by project

  val zipFile: File = downloadDir.get().file("openjml.zip").asFile
  downloadOpenJML(openJMLVersion, zipFile, logger)
  extractOpenJML(zipFile, openJMLDir, logger)

  copyJavaHome(openJMLJavaHomeDir, logger)
  // `jmlavac' is what we call `javac' that is actually
  // OpenJML's javac; likewise, `jmlava' is a wrapper for `java' with
  // OpenJML already in the classpath
  generateJmlavac(jmlavac.asFile, openJMLDir, logger)
  replaceJavac(openJMLJavaHomeDir, jmlavac.asFile, logger)
  generateJmlava(jmlava.asFile, openJMLDir, logger)
  replaceJava(openJMLJavaHomeDir, jmlava.asFile, logger)
  logger.lifecycle("✅ OpenJML successfully initialized in $openJMLDir")
}