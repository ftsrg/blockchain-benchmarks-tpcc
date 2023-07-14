/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.utils;

import org.hyperledger.fabric.contract.Context;

public class Common {

  /** Enumerates the tables of the TPC-C benchmark. */
  public class TABLES {
    static final String WAREHOUSE = "WAREHOUSE";
    static final String DISTRICT = "DISTRICT";
    static final String CUSTOMER = "CUSTOMER";
    static final String CUSTOMER_LAST_NAME = "CUSTOMER_LAST_NAME";
    static final String HISTORY = "HISTORY";
    static final String NEW_ORDER = "NEW_ORDER";
    static final String ORDERS = "ORDERS";
    static final String ORDER_LINE = "ORDER_LINE";
    static final String ITEM = "ITEM";
    static final String STOCK = "STOCK";
  }

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
