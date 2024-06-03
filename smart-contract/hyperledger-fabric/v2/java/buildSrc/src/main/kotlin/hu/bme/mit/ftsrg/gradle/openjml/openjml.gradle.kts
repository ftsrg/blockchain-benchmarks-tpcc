package hu.bme.mit.ftsrg.gradle.openjml

import gradle.kotlin.dsl.accessors._8c47cae829ea3d03260d5ff13fb2398e.java
import gradle.kotlin.dsl.accessors._8c47cae829ea3d03260d5ff13fb2398e.test
import hu.bme.mit.ftsrg.gradle.util.downloadUnlessExists
import hu.bme.mit.ftsrg.gradle.util.extractUnlessExists
import hu.bme.mit.ftsrg.gradle.util.replaceWith
import net.lingala.zip4j.ZipFile
import java.net.URI

val openJMLDir: Directory = layout.projectDirectory.dir(".openjml")
private val openJMLJavaHomeDir: Directory = openJMLDir.dir("jdk")

val withoutOpenJML: String? by project
val noOpenJML: Boolean = withoutOpenJML != null && withoutOpenJML.toBoolean()

tasks.register("initOpenJML") {
    val openJMLVersion: String by project
    val downloadDir: Provider<Directory> = layout.buildDirectory.dir("tmp/download")

    val uri =
        URI("https://github.com/OpenJML/OpenJML/releases/download/$openJMLVersion/openjml-ubuntu-20.04-$openJMLVersion.zip")
    val file: File = downloadDir.get().file("openjml.zip").asFile
    uri.downloadUnlessExists(target = file, logger = logger, name = "OpenJML release")
    ZipFile(file).extractUnlessExists(openJMLDir, logger, "OpenJML directory")

    // javac and java binaries (currently OpenJML's original, will be replaced with wrapper scripts)
    val javacBinary: File = openJMLJavaHomeDir.file("bin/javac").asFile
    val javaBinary: File = openJMLJavaHomeDir.file("bin/java").asFile

    // where OpenJML's javac and java binaries will be backed up
    val javacBackup: File = javacBinary.resolveSibling("javac-openjml-original")
    val javaBackup: File = javaBinary.resolveSibling("java-openjml-original")

    // where our wrapper scripts will go (which will delegate to the backup javac and java binaries)
    val javacWrapper: File = javacBinary.resolveSibling("javac-wrapper")
    val javaWrapper: File = javaBinary.resolveSibling("java-wrapper")

    // write the wrapper scripts (pointing to the backup binaries which do not exist yet actually)
    javacWrapper.writeWrapper(originalBinary = javacBackup, logger = logger)
    javaWrapper.writeWrapper(originalBinary = javaBackup, logger = logger)

    // replace javac and java binaries with scripts (at this point, backups of the binaries are also created)
    javacBinary.replaceWith(with = javacWrapper, logger = logger, backupFile = javacBackup)
    javaBinary.replaceWith(with = javaWrapper, logger = logger, backupFile = javaBackup)

    logger.lifecycle("✅ OpenJML successfully initialized in $openJMLDir")
}

if (!noOpenJML) {
    dependencies {
        add(configurationName = "implementation", dependencyNotation = files("$openJMLDir/jmlruntime.jar"))
        add(configurationName = "testImplementation", dependencyNotation = files("$openJMLDir/jmlruntime.jar"))
    }

    tasks.withType<JavaCompile> {
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
                    "--$mode",
                    "--nullable-by-default",
                    "--specs-path",
                    "specs/",
                )
            )
            options.forkOptions.javaHome = openJMLJavaHomeDir.asFile
        }
    }

    tasks.withType<Jar> { dependsOn(tasks.named("initOpenJML")) }

    tasks.test {
        java {
            executable = "$openJMLJavaHomeDir/bin/java"
            //classpath += files("$openJMLDir/jmlruntime.jar")
            jvmArgs = listOf("-Dorg.jmlspecs.openjml.rac=exception")
        }
    }
}