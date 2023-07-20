/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.utils;

import hu.bme.mit.ftsrg.tpcc.inputs.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ParseUtils {

  /**
   * Parses the parameters of a New Order TX. (To enable IDE code completion)
   *
   * @param params The JSON string of the parameters.
   * @return The parsed parameters.
   */
  public static DoNewOrderInputParameters parseNewOrderParameters(String params) {
    return JSON.deserialize(params, DoNewOrderInputParameters.class);
  }

  /**
   * Parses the parameters of a Payment TX. (To enable IDE code completion)
   *
   * @param params The JSON string of the parameters.
   * @return The parsed parameters.
   */
  public static DoPaymentInputParameters parsePaymentParameters(String params) {
    return JSON.deserialize(params, DoPaymentInputParameters.class);
  }

  /**
   * Parses the parameters of a Delivery TX. (To enable IDE code completion)
   *
   * @param params The JSON string of the parameters.
   * @return The parsed parameters.
   */
  public static DoDeliveryInputParameters parseDeliveryParameters(String params) {
    final DoDeliveryInputParameters deliveryParams =
        JSON.deserialize(params, DoDeliveryInputParameters.class);
    return deliveryParams;
  }

  /**
   * Parses the parameters of an Order Status TX. (To enable IDE code completion)
   *
   * @param params The JSON string of the parameters.
   * @return The parsed parameters.
   */
  public static DoOrderStatusInputParameters parseOrderStatusParameters(String params) {
    return JSON.deserialize(params, DoOrderStatusInputParameters.class);
  }

  /**
   * Parses the parameters of an Order Status TX. (To enable IDE code completion)
   *
   * @param params The JSON string of the parameters.
   * @return The parsed parameters.
   */
  public static DoStockLevelInputParameters parseStockLevelParameters(String params) {
    final DoStockLevelInputParameters stockLevelParams =
        JSON.deserialize(params, DoStockLevelInputParameters.class);
    return stockLevelParams;
  }
}
