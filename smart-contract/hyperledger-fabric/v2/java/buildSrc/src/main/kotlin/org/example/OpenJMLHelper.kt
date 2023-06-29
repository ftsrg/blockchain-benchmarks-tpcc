import java.io.File
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import net.lingala.zip4j.ZipFile
import org.gradle.api.file.Directory
import org.gradle.api.logging.Logger

/**
 * OpenJML compilation helpers.
 *
 * WARNING: Only compatible up to OpenJML v0.8.59 (the last version before the JDK16+ alphas).
 *
 * At the moment, compatible ONLY with v0.8.59. Earlier versions can be easily supported by
 * extending the RELEASE_DATE map.
 *
 * Originally based on https://github.com/mingyang91/openjml-template
 */
val RELEASE_DATE =
    mapOf<String, String>(
        "0.8.59" to "20211116",
        /* ... possibly older versions ... */
    )

// NOTE: ${'$'} is just an escaped $ inside a Kotlin string literal
val scriptPrefix =
    """
    #!/bin/sh -euCx

    args=
    for p in "${'$'}@"; do
      case "${'$'}p" in
      @*)
        # Try to slurp arguments from file at path after `@' sign
        args_file=${'$'}{p#@}
        if [ -f "${'$'}args_file" ]; then
            while IFS= read -r line; do
                args="${'$'}args ${'$'}line"
            done < "${'$'}args_file"
        else
            args="${'$'}args ${'$'}p"
        fi
        ;;

      *)
        # Regular parameter
        args="${'$'}args ${'$'}p"
        ;;
      esac
    done

"""
        .trimIndent()

fun downloadOpenJML(version: String, target: File, logger: Logger) {
  if (target.exists()) {
    logger.lifecycle("👌 OpenJML release archive is present in $target; no need to download")
    return
  }

  target.parentFile.mkdirs()
  target.createNewFile()

  val releaseDate: String =
      RELEASE_DATE.get(version)
          ?: run {
            logger.error("❌ Unknown OpenJML version $version")
            throw Exception("Unknown OpenJML version")
          }
  val downloadAddress =
      "https://github.com/OpenJML/OpenJML/releases/download/$version/openjml-$version-$releaseDate.zip"
  Channels.newChannel(URL(downloadAddress).openStream()).use { inChan ->
    logger.lifecycle("⬇️ OpenJML downloading...")
    target.outputStream().channel.use { outChan -> outChan.transferFrom(inChan, 0, Long.MAX_VALUE) }
  }
  logger.lifecycle("✅ OpenJML downloaded to $target")
}

fun extractOpenJML(zip: File, targetDir: Directory, logger: Logger) {
  if (targetDir.asFile.exists()) {
    logger.lifecycle("👌 OpenJML home is present at $targetDir; no need to unpack")
    return
  }

  logger.lifecycle("📦 OpenJML unzipping from $zip to $targetDir...")
  targetDir.asFile.mkdirs()
  ZipFile(zip).extractAll(targetDir.asFile.path)
  logger.lifecycle("✅ OpenJML unzipped to $targetDir")
}

internal fun generateScript(target: File, content: String, logger: Logger) {
  val name: String = target.nameWithoutExtension

  if (target.exists()) {
    logger.lifecycle("👌 $name present in $target; no need to generate")
    return
  }

  logger.lifecycle("⏳ Generating $name...")
  target.parentFile.mkdirs()
  Files.write(target.toPath(), (scriptPrefix + "\n" + content + "\n").toByteArray())
  target.setExecutable(true, true)
  logger.lifecycle("✅ $name generated at $target")
}

fun generateJmlavac(target: File, openJMLHome: Directory, logger: Logger) {
  generateScript(
      target,
      """OPENJML_ROOT="$openJMLHome" java -jar "$openJMLHome/openjml.jar" ${'$'}args""",
      logger)
}

fun generateJmlava(target: File, openJMLHome: Directory, logger: Logger) {
  generateScript(target, """java -cp "$openJMLHome/openjml.jar" ${'$'}args""", logger)
}

fun copyJavaHome(target: Directory, logger: Logger) {
  val original = org.gradle.internal.jvm.Jvm.current().getJavaHome()

  if (target.asFile.exists()) {
    logger.lifecycle("👌 Java home copy present in $target; no need to copy")
    return
  }

  logger.lifecycle("➡️ Copying Java home from $original to  $target...")
  original.copyRecursively(target.asFile, overwrite = true) { file, exception ->
    if (exception is NoSuchFileException) {
      logger.warn("⚠️ File $file not found; skipping")
      OnErrorAction.SKIP
    } else {
      logger.error("❌ Failed to copy $file: ${exception.message}")
      throw exception
    }
  }
  logger.lifecycle("✅ Java home copied to $target")
}

internal fun replaceExecutable(target: File, with: File, logger: Logger) {
  val name = target.nameWithoutExtension
  val backupFile = target.resolveSibling(File("$name.bak"))

  if (backupFile.exists()) {
    logger.lifecycle("👌 $name has already been replaced; no need to copy")
    return
  }

  logger.lifecycle("🔄 Replacing $name at $target with jmlavac at $with...")
  target.renameTo(backupFile)
  with.copyTo(target)
  target.setExecutable(true, true)
  logger.lifecycle("✅ $name replaced (original is $backupFile)")
}

fun replaceJavac(javaHome: Directory, jmlavac: File, logger: Logger) {
  replaceExecutable(javaHome.file("bin/javac").asFile, jmlavac, logger)
}

fun replaceJava(javaHome: Directory, jmlava: File, logger: Logger) {
  replaceExecutable(javaHome.file("bin/java").asFile, jmlava, logger)
}
