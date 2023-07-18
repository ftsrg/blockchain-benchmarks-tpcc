/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http =//www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
SPDX-License-Identifier = Apache-2.0
*/

package hu.bme.mit.ftsrg.tpcc;

import com.google.gson.Gson;
import hu.bme.mit.ftsrg.tpcc.entries.*;
import hu.bme.mit.ftsrg.tpcc.inputs.*;
import hu.bme.mit.ftsrg.tpcc.outputs.*;
import hu.bme.mit.ftsrg.tpcc.stub.EnhancedContext;
import hu.bme.mit.ftsrg.tpcc.utils.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.json.JSONObject;

@Contract(
    name = "TPCC",
    info =
        @Info(
            title = "tpcc contract",
            description = "My Smart Contract",
            version = "0.0.1",
            license = @License(name = "Apache-2.0", url = ""),
            contact = @Contact(email = "tpcc@example.com", name = "tpcc", url = "http://tpcc.me")))

/**
 * The implementation of the TPC-C benchmark smart contract according to the specification version
 * v5.11.0.
 */
@Default
public class TPCC implements ContractInterface {

  public TPCC() {}

  private static final Logger LOGGER = Logger.getLogger(TPCC.class.getName());
  Gson gson = new Gson();

  /**
   * Performs the Delivery read-write TX profile.
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String doDelivery(EnhancedContext ctx, String parameters) {

    // TPC-C 2.7.4.2
    LOGGER.info("Starting Delivery TX with parameters" + parameters);
    try {
      DoDeliveryInputParameters params = ParseUtils.parseDeliveryParameters(parameters);
      // For a given warehouse number (W_ID), for each of the districts (D_W_ID ,
      // D_ID)
      // within that warehouse, and for a given carrier number (O_CARRIER_ID):

      List<DeliveredOrder> deliveredOrders = new ArrayList<>();
      int skipped = 0;
      for (int d_id = 1; d_id <= 10; d_id++) {
        LOGGER.info("Begin for loop to retrieve oldest new orders from the various districts");
        // The row in the NEW-ORDER table with matching NO_W_ID (equals W_ID) and
        // NO_D_ID (equals D_ID) and with the lowest NO_O_ID value is selected.
        // This is the oldest undelivered order of that district.
        // NO_O_ID, the order number, is retrieved.

        NewOrder newO = new NewOrder();
        newO.no_w_id = params.w_id;
        newO.no_d_id = d_id;
        List<NewOrder> orders = ctx.registry.readAll(ctx, newO);
        NewOrder oldestNewOrder = orders.get(0);
        LOGGER.info("Oldest new order retrieved is: " + gson.toJson(oldestNewOrder));

        if (oldestNewOrder == null) {
          // If no matching row is found, then the delivery of an order for this district
          // is skipped. The condition in which no outstanding order is present at a given
          // district must be handled by skipping the delivery of an order for that
          // district
          // only and resuming the delivery of an order from all remaining districts of
          // the
          // selected warehouse. If this condition occurs in more than 1%, or in more than
          // one,
          // whichever is greater, of the business transactions, it must be reported.
          LOGGER.info(
              "Could not find new order for District( "
                  + params.w_id
                  + ","
                  + d_id
                  + " ) skipping it");
          skipped += 1;
          continue;
        }
        ctx.registry.delete(ctx, oldestNewOrder);
        LOGGER.info(gson.toJson(oldestNewOrder) + "DELETED");
        // The row in the ORDER table with matching O_W_ID (equals W_ ID), O_D_ID
        // (equals D_ID),
        // and O_ID (equals NO_O_ID) is selected, O_C_ID, the customer number, is
        // retrieved,
        // and O_CARRIER_ID is updated.
        Order orderRecord = new Order();
        orderRecord.o_w_id = params.w_id;
        orderRecord.o_d_id = d_id;
        orderRecord.o_id = oldestNewOrder.no_o_id;

        Order order = ctx.registry.read(ctx, orderRecord);

        LOGGER.info(
            "retrieved order details with no_o_id: "
                + oldestNewOrder.no_o_id
                + " and Warehouse and District IDs "
                + params.w_id);
        order.o_carrier_id = params.o_carrier_id;
        ctx.registry.update(ctx, order);
        LOGGER.info(
            "Updated order" + gson.toJson(order) + "with order.o_carrier_id" + params.o_carrier_id);

        // All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID),
        // OL_D_ID
        // (equals O_D_ID), and OL_O_ID (equals O_ID) are selected. All OL_DELIVERY_D,
        // the
        // delivery dates, are updated to the current system time as returned by the
        // operating
        // system and the sum of all OL_AMOUNT is retrieved.
        Double orderLineAmountTotal = 0.0;
        for (int i = 1; i <= order.o_ol_cnt; i++) {
          LOGGER.info("getOrderLine");
          OrderLine ol = new OrderLine();
          ol.ol_w_id = params.w_id;
          ol.ol_d_id = d_id;
          ol.ol_o_id = order.o_id;
          ol.ol_number = i;
          OrderLine orderLine = ctx.registry.read(ctx, ol);
          LOGGER.info("OrderLine: " + gson.toJson(orderLine) + "retrieved");

          orderLineAmountTotal += orderLine.ol_amount;
          orderLine.ol_delivery_d = params.ol_delivery_d;
          LOGGER.info(
              "updateOrderLine with orderLineAmountTotal "
                  + orderLineAmountTotal
                  + "and ol_delivery_d "
                  + orderLine.ol_delivery_d);
          ctx.registry.update(ctx, orderLine);
        }

        // The row in the CUSTOMER table with matching C_W_ID (equals W_ID), C_D_ID
        // (equals D_ID), and C_ID (equals O_C_ID) is selected and C_BALANCE is
        // increased by the sum of all order-line amounts (OL_AMOUNT) previously
        // retrieved.
        // C_DELIVERY_CNT is incremented by 1.
        LOGGER.info(
            "getCustomer with W_ID, D_ID and C_ID" + params.w_id + "," + d_id + "," + order.o_c_id);

        Customer cust = new Customer();
        cust.c_w_id = params.w_id;
        cust.c_d_id = d_id;
        cust.c_id = order.o_c_id;

        Customer customer = ctx.registry.read(ctx, cust);
        LOGGER.info("Customer: " + gson.toJson(customer) + "retrieved");
        customer.c_balance += orderLineAmountTotal;
        customer.c_delivery_cnt += 1;
        LOGGER.info(
            "updateCustomer. C_BALANCE is increased by the sum of all order-line amounts (OL_AMOUNT)"
                + "previously retrieved and C_DELIVERY_CNT is incremented by 1 ");
        ctx.registry.update(ctx, customer);

        deliveredOrders.add(new DeliveredOrder(d_id, order.o_id));
      }

      DoDeliveryOutput output =
          new DoDeliveryOutput(params.w_id, params.o_carrier_id, deliveredOrders, skipped);

      LOGGER.info("Finished Delivery TX with output: " + gson.toJson(output));
      return gson.toJson(output);
    } catch (Exception err) {
      LOGGER.info(err.toString());
    }
    return null;
  }

  /**
   * Performs the New Order read-write TX profile.
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return {Promise<{object}>} The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String doNewOrder(EnhancedContext ctx, String parameters) {

    // TPC-C 2.4.2.2
    LOGGER.info("Starting NewOrder TX with parameters" + parameters);

    try {
      DoNewOrderInputParameters params = ParseUtils.parseNewOrderParameters(parameters);

      // The row in the WAREHOUSE table with matching W_ID is selected and W_TAX,
      // the warehouse tax rate, is retrieved.
      Warehouse wh = new Warehouse();
      wh.w_id = params.w_id;
      final Warehouse warehouse = ctx.registry.read(ctx, wh);
      LOGGER.info("Warehouse " + gson.toJson(warehouse) + "retrieved");

      // The row in the DISTRICT table with matching D_W_ID and D_ ID is selected,
      // D_TAX, the district tax rate, is retrieved, and D_NEXT_O_ID, the next
      // available order number for the district, is retrieved and incremented by one.

      District dist = new District();
      dist.d_w_id = warehouse.w_id;
      dist.d_id = params.d_id;
      District district = ctx.registry.read(ctx, dist);
      LOGGER.info("District " + gson.toJson(district) + "retrieved");
      int nextOrderId = district.d_next_o_id;
      district.d_next_o_id += 1;
      ctx.registry.update(ctx, district);
      LOGGER.info(
          "Next available order number for District "
              + district.d_id
              + " incremented. New District values are: "
              + gson.toJson(district));

      // The row in the CUSTOMER table with matching C_W_ID, C_D_ID, and C_ID is
      // selected and C_DISCOUNT, the customer's discount rate, C_LAST, the customer's
      // last name, and C_CREDIT, the customer's credit status, are retrieved.

      Customer cust = new Customer();
      cust.c_w_id = warehouse.w_id;
      cust.c_d_id = district.d_id;
      cust.c_id = params.c_id;

      final Customer customer = ctx.registry.read(ctx, cust);
      LOGGER.info(
          "Customer "
              + customer.c_id
              + " with w_id "
              + customer.c_w_id
              + " and d_id "
              + customer.c_d_id
              + " retrieved");
      LOGGER.info(gson.toJson(customer));

      // A new row is inserted into both the NEW-ORDER table and the ORDER table to
      // reflect the creation of the new order. O_CARRIER_ID is set to a null value.
      // If the order includes only home order-lines, then O_ALL_LOCAL is set to 1,
      // otherwise O_ALL_LOCAL is set to 0.

      final NewOrder newOrder = new NewOrder();
      newOrder.no_o_id = nextOrderId;
      newOrder.no_d_id = district.d_id;
      newOrder.no_w_id = warehouse.w_id;

      ctx.registry.create(ctx, newOrder);
      LOGGER.info("New Order " + gson.toJson(newOrder) + "created");

      boolean allItemsLocal = true;
      for (int id : params.i_w_ids) {
        if (id != warehouse.w_id) {
          allItemsLocal = false;
          break;
        }
      }

      final Order order = new Order();
      order.o_id = nextOrderId;
      order.o_d_id = district.d_id;
      order.o_w_id = warehouse.w_id;
      order.o_c_id = customer.c_id;
      order.o_entry_d = params.o_entry_d;
      order.o_carrier_id = 0;
      order.o_ol_cnt = params.i_ids.length;
      order.o_all_local = allItemsLocal ? 1 : 0;
      ctx.registry.create(ctx, order);
      LOGGER.info("Created Order " + gson.toJson(order));

      List<ItemsData> itemsData = new ArrayList<>();

      Double totalOrderLineAmount = 0.0;
      // For each O_OL_CNT item on the order
      for (int i = 0; i < params.i_ids.length; i++) {
        int i_id = params.i_ids[i];
        int i_w_id = params.i_w_ids[i];
        int i_qty = params.i_qtys[i];

        // The row in the ITEM table with matching I_ID (equals OL_I_ID) is selected
        // and I_PRICE, the price of the item, I_NAME, the name of the item, and
        // I_DATA are retrieved. If I_ID has an unused value (see Clause 2.4.1.5), a
        // "not-found" condition is signaled, resulting in a rollback of the
        // database transaction (see Clause 2.4.2.3).

        Item itemEntry = new Item();
        itemEntry.i_id = i_id;

        Item item = ctx.registry.read(ctx, itemEntry);
        LOGGER.info("Retrived item " + gson.toJson(item) + " with item id " + i_id);

        if (item == null) {
          // 2.4.3.4 For transactions that are rolled back as a result of an
          // unused item number (1% of all New-Order transactions), the emulated
          // terminal must display in the appropriate fields of the input/output
          // screen the fields: W_ID, D_ID, C_ID, C_LAST, C_CREDIT, O_ID, and the
          // execution status message "Item number is not valid".

          HashMap<String, Object> errorOutput = new HashMap<>();
          errorOutput.put("w_id", warehouse.w_id);
          errorOutput.put("d_id", district.d_id);
          errorOutput.put("c_id", customer.c_id);
          errorOutput.put("c_last", customer.c_last);
          errorOutput.put("c_credit", customer.c_credit);
          errorOutput.put("o_id", order.o_id);
          errorOutput.put("message", "Item number is not valid");

          LOGGER.info("Item not found. Error output:" + errorOutput.toString());
          throw new Exception(new JSONObject(errorOutput).toString());
        }

        // The row in the STOCK table with matching S_I_ID (equals OL_I_ID) and
        // S_W_ID (equals OL_SUPPLY_W_ID) is selected. S_QUANTITY, the quantity in
        // stock, S_DIST_xx, where xx represents the district number, and S_DATA are
        // retrieved. If the retrieved value for S_QUANTITY exceeds OL_QUANTITY by
        // 10 or more, then S_QUANTITY is decreased by OL_QUANTITY; otherwise
        // S_QUANTITY is updated to (S_QUANTITY - OL_QUANTITY)+91. S_YTD is
        // increased by OL_QUANTITY and S_ORDER_CNT is incremented by 1. If the
        // order-line is remote, then S_REMOTE_CNT is incremented by 1.

        Stock stockEntry = new Stock();
        stockEntry.s_w_id = i_w_id;
        stockEntry.s_i_id = i_id;

        Stock stock = ctx.registry.read(ctx, stockEntry);
        LOGGER.info("Stock " + gson.toJson(stock) + " retrieved");

        if (stock.s_quantity >= i_qty + 10) {
          stock.s_quantity -= i_qty;
        } else {
          stock.s_quantity = (stock.s_quantity - i_qty) + 91;
        }

        stock.s_ytd += i_qty;
        stock.s_order_cnt += 1;

        if (i_w_id != warehouse.w_id) {
          stock.s_remote_cnt += 1;
        }

        ctx.registry.update(ctx, stock);
        LOGGER.info("Updated stock with Entries: " + gson.toJson(stock));

        // The amount for the item in the order (OL_AMOUNT) is computed as:
        // OL_QUANTITY * I_PRICE
        Double orderLineAmount = i_qty * item.i_price;
        totalOrderLineAmount += orderLineAmount;

        // The strings in I_DATA and S_DATA are examined. If they both include the
        // string "ORIGINAL", the brand-generic field for that item is set to "B",
        // otherwise, the brand-generic field is set to "G".
        String brandGeneric;
        if (item.i_data.contains("ORIGINAL") && stock.s_data.contains("ORIGINAL")) {
          brandGeneric = "B";
        } else {
          brandGeneric = "G";
        }

        // A new row is inserted into the ORDER-LINE table to reflect the item on
        // the order. OL_DELIVERY_D is set to a null value, OL_NUMBER is set to a
        // unique value within all the ORDER-LINE rows that have the same OL_O_ID
        // value, and OL_DIST_INFO is set to the content of S_DIST_xx, where xx
        // represents the district number (OL_D_ID)
        String stockDistrictId = String.format("%02d", district.d_id);

        final OrderLine orderLine = new OrderLine();
        orderLine.ol_o_id = nextOrderId;
        orderLine.ol_d_id = district.d_id;
        orderLine.ol_w_id = warehouse.w_id;
        orderLine.ol_number = i + 1;
        orderLine.ol_i_id = i_id;
        orderLine.ol_supply_w_id = i_w_id;
        orderLine.ol_delivery_d = null;
        orderLine.ol_quantity = i_qty;
        orderLine.ol_amount = orderLineAmount;
        orderLine.ol_dist_info = ("s_dist_" + stockDistrictId);

        ctx.registry.create(ctx, orderLine);
        LOGGER.info("OrderLine " + gson.toJson(orderLine) + " created");

        // 2.4.3.3 The emulated terminal must display, in the appropriate fields of
        // the input/ output screen, all input data and the output data resulting
        // from the execution of the transaction. The display field s are divided in
        // two groups as follows:
        // - One repeating group of fields: OL_SUPPLY_W_ID, OL_I_ID, I_NAME,
        // OL_QUANTITY, S_QUANTITY, brand_generic, I_PRICE, and OL_AMOUNT. The group
        // is repeated O_OL_CNT times (once per item in the order), equal to the
        // computed value of ol_cnt.

        itemsData.add(
            new ItemsData(
                orderLine.ol_supply_w_id,
                orderLine.ol_i_id,
                item.i_name,
                orderLine.ol_quantity,
                stock.s_quantity,
                brandGeneric,
                item.i_price,
                orderLine.ol_amount));
        LOGGER.info("ItemsData" + gson.toJson(itemsData));
      }

      // The total-amount for the complete order is computed as:
      // sum(OL_AMOUNT) * (1 - C_DISCOUNT) * (1 + W_TAX + D_TAX)

      Double totalAmount =
          totalOrderLineAmount * (1 - customer.c_discount) * (1 + warehouse.w_tax + district.d_tax);
      LOGGER.info("total amount = " + totalAmount);
      System.out.println(totalAmount);

      // 2.4.3.3 The emulated terminal must display, in the appropriate fields of
      // the input/ output screen, all input data and the output data resulting
      // from the execution of the transaction. The display field s are divided in
      // two groups as follows:
      // - One non-repeating group of fields: W_ID, D_ID, C_ID, O_ID, O_OL_CNT,
      // C_LAST, C_CREDIT, C_DISCOUNT, W_TAX, D_TAX, O_ENTRY_D, total_amount, and an
      // optional execution status message other than "Item number is not valid".

      DoNewOrderOutput output =
          new DoNewOrderOutput(
              warehouse.w_id,
              district.d_id,
              customer.c_id,
              customer.c_last,
              customer.c_credit,
              customer.c_discount,
              warehouse.w_tax,
              district.d_tax,
              order.o_ol_cnt,
              order.o_id,
              order.o_entry_d,
              totalAmount,
              itemsData);

      LOGGER.info("Finished New Order TX with output" + gson.toJson(output));
      return gson.toJson(output);
    } catch (Exception err) {
      LOGGER.info("ERROR" + err.toString() + "occured");
    }
    LOGGER.info("Error occured. NULL retured");
    return null;
  }

  /**
   * Performs the Order Status read TX profile.
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String doOrderStatus(EnhancedContext ctx, String parameters) {
    // TPC-C 2.6.2.2
    try {
      final DoOrderStatusInputParameters params = ParseUtils.parseOrderStatusParameters(parameters);

      Customer customer =
          LedgerUtils.getCustomersByIdOrLastName(
              ctx, params.w_id, params.d_id, params.c_id, params.c_last);
      LOGGER.info(
          "getCustomersByIdOrLastName returned Customer: "
              + customer.c_first
              + customer.c_last
              + " with ID: "
              + customer.c_id);

      // The row in the ORDER table with matching O_W_ID (equals C_W_ID), O_D_ID
      // (equals C_D_ID),
      // O_C_ID (equals C_ID), and with the largest existing O_ID, is selected. This
      // is the most
      // recent order placed by that customer. O_ID, O_ENTRY_D, and O_CARRIER_ID are
      // retrieved.

      ///////////////////////////////////////////////////////////////////////////////////////////
      Order order =
          LedgerUtils.getLastOrderOfCustomer(ctx, customer.c_w_id, customer.c_d_id, customer.c_id);
      LOGGER.info("getLastOrderOfCustomer returned: " + gson.toJson(order));
      ///////////////////////////////////////////////////////////////////////////////////////////////
      // All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID),
      // OL_D_ID (equals
      // O_D_ID), and OL_O_ID (equals O_ID) are selected and the corresponding sets of
      // OL_I_ID,
      // OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, and OL_DELIVERY_D are retrieved.
      // ctx.txinfo.md_tpcc_order_status_order_lines = order.o_ol_cnt;

      List<OrderLineData> orderLineData = new ArrayList<>();

      for (int i = 1; i <= order.o_ol_cnt; i++) {
        OrderLine ol = new OrderLine();
        ol.ol_w_id = order.o_w_id;
        ol.ol_d_id = order.o_d_id;
        ol.ol_o_id = order.o_id;
        ol.ol_number = i;
        OrderLine orderLine = ctx.registry.read(ctx, ol);
        LOGGER.info("get OrderLine with order.o_ids: " + order.o_id + "retrieved");

        OrderLineData olData = new OrderLineData();
        olData.ol_supply_w_id = orderLine.ol_supply_w_id;
        olData.ol_i_id = orderLine.ol_i_id;
        olData.ol_quantity = orderLine.ol_quantity;
        olData.ol_amount = orderLine.ol_amount;
        olData.ol_delivery_d = orderLine.ol_delivery_d;

        orderLineData.add(olData);

        LOGGER.info("ordeline data retrived: " + orderLineData);
      }

      // 2.6.3.3 The emulated terminal must display, in the appropriate fields of the
      // input/output
      // screen, all input data and the output data resulting from the execution of
      // the
      // transaction. The display fields are divided in two groups as follows:
      // - One non-repeating group of fields: W_ID, D_ID, C_ID, C_FIRST, C_MIDDLE,
      // C_LAST,
      // C_BALANCE, O_ID, O_ENTRY_D, and O_CARRIER_ID;
      // - One repeating group of fields: OL_SUPPLY_W_ID, OL_I_ID, OL_QUANTITY,
      // OL_AMOUNT, and
      // OL_DELIVERY_D. The group is repeated O_OL_CNT times (once per item in the
      // order).

      DoOrderStatusOutput output = new DoOrderStatusOutput();
      output.w_id = params.w_id;
      output.d_id = params.d_id;
      output.c_id = customer.c_id;
      output.c_first = customer.c_first;
      output.c_middle = customer.c_middle;
      output.c_last = customer.c_last;
      output.c_balance = customer.c_balance;
      output.o_id = order.o_id;
      output.o_entry_d = order.o_entry_d;
      output.o_carrier_id = order.o_carrier_id;
      output.order_lines = orderLineData;

      LOGGER.info("Finished Order Status TX with output: " + gson.toJson(output));
      return gson.toJson(output);
    } catch (Exception err) {
      Common.log(err.toString(), ctx, "error");
    }
    return null;
  }

  /**
   * Performs the Payment read-write TX profile.
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String doPayment(EnhancedContext ctx, String parameters) {
    // TPC-C 2.5.2.2
    LOGGER.info("Starting Payment TX with parameters: " + parameters);
    try {
      DoPaymentInputParameters params = ParseUtils.parsePaymentParameters(parameters);

      // The row in the WAREHOUSE table with matching W_ID is selected. W_NAME,
      // W_STREET_1, W_STREET_2, W_CITY, W_STATE, and W_ZIP are retrieved and W_YTD,
      // the
      // warehouse's year-to-date balance, is increased by H_ AMOUNT.
      Warehouse wh = new Warehouse();
      wh.w_id = params.w_id;
      Warehouse warehouse = ctx.registry.read(ctx, wh);
      LOGGER.info("Retrieved warehouse with ID " + params.w_id);
      warehouse.w_ytd += params.h_amount;
      ctx.registry.update(ctx, warehouse);
      LOGGER.info("Updated warehouse's year-to-date balance  with new value: " + warehouse.w_ytd);

      // The row in the DISTRICT table with matching D_W_ID and D_ID is selected.
      // D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE, and D_ZIP are retrieved and
      // D_YTD, the district's year-to-date balance, is increased by H_AMOUNT.
      District dist = new District();
      dist.d_w_id = warehouse.w_id;
      dist.d_id = params.d_id;
      District district = ctx.registry.read(ctx, dist);
      LOGGER.info("Read entry for  district " + params.d_id);
      district.d_ytd += params.h_amount;
      ctx.registry.update(ctx, district);
      LOGGER.info("Update district's year-to-date balance with value: " + district.d_ytd);

      Customer customer =
          LedgerUtils.getCustomersByIdOrLastName(
              ctx, warehouse.w_id, district.d_id, params.c_id, params.c_last);
      LOGGER.info("Customer " + gson.toJson(customer) + "retrieved by last name " + params.c_last);

      // C_BALANCE is decreased by H_AMOUNT. C_YTD_PAYMENT is increased by H_AMOUNT.
      // C_PAYMENT_CNT is incremented by 1.
      customer.c_balance -= params.h_amount;
      customer.c_ytd_payment += params.h_amount;
      customer.c_payment_cnt += 1;

      // If the value of C_CREDIT is equal to "BC", then C_DATA is also retrieved from
      // the selected customer and the following history information: C_ID, C_D_ID,
      // C_W_ID, D_ID, W_ID, and H_AMOUNT, are inserted at the left of the C_DATA
      // field
      // by shifting the existing content of C_DATA to the right by an equal number of
      // bytes and by discarding the bytes that are shifted out of the right side of
      // the
      // C_DATA field. The content of the C_DATA field never exceeds 500 characters.
      // The
      // selected customer is updated with the new C_DATA field.

      if (customer.c_credit.equals("BC")) {
        String history =
            customer.c_id
                + " "
                + customer.c_d_id
                + " "
                + customer.c_w_id
                + " "
                + district.d_id
                + " "
                + warehouse.w_id
                + " "
                + params.h_amount;
        customer.c_data = history + "|" + customer.c_data;
        LOGGER.info(
            "history information: "
                + customer.c_id
                + ", "
                + customer.c_d_id
                + ", "
                + customer.c_w_id
                + ", "
                + district.d_id
                + ", "
                + warehouse.w_id
                + "and "
                + params.h_amount
                + "inserted at the left of the C_DATA");

        if (customer.c_data.length() > 500) customer.c_data = customer.c_data.substring(0, 500);
      }

      ctx.registry.update(ctx, customer);
      LOGGER.info("customer updated with the new C_DATA field.");

      // H_DATA is built by concatenating W_NAME and D_NAME separated by 4 spaces.
      String h_data = warehouse.w_name + "    " + district.d_name;

      // A new row is inserted into the HISTORY table with H_C_ID = C_ID,
      // H_C_D_ID = C_D_ID, H_C_W_ID = C_W_ID, H_D_ID = D_ID, and H_W_ID = W_ID.
      History history = new History();
      history.h_c_id = customer.c_id;
      history.h_c_d_id = customer.c_d_id;
      history.h_c_w_id = customer.c_w_id;
      history.h_d_id = district.d_id;
      history.h_w_id = warehouse.w_id;
      history.h_date = params.h_date;
      history.h_amount = params.h_amount;
      history.h_data = h_data;

      ctx.registry.create(ctx, history);
      LOGGER.info("History created with data " + gson.toJson(history));

      // 2.5.3.3 The emulated terminal must display, in the appropriate fields of the
      // input/output screen, all input data and the output data resulting from the
      // execution of the transaction. The following fields are displayed: W_ID, D_ID,
      // C_ID, C_D_ID, C_W_ID, W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP,
      // D_STREET_1, D_STREET_2, D_CITY, D_STATE, D_ZIP, C_FIRST, C_MIDDLE, C_LAST,
      // C_STREET_1, C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT,
      // C_CREDIT_LIM, C_DISCOUNT, C_BALANCE, the first 200 characters of C_DATA (only
      // if C_CREDIT = "BC"), H_AMOUNT, and H_DATE.

      DoPaymentOutput output = new DoPaymentOutput();
      output.w_id = warehouse.w_id;
      output.d_id = district.d_id;
      output.c_id = customer.c_id;
      output.c_d_id = customer.c_d_id;
      output.c_w_id = customer.c_w_id;
      output.h_amount = history.h_amount;
      output.h_date = history.h_date;
      output.w_street_1 = warehouse.w_street_1;
      output.w_street_2 = warehouse.w_street_2;
      output.w_city = warehouse.w_city;
      output.w_state = warehouse.w_state;
      output.w_zip = warehouse.w_zip;
      output.d_street_1 = district.d_street_1;
      output.d_street_2 = district.d_street_2;
      output.d_city = district.d_city;
      output.d_state = district.d_state;
      output.d_zip = district.d_zip;
      output.c_first = customer.c_first;
      output.c_middle = customer.c_middle;
      output.c_last = customer.c_last;
      output.c_street_1 = customer.c_street_1;
      output.c_street_2 = customer.c_street_2;
      output.c_city = customer.c_city;
      output.c_state = customer.c_state;
      output.c_zip = customer.c_zip;
      output.c_phone = customer.c_phone;
      output.c_since = customer.c_since;
      output.c_credit = customer.c_credit;
      output.c_credit_lim = customer.c_credit_lim;
      output.c_discount = customer.c_discount;
      output.c_balance = customer.c_balance;

      if (customer.c_credit.equals("BC")) {
        output.c_data = customer.c_data.substring(0, 200);
      }

      LOGGER.info("Finished Payment TX with output: " + gson.toJson(output));

      return gson.toJson(output);
    } catch (Exception err) {
      Common.log(err.toString(), ctx, "error");
    }
    return null;
  }

  /**
   * Performs the Stock Level read TX profile.
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String doStockLevel(EnhancedContext ctx, String parameters) {
    // TPC-C 2.8.2.2
    LOGGER.info("Starting Stock Level TX with parameters: " + parameters);
    try {
      final DoStockLevelInputParameters params = ParseUtils.parseStockLevelParameters(parameters);
      // The row in the DISTRICT table with matching D_W_ID and D_ID is selected and
      // D_NEXT_O_ID is retrieved.

      District dist = new District();
      dist.d_w_id = params.w_id;
      dist.d_id = params.d_id;
      final District district = ctx.registry.read(ctx, dist);
      LOGGER.info("District " + district.d_id + " RETRIEVED");

      // All rows in the ORDER-LINE table with matching OL_W_ID (equals W_ID), OL_D_ID
      // (equals
      // D_ID), and OL_O_ID (lower than D_NEXT_O_ID and greater than or equal to
      // D_NEXT_O_ID
      // minus 5) are selected. They are the items for 5 recent orders of the
      // district.

      final int o_id_min = district.d_next_o_id - 5;
      final int o_id_max = district.d_next_o_id;
      LOGGER.info("o_id_min = " + o_id_min + " and o_id_max = " + o_id_max);
      LOGGER.info("get recent 5 orders");
      ////////////////////////////////////////////////////////////////////////////
      List<Integer> recentItemIds =
          LedgerUtils.getItemIdsOfRecentOrders(ctx, params.w_id, district.d_id, o_id_min, o_id_max);
      ///////////////////////////////////////////////////////////////////////////////////////
      LOGGER.info("getItemIdsOfRecentOrders returned " + recentItemIds);
      // ctx.txinfo.md_tpcc_stock_level_recent_items = recentItemIds.length;

      // All rows in the STOCK table with matching S_I_ID (equals OL_I_ID) and S_W_ID
      // (equals W_ID)
      // from the list of distinct item numbers and with S_QUANTITY lower than
      // threshold are counted
      // (giving low_stock).
      int lowStock = 0;
      for (int i_id : recentItemIds) {
        Stock itemsStock = new Stock();
        itemsStock.s_w_id = params.w_id;
        itemsStock.s_i_id = i_id;
        Stock stock = ctx.registry.read(ctx, itemsStock);
        LOGGER.info("get stock for items> " + i_id);
        if (stock.s_quantity < params.threshold) {
          LOGGER.info("The stock quantity is less than the threshold");
          lowStock += 1;
        }
        LOGGER.info("LowStock = " + lowStock);
      }

      // 2.8.3.3 The emulated terminal must display, in the appropriate field of the
      // input/
      // output screen, all input data and the output data which results from the
      // execution of
      // the transaction. The following fields are displayed: W_ID, D_ID, threshold,
      // and low_stock.

      DoStockLevelOutput output = new DoStockLevelOutput();
      output.w_id = params.w_id;
      output.d_id = params.d_id;
      output.threshold = params.threshold;
      output.low_stock = lowStock;

      LOGGER.info("Finished Stock Level TX with output: " + gson.toJson(output));

      return gson.toJson(output);

    } catch (Exception err) {
      Common.log(err.toString(), ctx, "error");
    }
    return null;
  }

  /**
   * Initializes the TPC-C chaincode.
   *
   * @param ctx The TX context.
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public void instantiate(EnhancedContext ctx) {
    Common.log("Instantiating TPC-C chaincode", ctx, "info");
    LOGGER.info("Instantiating TPC-C chaincode");
  }

  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void initEntries(EnhancedContext ctx) {
    LOGGER.info("Starting initEntries");
    try {
      Warehouse warehouse = new Warehouse();
      warehouse.w_id = 1;
      warehouse.w_name = "W_One";
      warehouse.w_street_1 = "xyz";
      warehouse.w_street_2 = "123";
      warehouse.w_city = "Budapest";
      warehouse.w_state = "LA";
      warehouse.w_zip = "00011111";
      warehouse.w_tax = 0.1000;
      warehouse.w_ytd = 10000;

      ctx.registry.create(ctx, warehouse);
      LOGGER.info("Warehouse " + gson.toJson(warehouse) + "innitialized");

      District district = new District();
      district.d_id = 1;
      district.d_w_id = 1;
      district.d_name = "D_One";
      district.d_street_1 = "abc";
      district.d_street_2 = "456";
      district.d_city = "Budapest";
      district.d_state = "BP";
      district.d_zip = "00111111";
      district.d_tax = 0.0100;
      district.d_ytd = 10000;
      district.d_next_o_id = 3001;

      ctx.registry.create(ctx, district);
      LOGGER.info("District " + gson.toJson(district) + "innitialized");

      District district2 = new District();
      district2.d_id = 2;
      district2.d_w_id = 1;
      district2.d_name = "D_Two";
      district2.d_street_1 = "cde";
      district2.d_street_2 = "567";
      district2.d_city = "Budapest";
      district2.d_state = "BP";
      district2.d_zip = "00111111";
      district2.d_tax = 0.0100;
      district2.d_ytd = 10000;
      district2.d_next_o_id = 3001;

      ctx.registry.create(ctx, district2);
      LOGGER.info("District " + gson.toJson(district2) + "innitialized");

      Customer customer1 =
          new Customer(
              1,
              1,
              1,
              "Alice",
              "Is",
              "Yong",
              "xyz",
              "123",
              "Budapest",
              "Buda",
              "00101111",
              "123456789",
              "19/01/2020",
              "GC",
              50000,
              0.25,
              1000.00,
              10,
              1,
              0,
              "Good credit");

      Customer customer2 =
          new Customer(
              2,
              1,
              1,
              "Peter",
              "Peet",
              "Peter",
              "ABC",
              "23",
              "Budapest",
              "DC",
              "00011111",
              "456712389",
              "19/01/2020",
              "GC",
              50000,
              0.30,
              1000.00,
              10,
              1,
              0,
              "Good credit");

      ctx.registry.create(ctx, customer2);
      LOGGER.info(
          "Customer" + gson.toJson(customer1) + " and " + gson.toJson(customer2) + "innitialized");

      Item item1 = new Item();
      item1.i_id = 1;
      item1.i_im_id = 123;
      item1.i_name = "Cup";
      item1.i_price = 99.50;
      item1.i_data = "ORIGINAL";

      ctx.registry.create(ctx, item1);

      Item item2 = new Item();
      item2.i_id = 2;
      item2.i_im_id = 234;
      item2.i_name = "Plate";
      item2.i_price = 89.50;
      item2.i_data = "ORIGINAL";

      ctx.registry.create(ctx, item2);

      Item item3 = new Item();
      item3.i_id = 3;
      item3.i_im_id = 456;
      item3.i_name = "Glass";
      item3.i_price = 78.00;
      item3.i_data = "GENERIC";

      ctx.registry.create(ctx, item3);

      LOGGER.info(
          "Created Items "
              + gson.toJson(item1)
              + " , "
              + gson.toJson(item2)
              + " and "
              + gson.toJson(item3));

      Stock stock =
          new Stock(
              1,
              1,
              100,
              "good",
              "null",
              "null",
              "null",
              "null",
              "null",
              "null",
              "null",
              "null",
              "null",
              0,
              0,
              0,
              "ORIGINAL");

      ctx.registry.create(ctx, stock);

      Stock stock2 =
          new Stock(
              2,
              1,
              90,
              "good",
              "null",
              "null",
              "null",
              "null",
              "null",
              "null",
              "null",
              "null",
              "null",
              0,
              0,
              0,
              "ORIGINAL");

      ctx.registry.create(ctx, stock2);

      Stock stock3 =
          new Stock(
              3, 1, 99, "good", "null", "null", "null", "null", "null", "null", "null", "null",
              "null", 0, 0, 0, "GENERIC");

      ctx.registry.create(ctx, stock3);

      LOGGER.info(
          "INNITIALIZED STOCK ENTRIES"
              + gson.toJson(stock)
              + " , "
              + gson.toJson(stock2)
              + " and "
              + gson.toJson(stock3));

      LOGGER.info("ENTRIES INNITIALIZED");

    } catch (Exception e) {
      LOGGER.info("Problem occured while innitializing entries, caused by  " + e);
      e.printStackTrace();
    }
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String readWarehouseEntry(EnhancedContext ctx, int w_id) throws Exception {
    LOGGER.info("Attemp to retrieve warehouse details  for " + w_id);
    Warehouse wh = new Warehouse();
    wh.w_id = w_id;
    Warehouse warehouse = ctx.registry.read(ctx, wh);
    LOGGER.info("Warehouse" + gson.toJson(warehouse) + " returned");
    return gson.toJson(warehouse);
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String getOrderEntry(EnhancedContext ctx, int w_id, int d_id, int o_id) throws Exception {
    LOGGER.info("retrieve details  for existing order entry" + w_id);
    Order od = new Order();
    od.o_w_id = w_id;
    od.o_d_id = d_id;
    od.o_id = o_id;
    Order order = ctx.registry.read(ctx, od);
    LOGGER.info("Order " + gson.toJson(order) + " returned");
    return gson.toJson(order);
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String getItemEntry(EnhancedContext ctx, int i_id) throws Exception {
    LOGGER.info("retrieve details  for existing item entry " + i_id);
    Item itm = new Item();
    itm.i_id = i_id;
    Item item = ctx.registry.read(ctx, itm);
    LOGGER.info("Item " + gson.toJson(item) + " returned");
    return gson.toJson(item);
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String getNewOrderEntry(EnhancedContext ctx, int w_id, int d_id, int o_id)
      throws Exception {
    LOGGER.info(
        "Attemp to retrieve oldest new order details  for warehouse"
            + w_id
            + "and District "
            + d_id);
    NewOrder no = new NewOrder();
    no.no_w_id = w_id;
    no.no_d_id = d_id;
    no.no_o_id = o_id;

    NewOrder newOrder = ctx.registry.read(ctx, no);
    LOGGER.info("Retrieved new order  " + gson.toJson(newOrder));
    return gson.toJson(newOrder);
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String ping(EnhancedContext ctx) {
    LOGGER.info("Received ping");
    return "pong";
  }

  /*
   * This is just a dummy OpenJML test.  Should only allow getting the
   * details of customer #1, but not customer #2 created by initEntries.
   */
  // spotless:off
  //@ requires c_id < 2;
  // spotless:on
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String OJMTEST__getCustomer(EnhancedContext ctx, int c_w_id, int c_d_id, int c_id)
      throws Exception {
    Customer customer = new Customer();
    customer.c_w_id = c_w_id;
    customer.c_d_id = c_d_id;
    customer.c_id = c_id;
    return gson.toJson(ctx.registry.read(ctx, customer));
  }
}
