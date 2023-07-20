/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.util;

import hu.bme.mit.ftsrg.chaincode.tpcc.data.input.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ParseUtils {

  /**
   * Parses the parameters of a New Order TX. (To enable IDE code completion)
   *
   * @param params The JSON string of the parameters.
   * @return The parsed parameters.
   */
  public static NewOrderInput parseNewOrderParameters(String params) {
    return JSON.deserialize(params, NewOrderInput.class);
  }

  /**
   * Parses the parameters of a Payment TX. (To enable IDE code completion)
   *
   * @param params The JSON string of the parameters.
   * @return The parsed parameters.
   */
  public static PaymentInput parsePaymentParameters(String params) {
    return JSON.deserialize(params, PaymentInput.class);
  }

  /**
   * Parses the parameters of a Delivery TX. (To enable IDE code completion)
   *
   * @param params The JSON string of the parameters.
   * @return The parsed parameters.
   */
  public static DeliveryInput parseDeliveryParameters(String params) {
    return JSON.deserialize(params, DeliveryInput.class);
  }

  /**
   * Parses the parameters of an Order Status TX. (To enable IDE code completion)
   *
   * @param params The JSON string of the parameters.
   * @return The parsed parameters.
   */
  public static OrderStatusInput parseOrderStatusParameters(String params) {
    return JSON.deserialize(params, OrderStatusInput.class);
  }

  /**
   * Parses the parameters of an Order Status TX. (To enable IDE code completion)
   *
   * @param params The JSON string of the parameters.
   * @return The parsed parameters.
   */
  public static StockLevelInput parseStockLevelParameters(String params) {
    return JSON.deserialize(params, StockLevelInput.class);
  }
}
