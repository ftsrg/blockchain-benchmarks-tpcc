/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.gradle.util

import java.io.File
import org.gradle.api.logging.Logger

fun File.replaceWith(with: File, logger: Logger, backupFile: File = resolveSibling("$name.bak")) {
  val name = nameWithoutExtension
  val isExecutable = canExecute()

  if (backupFile.exists()) {
    logger.lifecycle("⏩ $name has already been replaced")
    return
  }

  logger.lifecycle("🔄 Replacing $name at $this with $with...")
  renameTo(backupFile)
  with.copyTo(this)
  if (isExecutable) {
    setExecutable(true, true)
  }
  logger.lifecycle("✅ $name replaced (original is $backupFile)")
}
