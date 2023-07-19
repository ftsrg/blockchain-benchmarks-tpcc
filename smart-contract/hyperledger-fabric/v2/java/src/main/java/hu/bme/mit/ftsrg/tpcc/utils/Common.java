/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.utils;

import lombok.experimental.UtilityClass;
import org.hyperledger.fabric.contract.Context;

@UtilityClass
public final class Common {

  // Logs the given debug message by appending the TX ID stub before it.
  public static void log(String msg, Context ctx, String level) {}

  static final int padLength = Integer.toString(Integer.MAX_VALUE).length();

  /**
   * Converts the number to text and pads it to a fix length.
   *
   * @param num The number to pad.
   * @return The padded number text.
   */
  public static String pad(int num) {
    return String.format("%0" + padLength + "d", num);
  }
}
