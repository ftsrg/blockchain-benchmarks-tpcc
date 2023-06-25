import net.lingala.zip4j.ZipFile;

import org.gradle.internal.os.OperatingSystem;

import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.file.Files

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    application
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "org.example"
version = "0.1.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim:2.5.0")
    implementation("org.json:json:20230227")
    implementation("com.google.code.gson:gson:2.10.1")
    // Included also as implementation dependency so shadow will package it
    implementation(files("openjml/jmlruntime.jar"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.mockito:mockito-core:2.28.2")
    testImplementation(files("libs/jmlruntime.jar"))
}

application {
    mainClass.set("org.hyperledger.fabric.contract.ContractRouter")
}

tasks.named<ShadowJar>("shadowJar") {
   archiveBaseName.set("chaincode")
   archiveClassifier.set("")
   archiveVersion.set("")
}


tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val openjml_path = layout.projectDirectory.dir("openjml")

tasks.test {
    java {
        executable = "$openjml_path/jdk/bin/java"
        jvmArgs = listOf("-Dorg.jmlspecs.openjml.rac=exception")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile>().configureEach {
    val mode = when (System.getenv("JML_MODE")) {
        "esc" -> "esc"
        else -> "rac"
    }
    if (name == "compileJava") {
        options.isFork = true
        options.compilerArgs.addAll(listOf(
                "-jml",
                "--$mode",
                "--timeout",
                "30"
        ))
        //options.forkOptions.javaHome = File("$openjml_path/jdk")
        //options.forkOptions.executable = "${System.getProperty("java.home")}/bin/java -jar $openjml_path/openjml.jar"
        options.forkOptions.executable = "./javavac"
    }
}

tasks.register("downloadOpenJML", ::downloadOpenJML)


fun downloadOpenJML(action: Task) {
    val currentOS = OperatingSystem.current()
    val jmlVersion = "0.8.59"
    val jmlReleaseDate = "20211116"
    val jmlDownloadAddress = "https://github.com/OpenJML/OpenJML/releases/download/${jmlVersion}/openjml-${jmlVersion}-${jmlReleaseDate}.zip"

    val downloadFile = layout.buildDirectory
        .file("tmp/download/openjml.zip").get()

    val unzipFile = openjml_path

    if (downloadFile.asFile.exists()) {
        logger.lifecycle("👌 OpenJML package is present in ${downloadFile}, no need to do this")
    } else {
        downloadFile.asFile.parentFile.mkdirs()
        downloadFile.asFile.createNewFile()

        val url = URL(jmlDownloadAddress)
        Channels.newChannel(url.openStream()).use { inChan ->
            logger.lifecycle("⬇️ OpenJML downloading")
            downloadFile.asFile.outputStream().channel.use { outChan ->
                outChan.transferFrom(inChan, 0, Long.MAX_VALUE);
            }
        }
        logger.lifecycle("✅ OpenJML downloaded, $downloadFile")
    }


    if (unzipFile.asFile.exists()) {
        logger.lifecycle("👌 OpenJML home is present in ${unzipFile}, no need to do this")
    } else {
        logger.lifecycle("\uD83D\uDCE6 OpenJML unpacking, $unzipFile")

        unzipFile.asFile.mkdirs()

	val zipFile = ZipFile(downloadFile.asFile)
	zipFile.extractAll(unzipFile.asFile.path)
        logger.lifecycle("✅ OpenJML unpacked, $unzipFile")
    }

    val originJavac = openjml_path.file("jdk/bin/origin-javac")

    if (originJavac.asFile.exists()) {
        logger.lifecycle("\uD83D\uDC4C javac has been replaced")
    } else {
        val oldJavac = openjml_path.file("jdk/bin/javac")
        oldJavac.asFile.renameTo(originJavac.asFile)
        Files.write(oldJavac.asFile.toPath(), """
            #!/bin/bash
            args=()
            for var in "${'$'}@"
            do
              # if starts with @, the tail is path.
              # if exists, read contains as list of strings
              # otherwise, pass as is
              if [[ ${'$'}var == @* ]]; then
                tail=${'$'}{var:1}
                if [ -f "${'$'}tail" ]; then
                  while IFS= read -r line
                  do
                    args+=("${'$'}line")
                  done < "${'$'}tail"
                else
                  args+=("${'$'}var")
                fi
              else
                args+=("${'$'}var")
              fi
            done

            SCRIPT_DIR=${'$'}( cd -- "${'$'}( dirname -- "${'$'}{BASH_SOURCE[0]}" )" &> /dev/null && pwd )
            OPENJML_ROOT="$openjml_path" ${'$'}SCRIPT_DIR/origin-javac "${'$'}{args[@]}"
        """.trimIndent().toByteArray())
        oldJavac.asFile.setExecutable(true, true)
        logger.lifecycle("✅ Javac is replaced, $unzipFile")
    }

    val originJava = openjml_path.file("jdk/bin/origin-java")

    if (originJava.asFile.exists()) {
        logger.lifecycle("\uD83D\uDC4C java has been replaced")
    } else {
        val oldJava = openjml_path.file("jdk/bin/java")
        oldJava.asFile.renameTo(originJava.asFile)
        Files.write(oldJava.asFile.toPath(), """
            #!/bin/bash
            args=()
            for var in "${'$'}@"
            do
              # if starts with @, the tail is path.
              # if exists, read contains as list of strings
              # otherwise, pass as is
              if [[ ${'$'}var == @* ]]; then
                tail=${'$'}{var:1}
                if [ -f "${'$'}tail" ]; then
                  while IFS= read -r line
                  do
                    args+=("${'$'}line")
                  done < "${'$'}tail"
                else
                  args+=("${'$'}var")
                fi
              else
                args+=("${'$'}var")
              fi
            done

            SCRIPT_DIR=${'$'}( cd -- "${'$'}( dirname -- "${'$'}{BASH_SOURCE[0]}" )" &> /dev/null && pwd )
            OPENJML_ROOT="$openjml_path" ${'$'}SCRIPT_DIR/origin-java "${'$'}{args[@]}"
        """.trimIndent().toByteArray())
        oldJava.asFile.setExecutable(true, true)
        logger.lifecycle("✅ Java is replaced, $unzipFile")
    }
}
