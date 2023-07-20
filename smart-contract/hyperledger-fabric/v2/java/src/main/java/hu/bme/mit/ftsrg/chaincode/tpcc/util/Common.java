/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Common {

  private static final int padLength = Integer.toString(Integer.MAX_VALUE).length();

  /**
   * Converts the number to text and pads it to a fix length.
   *
   * @param num The number to pad.
   * @return The padded number text.
   */
  public static String pad(final int num) {
    return String.format("%0" + padLength + "d", num);
  }
}
