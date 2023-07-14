/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.utils;

import com.google.gson.Gson;
import hu.bme.mit.ftsrg.tpcc.TPCC;
import hu.bme.mit.ftsrg.tpcc.entries.*;
import hu.bme.mit.ftsrg.tpcc.inputs.*;
import java.util.logging.Logger;

public class ParseUtils {

  static Gson gson = new Gson();
  private static final Logger LOGGER = Logger.getLogger(TPCC.class.getName());

  /**
   * Parses the parameters of a New Order TX. (To enable IDE code completion)
   *
   * @param params The JSON string of the parameters.
   * @return The parsed parameters.
   */
  public static DoNewOrderInputParameters parseNewOrderParameters(String params) throws Exception {
    DoNewOrderInputParameters newOrderParams =
        gson.fromJson(params, DoNewOrderInputParameters.class);
    return newOrderParams;
  }

  /**
   * Parses the parameters of a Payment TX. (To enable IDE code completion)
   *
   * @param {string} params The JSON string of the parameters.
   * @return {PaymentParameters} The parsed parameters.
   */
  public static DoPaymentInputParameters parsePaymentParameters(String params) throws Exception {
    DoPaymentInputParameters paymentParams = gson.fromJson(params, DoPaymentInputParameters.class);
    return paymentParams;
  }

  /**
   * Parses the parameters of a Delivery TX. (To enable IDE code completion)
   *
   * @param {string} params The JSON string of the parameters.
   * @return {DeliveryParameters} The parsed parameters.
   */
  public static DoDeliveryInputParameters parseDeliveryParameters(String params) throws Exception {
    LOGGER.info("Parse Delivery parameters " + params);
    DoDeliveryInputParameters deliveryParams =
        gson.fromJson(params, DoDeliveryInputParameters.class);
    LOGGER.info("Delivery parameters are: " + deliveryParams);
    return deliveryParams;
  }

  /**
   * Parses the parameters of an Order Status TX. (To enable IDE code completion)
   *
   * @param {string} params The JSON string of the parameters.
   * @return {OrderStatusParameters} The parsed parameters.
   */
  public static DoOrderStatusInputParameters parseOrderStatusParameters(String params)
      throws Exception {
    DoOrderStatusInputParameters oStatusParams =
        gson.fromJson(params, DoOrderStatusInputParameters.class);
    return oStatusParams;
  }

  /**
   * Parses the parameters of an Order Status TX. (To enable IDE code completion)
   *
   * @param {string} params The JSON string of the parameters.
   * @return {StockLevelParameters} The parsed parameters.
   */
  public static DoStockLevelInputParameters parseStockLevelParameters(String params)
      throws Exception {
    LOGGER.info("parses stock level parameters");
    DoStockLevelInputParameters stockLevelParams =
        gson.fromJson(params, DoStockLevelInputParameters.class);
    LOGGER.info("stock level parameters: " + stockLevelParams);
    return stockLevelParams;
  }

  /**
   * Parses the Warehouse entry JSON string. (To enable IDE code completion)
   *
   * @param {string} jsonString The JSON string of the warehouse.
   * @return {Warehouse} The warehouse object.
   */
  public static Warehouse parseWarehouse(String jsonString) {
    LOGGER.info("Starting parseWarehouse for " + jsonString + "Warehouse parameters");
    Warehouse warehouseParams = gson.fromJson(jsonString, Warehouse.class);
    LOGGER.info("parseWarehouse returned " + warehouseParams.toString() + "Warehouse parameters");
    return warehouseParams;
  }

  /**
   * Parses the District entry JSON string. (To enable IDE code completion)
   *
   * @param jsonString The JSON string of the district.
   * @return The district object.
   */
  public static District parseDistrict(String jsonString) {
    District distParams = gson.fromJson(jsonString, District.class);
    return distParams;
  }

  /**
   * Parses the Customer entry JSON string. (To enable IDE code completion)
   *
   * @param {string} jsonString The JSON string of the customer.
   * @return {Customer} The customer object.
   */
  public static Customer parseCustomer(String jsonString) {
    LOGGER.info("parse customer parameters");
    Customer custParams = gson.fromJson(jsonString, Customer.class);
    LOGGER.info("return parsed customer parameters");
    return custParams;
  }

  /**
   * Parses the History entry JSON string. (To enable IDE code completion)
   *
   * @param {string} jsonString The JSON string of the history.
   * @return {History} The history object.
   */
  public static History parseHistory(String jsonString) {
    History historyParams = gson.fromJson(jsonString, History.class);
    return historyParams;
  }

  /**
   * Parses the New Order entry JSON string. (To enable IDE code completion)
   *
   * @param {string} jsonString The JSON string of the new order.
   * @return {NewOrder} The new order object.
   */
  public static NewOrder parseNewOrder(String jsonString) {
    LOGGER.info("parse NewOrder Parameter");
    NewOrder newOParams = gson.fromJson(jsonString, NewOrder.class);
    LOGGER.info("Returned parsed NewOrder parameters: " + gson.toJson(newOParams));
    return newOParams;
  }

  /**
   * Parses the Order entry JSON string. (To enable IDE code completion)
   *
   * @param {string} jsonString The JSON string of the order.
   * @return {Order} The order object.
   */
  public static Order parseOrder(String jsonString) {
    LOGGER.info("parse order parameters: " + jsonString);
    Order orderParams = gson.fromJson(jsonString, Order.class);
    LOGGER.info("Returned parsed order parameters are: " + gson.toJson(orderParams));
    return orderParams;
  }

  /**
   * Parses the Order Line entry JSON string. (To enable IDE code completion)
   *
   * @param {string} jsonString The JSON string of the order line.
   * @return {OrderLine} The order line object.
   */
  public static OrderLine parseOrderLine(String jsonString) {
    OrderLine oLineParams = gson.fromJson(jsonString, OrderLine.class);
    return oLineParams;
  }

  /**
   * Parses the Item entry JSON string. (To enable IDE code completion)
   *
   * @param {string} jsonString The JSON string of the item.
   * @return {Item} The item object.
   */
  public static Item parseItem(String jsonString) {
    Item itemParams = gson.fromJson(jsonString, Item.class);
    return itemParams;
  }

  /**
   * Parses the Stock entry JSON string. (To enable IDE code completion)
   *
   * @param {string} jsonString The JSON string of the stock.
   * @return {Stock} The stock object.
   */
  public static Stock parseStock(String jsonString) {
    Stock stockParams = gson.fromJson(jsonString, Stock.class);
    return stockParams;
  }
}
