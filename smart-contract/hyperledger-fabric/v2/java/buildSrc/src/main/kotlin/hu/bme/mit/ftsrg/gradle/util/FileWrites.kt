/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.gradle.util

import java.io.File
import java.nio.file.Files
import org.gradle.api.logging.Logger

fun File.writeUnlessExists(content: String, logger: Logger) {
  val name: String = nameWithoutExtension

  if (exists()) {
    logger.lifecycle("⏩ $name present in $this; no need to write")
    return
  }

  logger.lifecycle("⏳ Generating $name...")
  parentFile.mkdirs()
  Files.write(toPath(), (content + "\n").toByteArray())
  logger.lifecycle("✅ $name generated at $this")
}
