package hu.bme.mit.ftsrg.gradle.major

import hu.bme.mit.ftsrg.gradle.util.downloadUnlessExists
import hu.bme.mit.ftsrg.gradle.util.extractUnlessExists
import net.lingala.zip4j.ZipFile
import java.net.URI

val majorDir: Directory = layout.projectDirectory.dir(".major")
val majorMMLPath: String? by project
val majorMMLBinPath = "$majorMMLPath.bin"

plugins {
  java
}

tasks.register("initMajor") {
  doLast {
    val majorVersion: String by project
    val downloadDir: Provider<Directory> = layout.buildDirectory.dir("tmp/download")

    val uri = URI("https://mutation-testing.org/downloads/files/major-$majorVersion.zip")
    val file: File = downloadDir.get().file("major.zip").asFile
    uri.downloadUnlessExists(target = file, logger = logger, name = "Major Mutation Framework release")
    ZipFile(file).extractUnlessExists(
      targetDir = majorDir,
      logger = logger,
      name = "Major Mutation Framework directory"
    )

    logger.lifecycle("✅ Major successfully initialized in $majorDir")
  }
}

tasks.register<JavaExec>("compileMML") {
  doLast {
    dependsOn(tasks.named("initMajor"))

    if (majorMMLPath == null) {
      throw Error("The majorMMLPath property must be defined")
    }

    javaLauncher.set(javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(8) })
    classpath = files(majorDir.file("major/lib/major.jar"))
    args("--mmlc", majorMMLPath)

    inputs.file(majorMMLPath!!)
    outputs.file(majorMMLBinPath)
  }
}

tasks.register<JavaCompile>("generateMutants") {
  doLast {
    dependsOn(tasks.named("compileMML"))

    val compiler: Provider<JavaCompiler> =
      javaToolchains.compilerFor { languageVersion = JavaLanguageVersion.of(8) }
    javaCompiler.set(compiler)

    val defaultTask: JavaCompile = tasks.named<JavaCompile>("compileJava").get()
    source = defaultTask.source
    classpath = defaultTask.classpath + files(majorDir.file("major/lib/major.jar"))
    destinationDirectory.set(defaultTask.destinationDirectory)

    options.apply {
      isFork = true
      forkOptions.executable = compiler.get().executablePath.asFile.absolutePath
      compilerArgs.add("-Xplugin:MajorPlugin export.mutants mml:$majorMMLBinPath")
    }

    logger.lifecycle("✅ Generated mutants")
  }
}