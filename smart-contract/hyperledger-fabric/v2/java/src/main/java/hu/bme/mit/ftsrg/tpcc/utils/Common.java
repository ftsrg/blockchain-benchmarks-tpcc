/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
SPDX-License-Identifier: Apache-2.0
*/

package hu.bme.mit.ftsrg.tpcc.utils;

import com.google.gson.Gson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.DataType;

// import com.google.gson.JsonObject;
// import com.google.gson.JsonParser;

@DataType()
public class Common {
  static Gson gson = new Gson();

  // public static final org.hyperledger.fabric.Logger logger = new Logger("tpcc");

  /**
   * Enumerates the tables of the TPC-C benchmark. //@type {{WAREHOUSE: string, DISTRICT: string,
   * CUSTOMER: string, HISTORY: string, NEW_ORDER: string, ORDER: string, ORDER_LINE: string, ITEM:
   * string, STOCK: string}}
   */
  public class TABLES {
    public static final String WAREHOUSE = "WAREHOUSE";
    public static final String DISTRICT = "DISTRICT";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String CUSTOMER_LAST_NAME = "CUSTOMER_LAST_NAME";
    public static final String HISTORY = "HISTORY";
    public static final String NEW_ORDER = "NEW_ORDER";
    public static final String ORDER = "ORDER";
    public static final String ORDER_LINE = "ORDER_LINE";
    public static final String ITEM = "ITEM";
    public static final String STOCK = "STOCK";
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

  // public static Object robustJsonParse(String value) {
  //   if (value == null) {
  //     throw new IllegalArgumentException("The received JSON string is null");
  //   }

  //   if (value.isEmpty()) {
  //     throw new IllegalArgumentException("The received JSON string is empty");
  //   }

  //   try {
  //     return gson.fromJson(value, Object.class);
  //     // JsonObject jsonObject = JsonParser.parseString(value).getAsJsonObject();
  //     // return jsonObject;

  //   } catch (Exception e) {
  //     return new Object();
  //   }
  // }
}
