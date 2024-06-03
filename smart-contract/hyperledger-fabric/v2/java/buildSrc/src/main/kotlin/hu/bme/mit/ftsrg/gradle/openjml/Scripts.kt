/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.gradle.openjml

import hu.bme.mit.ftsrg.gradle.util.writeUnlessExists
import java.io.File
import org.gradle.api.logging.Logger

private const val argParsePart =
    """#!/bin/sh -euC

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

internal fun File.writeWrapper(originalBinary: File, logger: Logger) {
  writeUnlessExists("""$argParsePart"${originalBinary.absolutePath}" ${'$'}args""", logger)
}
