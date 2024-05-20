/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.gradle.util

import java.io.File
import java.net.URI
import java.nio.channels.Channels
import net.lingala.zip4j.ZipFile
import org.gradle.api.file.Directory
import org.gradle.api.logging.Logger

fun URI.downloadUnlessExists(target: File, logger: Logger, name: String = "File") {
  if (target.exists()) {
    logger.lifecycle("⏩ $name is present in $target; no need to download")
    return
  }

  target.parentFile.mkdirs()
  target.createNewFile()

  Channels.newChannel(toURL().openStream()).use { inChan ->
    logger.lifecycle("⬇️ $name downloading...")
    target.outputStream().channel.use { outChan -> outChan.transferFrom(inChan, 0, Long.MAX_VALUE) }
  }
  logger.lifecycle("✅ $name downloaded to $target")
}

fun ZipFile.extractUnlessExists(targetDir: Directory, logger: Logger, name: String = "Directory") {
  if (targetDir.asFile.exists()) {
    logger.lifecycle("⏩ $name is present at $targetDir; no need to unpack")
    return
  }

  logger.lifecycle("📦 $name unzipping from $this to $targetDir...")
  targetDir.asFile.mkdirs()
  extractAll(targetDir.asFile.path)
  logger.lifecycle("✅ $name unzipped to $targetDir")
}
