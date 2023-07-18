/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc;

import hu.bme.mit.ftsrg.tpcc.entries.*;
import hu.bme.mit.ftsrg.tpcc.entries.extra.*;
import hu.bme.mit.ftsrg.tpcc.inputs.*;
import hu.bme.mit.ftsrg.tpcc.outputs.*;
import hu.bme.mit.ftsrg.tpcc.utils.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.hyperledger.fabric.contract.Context;
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
public final class TPCC implements ContractInterface {

  private static final Logger LOGGER = Logger.getLogger(TPCC.class.getName());

  /**
   * Performs the Delivery read-write TX profile.
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String doDelivery(final Context ctx, final String parameters) {
    // addTxInfo(ctx);

    // TPC-C 2.7.4.2
    LOGGER.info("Starting Delivery TX with parameters" + parameters);
    try {
      final DoDeliveryInputParameters params = ParseUtils.parseDeliveryParameters(parameters);
      // For a given warehouse number (W_ID), for each of the districts (D_W_ID, D_ID)
      // within that warehouse, and for a given carrier number (O_CARRIER_ID):
      final List<DeliveredOrder> deliveredOrders = new ArrayList<>();
      int skipped = 0;
      for (int d_id = 1; d_id <= 10; d_id++) {
        LOGGER.info("Begin for loop to retrieve oldest new orders from the various districts");

        // The row in the NEW-ORDER table with matching NO_W_ID (equals W_ID) and
        // NO_D_ID (equals D_ID) and with the lowest NO_O_ID value is selected.
        // This is the oldest undelivered order of that district.
        // NO_O_ID, the order number, is retrieved.
        final NewOrder newOrder = LedgerUtils.getOldestNewOrder(ctx, params.getW_id(), d_id);
        LOGGER.info("Oldest new order retrieved is: " + JSON.serialize(newOrder));

        if (newOrder == null) {
          // If no matching row is found, then the delivery of an order for this district
          // is skipped. The condition in which no outstanding order is present at a given
          // district must be handled by skipping the delivery of an order for that
          // district only and resuming the delivery of an order from all remaining districts of
          // the selected warehouse. If this condition occurs in more than 1%, or in more than
          // one, whichever is greater, of the business transactions, it must be reported.
          // common.log("Could not find new order for District( " + params.w_id + "," +
          // d_id + " ) skipping it",
          // ctx, "info");
          LOGGER.info(
              "Could not find new order for District( "
                  + params.getW_id()
                  + ","
                  + d_id
                  + " ) skipping it");
          skipped++;
          continue;
        }

        LedgerUtils.deleteNewOrder(ctx, newOrder);
        LOGGER.info(JSON.serialize(newOrder) + "DELETED");

        // The row in the ORDER table with matching O_W_ID (equals W_ ID), O_D_ID
        // (equals D_ID),
        // and O_ID (equals NO_O_ID) is selected, O_C_ID, the customer number, is
        // retrieved,
        // and O_CARRIER_ID is updated.
        final Order order =
            LedgerUtils.getOrder(ctx, params.getW_id(), d_id, newOrder.getNo_o_id());
        LOGGER.info(
            "retrieved order details with no_o_id: "
                + newOrder.getNo_o_id()
                + " and Warehouse and District IDs "
                + params.getW_id());
        order.setO_carrier_id(params.getO_carrier_id());
        LedgerUtils.updateOrder(ctx, order);
        LOGGER.info(
            "Updated order"
                + JSON.serialize(order)
                + "with order.o_carrier_id"
                + params.getO_carrier_id());

        // All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID),
        // OL_D_ID
        // (equals O_D_ID), and OL_O_ID (equals O_ID) are selected. All OL_DELIVERY_D,
        // the
        // delivery dates, are updated to the current system time as returned by the
        // operating
        // system and the sum of all OL_AMOUNT is retrieved.
        double orderLineAmountTotal = 0;
        // ctx.getTxinfo().md_tpcc_delivery_total_order_lines += order.o_ol_cnt;
        for (int i = 1; i <= order.getO_ol_cnt(); i++) {
          LOGGER.info("getOrderLine");
          final OrderLine orderLine =
              LedgerUtils.getOrderLine(ctx, params.getW_id(), d_id, order.getO_id(), i);
          LOGGER.info("OrderLine: " + JSON.serialize(orderLine) + "retrieved");
          orderLineAmountTotal += orderLine.getOl_amount();
          orderLine.setOl_delivery_d(params.getOl_delivery_d());
          LOGGER.info(
              "updateOrderLine with orderLineAmountTotal "
                  + orderLineAmountTotal
                  + "and ol_delivery_d "
                  + orderLine.getOl_delivery_d());
          LedgerUtils.updateOrderLine(ctx, orderLine);
        }

        // The row in the CUSTOMER table with matching C_W_ID (equals W_ID), C_D_ID
        // (equals D_ID), and C_ID (equals O_C_ID) is selected and C_BALANCE is
        // increased by the sum of all order-line amounts (OL_AMOUNT) previously
        // retrieved.
        // C_DELIVERY_CNT is incremented by 1.
        LOGGER.info(
            "getCustomer with W_ID, D_ID and C_ID"
                + params.getW_id()
                + ","
                + d_id
                + ","
                + order.getO_c_id());
        final Customer customer =
            LedgerUtils.getCustomer(ctx, params.getW_id(), d_id, order.getO_c_id());
        LOGGER.info("Customer: " + JSON.serialize(customer) + "retrieved");
        customer.setC_balance(customer.getC_balance() + orderLineAmountTotal);
        customer.setC_delivery_cnt(customer.getC_delivery_cnt() + 1);
        LOGGER.info(
            "updateCustomer. C_BALANCE is increased by the sum of all order-line amounts (OL_AMOUNT)"
                + "previously retrieved and C_DELIVERY_CNT is incremented by 1 ");
        LedgerUtils.updateCustomer(ctx, customer);

        deliveredOrders.add(new DeliveredOrder(d_id, order.getO_id()));
      }

      final DoDeliveryOutput output =
          new DoDeliveryOutput(
              params.getW_id(), params.getO_carrier_id(), deliveredOrders, skipped);

      LOGGER.info("Finished Delivery TX with output: " + JSON.serialize(output));
      // System.out.println("Output : " + output);
      return JSON.serialize(output);
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
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String doNewOrder(final Context ctx, final String parameters) {
    // addTxInfo(ctx);
    // TPC-C 2.4.2.2
    // common.log("Starting NewOrder TX with parameters" + parameters, ctx, "info");
    LOGGER.info("Starting NewOrder TX with parameters" + parameters);

    try {
      final DoNewOrderInputParameters params = ParseUtils.parseNewOrderParameters(parameters);

      // The row in the WAREHOUSE table with matching W_ID is selected and W_TAX,
      // the warehouse tax rate, is retrieved.
      final Warehouse warehouse = LedgerUtils.getWarehouse(ctx, params.getW_id());
      LOGGER.info("Warehouse " + JSON.serialize(warehouse) + "retrieved");

      // The row in the DISTRICT table with matching D_W_ID and D_ ID is selected,
      // D_TAX, the district tax rate, is retrieved, and D_NEXT_O_ID, the next
      // available order number for the district, is retrieved and incremented by one.
      final District district = LedgerUtils.getDistrict(ctx, warehouse.getW_id(), params.getD_id());
      LOGGER.info("District " + JSON.serialize(district) + "retrieved");
      final int nextOrderId = district.getD_next_o_id();
      district.setD_next_o_id(nextOrderId + 1);
      LedgerUtils.updateDistrict(ctx, district);
      LOGGER.info(
          "Next available order number for District "
              + params.getD_id()
              + " incremented. New District values are: "
              + JSON.serialize(district));

      // The row in the CUSTOMER table with matching C_W_ID, C_D_ID, and C_ID is
      // selected and C_DISCOUNT, the customer's discount rate, C_LAST, the customer's
      // last name, and C_CREDIT, the customer's credit status, are retrieved.
      final Customer customer =
          LedgerUtils.getCustomer(ctx, warehouse.getW_id(), district.getD_id(), params.getC_id());
      LOGGER.info(
          "Customer "
              + params.getC_id()
              + " with w_id "
              + warehouse.getW_id()
              + " and d_id "
              + district.getD_id()
              + " retrieved");
      LOGGER.info(JSON.serialize(customer));

      // A new row is inserted into both the NEW-ORDER table and the ORDER table to
      // reflect the creation of the new order. O_CARRIER_ID is set to a null value.
      // If the order includes only home order-lines, then O_ALL_LOCAL is set to 1,
      // otherwise O_ALL_LOCAL is set to 0.
      final NewOrder newOrder =
          NewOrder.builder()
              .o_id(nextOrderId)
              .d_id(district.getD_id())
              .w_id(warehouse.getW_id())
              .build();

      final String jsonNewOrder = JSON.serialize(newOrder);
      LedgerUtils.createNewOrder(ctx, jsonNewOrder);
      LOGGER.info("New Order " + JSON.serialize(newOrder) + "created");

      boolean allItemsLocal = true;
      for (int id : params.getI_w_ids()) {
        if (id != warehouse.getW_id()) {
          allItemsLocal = false;
          break;
        }
      }

      final Order order =
          Order.builder()
              .id(nextOrderId)
              .d_id(district.getD_id())
              .w_id(warehouse.getW_id())
              .c_id(customer.getC_id())
              .entry_d(params.getO_entry_d())
              .carrier_id(0)
              .ol_cnt(params.getI_ids().length)
              .all_local(allItemsLocal ? 1 : 0)
              .build();

      final String jsonOrder = JSON.serialize(order);
      LedgerUtils.createOrder(ctx, jsonOrder);
      LOGGER.info("Created Order " + JSON.serialize(order));

      final List<ItemsData> itemsData = new ArrayList<>();

      double totalOrderLineAmount = 0;
      // For each O_OL_CNT item on the order
      for (int i = 0; i < params.getI_ids().length; i++) {
        final int i_id = params.getI_ids()[i];
        final int i_w_id = params.getI_w_ids()[i];
        final int i_qty = params.getI_qtys()[i];

        // The row in the ITEM table with matching I_ID (equals OL_I_ID) is selected
        // and I_PRICE, the price of the item, I_NAME, the name of the item, and
        // I_DATA are retrieved. If I_ID has an unused value (see Clause 2.4.1.5), a
        // "not-found" condition is signaled, resulting in a rollback of the
        // database transaction (see Clause 2.4.2.3).
        final Item item = LedgerUtils.getItem(ctx, i_id);
        LOGGER.info("Retrieved item " + JSON.serialize(item) + " with item id " + i_id);
        if (item == null) {
          // 2.4.3.4 For transactions that are rolled back as a result of an
          // unused item number (1% of all New-Order transactions), the emulated
          // terminal must display in the appropriate fields of the input/output
          // screen the fields: W_ID, D_ID, C_ID, C_LAST, C_CREDIT, O_ID, and the
          // execution status message "Item number is not valid".
          final Map<String, Object> errorOutput = new HashMap<>();
          errorOutput.put("w_id", warehouse.getW_id());
          errorOutput.put("d_id", district.getD_id());
          errorOutput.put("c_id", customer.getC_id());
          errorOutput.put("c_last", customer.getC_last());
          errorOutput.put("c_credit", customer.getC_credit());
          errorOutput.put("o_id", order.getO_id());
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
        final Stock stock = LedgerUtils.getStock(ctx, i_w_id, i_id);
        LOGGER.info("Stock " + JSON.serialize(stock) + " retrieved");

        if (stock.getS_quantity() >= i_qty + 10) {
          stock.setS_quantity(stock.getS_quantity() - i_qty);
        } else {
          stock.setS_quantity(stock.getS_quantity() - i_qty + 91);
        }

        stock.setS_ytd(stock.getS_ytd() + i_qty);
        stock.setS_order_cnt(stock.getS_order_cnt() + 1);

        if (i_w_id != warehouse.getW_id()) {
          stock.setS_remote_cnt(stock.getS_remote_cnt() + 1);
        }

        LedgerUtils.updateStock(ctx, stock);
        LOGGER.info("Updated stock with " + JSON.serialize(stock));

        // The amount for the item in the order (OL_AMOUNT) is computed as:
        // OL_QUANTITY * I_PRICE
        final double orderLineAmount = i_qty * item.getI_price();
        totalOrderLineAmount += orderLineAmount;

        // The strings in I_DATA and S_DATA are examined. If they both include the
        // string "ORIGINAL", the brand-generic field for that item is set to "B",
        // otherwise, the brand-generic field is set to "G".
        String brandGeneric;
        if (item.getI_data().contains("ORIGINAL") && stock.getS_data().contains("ORIGINAL")) {
          brandGeneric = "B";
        } else {
          brandGeneric = "G";
        }

        // A new row is inserted into the ORDER-LINE table to reflect the item on
        // the order. OL_DELIVERY_D is set to a null value, OL_NUMBER is set to a
        // unique value within all the ORDER-LINE rows that have the same OL_O_ID
        // value, and OL_DIST_INFO is set to the content of S_DIST_xx, where xx
        // represents the district number (OL_D_ID)
        final String stockDistrictId = String.format("%02d", district.getD_id());

        final OrderLine orderLine =
            OrderLine.builder()
                .o_id(nextOrderId)
                .d_id(district.getD_id())
                .w_id(warehouse.getW_id())
                .number(i + 1)
                .i_id(i_id)
                .supply_w_id(i_w_id)
                .delivery_d(null)
                .quantity(i_qty)
                .amount(orderLineAmount)
                .dist_info("s_dist_" + stockDistrictId)
                .build();

        final String jsonOL = JSON.serialize(orderLine);
        LedgerUtils.createOrderLine(ctx, jsonOL);
        LOGGER.info("OrderLine " + JSON.serialize(orderLine) + " created");

        // 2.4.3.3 The emulated terminal must display, in the appropriate fields of
        // the input/ output screen, all input data and the output data resulting
        // from the execution of the transaction. The display field s are divided in
        // two groups as follows:
        // - One repeating group of fields: OL_SUPPLY_W_ID, OL_I_ID, I_NAME,
        // OL_QUANTITY, S_QUANTITY, brand_generic, I_PRICE, and OL_AMOUNT. The group
        // is repeated O_OL_CNT times (once per item in the order), equal to the
        // computed value of ol_cnt.
        itemsData.add(
            ItemsData.builder()
                .ol_supply_w_id(orderLine.getOl_supply_w_id())
                .ol_i_id(orderLine.getOl_i_id())
                .i_name(item.getI_name())
                .ol_quantity(orderLine.getOl_quantity())
                .s_quantity(stock.getS_quantity())
                .brand_generic(brandGeneric)
                .i_price(item.getI_price())
                .ol_amount(orderLine.getOl_amount())
                .build());
        LOGGER.info("ItemsData" + JSON.serialize(itemsData));
      }

      // The total-amount for the complete order is computed as:
      // sum(OL_AMOUNT) * (1 - C_DISCOUNT) * (1 + W_TAX + D_TAX)
      final double totalAmount =
          totalOrderLineAmount
              * (1 - customer.getC_discount())
              * (1 + warehouse.getW_tax() + district.getD_tax());
      LOGGER.info("total amount = " + totalAmount);
      System.out.println(totalAmount);

      // 2.4.3.3 The emulated terminal must display, in the appropriate fields of
      // the input/ output screen, all input data and the output data resulting
      // from the execution of the transaction. The display field s are divided in
      // two groups as follows:
      // - One non-repeating group of fields: W_ID, D_ID, C_ID, O_ID, O_OL_CNT,
      // C_LAST, C_CREDIT, C_DISCOUNT, W_TAX, D_TAX, O_ENTRY_D, total_amount, and an
      // optional execution status message other than "Item number is not valid".
      final DoNewOrderOutput output =
          DoNewOrderOutput.builder()
              .w_id(warehouse.getW_id())
              .d_id(district.getD_id())
              .c_id(customer.getC_id())
              .c_last(customer.getC_last())
              .c_credit(customer.getC_credit())
              .c_discount(customer.getC_discount())
              .w_tax(warehouse.getW_tax())
              .d_tax(district.getD_tax())
              .o_ol_cnt(order.getO_ol_cnt())
              .o_id(order.getO_id())
              .o_entry_d(order.getO_entry_d())
              .total_amount(totalAmount)
              .items(itemsData)
              .build();

      LOGGER.info("Finished New Order TX with output" + JSON.serialize(output));
      // System.out.println("console output print" + JSON.serialize(output));
      // System.out.println("THIS IS THE OUTPUT" + output);

      return JSON.serialize(output);
    } catch (Exception err) {
      LOGGER.info("ERROR" + err.toString() + "occured");
      // throw err;
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
  public String doOrderStatus(final Context ctx, final String parameters) {
    // TPC-C 2.6.2.2
    // log(`Starting Order Status TX with parameters: ${parameters}`, ctx, 'info');
    try {
      final DoOrderStatusInputParameters params = ParseUtils.parseOrderStatusParameters(parameters);

      final Customer customer =
          LedgerUtils.getCustomersByIdOrLastName(
              ctx, params.getW_id(), params.getD_id(), params.getC_id(), params.getC_last());
      LOGGER.info(
          "getCustomersByIdOrLastName returned Customer: "
              + customer.getC_first()
              + customer.getC_last()
              + " with ID: "
              + customer.getC_id());

      // The row in the ORDER table with matching O_W_ID (equals C_W_ID), O_D_ID
      // (equals C_D_ID),
      // O_C_ID (equals C_ID), and with the largest existing O_ID, is selected. This
      // is the most
      // recent order placed by that customer. O_ID, O_ENTRY_D, and O_CARRIER_ID are
      // retrieved.
      final Order order =
          LedgerUtils.getLastOrderOfCustomer(
              ctx, customer.getC_w_id(), customer.getC_d_id(), customer.getC_id());
      LOGGER.info("getLastOrderOfCustomer returned: " + JSON.serialize(order));

      // All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID),
      // OL_D_ID (equals O_D_ID), and OL_O_ID (equals O_ID) are selected and the corresponding sets
      // of
      // OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, and OL_DELIVERY_D are retrieved.
      // ctx.txinfo.md_tpcc_order_status_order_lines = order.o_ol_cnt;
      final List<OrderLineData> orderLineData = new ArrayList<>();

      for (int i = 1; i <= order.getO_ol_cnt(); i++) {
        final OrderLine orderLine =
            LedgerUtils.getOrderLine(ctx, order.getO_w_id(), order.getO_d_id(), order.getO_id(), i);
        LOGGER.info("getOrderLine with order.o_ids: " + order.getO_id() + "retrieved");

        OrderLineData olData =
            OrderLineData.builder()
                .ol_supply_w_id(orderLine.getOl_supply_w_id())
                .ol_i_id(orderLine.getOl_i_id())
                .ol_quantity(orderLine.getOl_quantity())
                .ol_amount(orderLine.getOl_amount())
                .ol_delivery_d(orderLine.getOl_delivery_d())
                .build();

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
      final DoOrderStatusOutput output =
          DoOrderStatusOutput.builder()
              .w_id(params.getW_id())
              .d_id(params.getD_id())
              .c_id(customer.getC_id())
              .c_first(customer.getC_first())
              .c_middle(customer.getC_middle())
              .c_last(customer.getC_last())
              .c_balance(customer.getC_balance())
              .o_id(order.getO_id())
              .o_entry_d(order.getO_entry_d())
              .o_carrier_id(order.getO_carrier_id())
              .order_lines(orderLineData)
              .build();

      LOGGER.info("Finished Order Status TX with output: " + JSON.serialize(output));
      return JSON.serialize(output);
    } catch (Exception err) {
      Common.log(err.toString(), ctx, "error");
      // throw err;
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
  public String doPayment(final Context ctx, final String parameters) {
    // addTxInfo(ctx);
    // TPC-C 2.5.2.2
    LOGGER.info("Starting Payment TX with parameters: " + parameters);
    try {
      final DoPaymentInputParameters params = ParseUtils.parsePaymentParameters(parameters);

      // The row in the WAREHOUSE table with matching W_ID is selected. W_NAME,
      // W_STREET_1, W_STREET_2, W_CITY, W_STATE, and W_ZIP are retrieved and W_YTD,
      // the
      // warehouse's year-to-date balance, is increased by H_ AMOUNT.
      final Warehouse warehouse = LedgerUtils.getWarehouse(ctx, params.getW_id());
      LOGGER.info("Parameters retrieved for warehouse " + params.getW_id());
      warehouse.setW_ytd(warehouse.getW_ytd() + params.getH_amount());
      LedgerUtils.updateWarehouse(ctx, warehouse);
      LOGGER.info(
          "Updated warehouse's year-to-date balance  with new value: " + warehouse.getW_ytd());

      // The row in the DISTRICT table with matching D_W_ID and D_ID is selected.
      // D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE, and D_ZIP are retrieved and
      // D_YTD, the district's year-to-date balance, is increased by H_AMOUNT.
      final District district = LedgerUtils.getDistrict(ctx, warehouse.getW_id(), params.getD_id());
      LOGGER.info("get parameters for  district " + params.getD_id());
      district.setD_ytd(district.getD_ytd() + params.getH_amount());
      LedgerUtils.updateDistrict(ctx, district);
      LOGGER.info("Update district's year-to-date balance with value: " + district.getD_ytd());

      final Customer customer =
          LedgerUtils.getCustomersByIdOrLastName(
              ctx, warehouse.getW_id(), district.getD_id(), params.getC_id(), params.getC_last());
      LOGGER.info(
          "Customer " + JSON.serialize(customer) + "retrieved by last name " + params.getC_last());

      // C_BALANCE is decreased by H_AMOUNT. C_YTD_PAYMENT is increased by H_AMOUNT.
      // C_PAYMENT_CNT is incremented by 1.
      customer.setC_balance(customer.getC_balance() - params.getH_amount());
      customer.setC_ytd_payment(customer.getC_ytd_payment() + params.getH_amount());
      customer.setC_payment_cnt(customer.getC_payment_cnt() + 1);

      // If the value of C_CREDIT is equal to "BC", then C_DATA is also retrieved from
      // the selected customer and the following history information: C_ID, C_D_ID,
      // C_W_ID, D_ID, W_ID, and H_AMOUNT, are inserted at the left of the C_DATA field
      // by shifting the existing content of C_DATA to the right by an equal number of
      // bytes and by discarding the bytes that are shifted out of the right side of the
      // C_DATA field. The content of the C_DATA field never exceeds 500 characters. The
      // selected customer is updated with the new C_DATA field.
      if (customer.getC_credit().equals("BC")) {
        final String history =
            customer.getC_id()
                + " "
                + customer.getC_d_id()
                + " "
                + customer.getC_w_id()
                + " "
                + district.getD_id()
                + " "
                + warehouse.getW_id()
                + " "
                + params.getH_amount();
        customer.setC_data(history + "|" + customer.getC_data());
        LOGGER.info(
            "history information: "
                + customer.getC_id()
                + ", "
                + customer.getC_d_id()
                + ", "
                + customer.getC_w_id()
                + ", "
                + district.getD_id()
                + ", "
                + warehouse.getW_id()
                + "and "
                + params.getH_amount()
                + "inserted at the left of the C_DATA");

        if (customer.getC_data().length() > 500)
          customer.setC_data(customer.getC_data().substring(0, 500));
      }

      LedgerUtils.updateCustomer(ctx, customer);
      LOGGER.info("customer updated with the new C_DATA field.");

      // H_DATA is built by concatenating W_NAME and D_NAME separated by 4 spaces.
      final String h_data = warehouse.getW_name() + "    " + district.getD_name();

      // A new row is inserted into the HISTORY table with H_C_ID = C_ID,
      // H_C_D_ID = C_D_ID, H_C_W_ID = C_W_ID, H_D_ID = D_ID, and H_W_ID = W_ID.
      final History history =
          History.builder()
              .c_id(customer.getC_id())
              .c_d_id(customer.getC_d_id())
              .c_w_id(customer.getC_w_id())
              .d_id(district.getD_id())
              .w_id(warehouse.getW_id())
              .date(params.getH_date())
              .amount(params.getH_amount())
              .data(h_data)
              .build();

      final String historyData = JSON.serialize(history);
      LedgerUtils.createHistory(ctx, historyData);
      LOGGER.info("History created with data " + historyData);

      // 2.5.3.3 The emulated terminal must display, in the appropriate fields of the
      // input/output screen, all input data and the output data resulting from the
      // execution of the transaction. The following fields are displayed: W_ID, D_ID,
      // C_ID, C_D_ID, C_W_ID, W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP,
      // D_STREET_1, D_STREET_2, D_CITY, D_STATE, D_ZIP, C_FIRST, C_MIDDLE, C_LAST,
      // C_STREET_1, C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT,
      // C_CREDIT_LIM, C_DISCOUNT, C_BALANCE, the first 200 characters of C_DATA (only
      // if C_CREDIT = "BC"), H_AMOUNT, and H_DATE.
      final DoPaymentOutput output =
          DoPaymentOutput.builder()
              .w_id(warehouse.getW_id())
              .d_id(district.getD_id())
              .c_id(customer.getC_id())
              .c_d_id(customer.getC_d_id())
              .c_w_id(customer.getC_w_id())
              .h_amount(history.getH_amount())
              .h_date(history.getH_date())
              .w_street_1(warehouse.getW_street_1())
              .w_street_2(warehouse.getW_street_2())
              .w_city(warehouse.getW_city())
              .w_state(warehouse.getW_state())
              .w_zip(warehouse.getW_zip())
              .d_street_1(district.getD_street_1())
              .d_street_2(district.getD_street_2())
              .d_city(district.getD_city())
              .d_state(district.getD_state())
              .d_zip(district.getD_zip())
              .c_first(customer.getC_first())
              .c_middle(customer.getC_middle())
              .c_last(customer.getC_last())
              .c_street_1(customer.getC_street_1())
              .c_street_2(customer.getC_street_2())
              .c_city(customer.getC_city())
              .c_state(customer.getC_state())
              .c_zip(customer.getC_zip())
              .c_phone(customer.getC_phone())
              .c_since(customer.getC_since())
              .c_credit(customer.getC_credit())
              .c_credit_lim(customer.getC_credit_lim())
              .c_discount(customer.getC_discount())
              .c_balance(customer.getC_balance())
              .build();

      if (customer.getC_credit().equals("BC")) {
        output.setC_data(customer.getC_data().substring(0, 200));
      }

      LOGGER.info("Finished Payment TX with output: " + JSON.serialize(output));

      return JSON.serialize(output);
    } catch (Exception err) {
      Common.log(err.toString(), ctx, "error");
      // throw err;
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
  public String doStockLevel(final Context ctx, final String parameters) {
    // addTxInfo(ctx);
    // TPC-C 2.8.2.2
    LOGGER.info("Starting Stock Level TX with parameters: " + parameters);
    try {
      final DoStockLevelInputParameters params = ParseUtils.parseStockLevelParameters(parameters);

      // The row in the DISTRICT table with matching D_W_ID and D_ID is selected and
      // D_NEXT_O_ID is retrieved.
      final District district = LedgerUtils.getDistrict(ctx, params.getW_id(), params.getD_id());
      LOGGER.info("District " + district.getD_id() + " RETRIEVED");

      // All rows in the ORDER-LINE table with matching OL_W_ID (equals W_ID), OL_D_ID
      // (equals D_ID), and OL_O_ID (lower than D_NEXT_O_ID and greater than or equal to
      // D_NEXT_O_ID minus 5) are selected. They are the items for 5 recent orders of the
      // district.
      final int o_id_min = district.getD_next_o_id() - 5;
      final int o_id_max = district.getD_next_o_id();
      LOGGER.info("o_id_min = " + o_id_min + " and o_id_max = " + o_id_max);
      LOGGER.info("get recent 5 orders");
      final List<Integer> recentItemIds =
          LedgerUtils.getItemIdsOfRecentOrders(
              ctx, params.getW_id(), district.getD_id(), o_id_min, o_id_max);
      LOGGER.info("getItemIdsOfRecentOrders returned " + recentItemIds);
      // ctx.txinfo.md_tpcc_stock_level_recent_items = recentItemIds.length;

      // All rows in the STOCK table with matching S_I_ID (equals OL_I_ID) and S_W_ID (equals W_ID)
      // from the list of distinct item numbers and with S_QUANTITY lower than threshold are counted
      // (giving low_stock).
      int lowStock = 0;
      for (final int i_id : recentItemIds) {
        final Stock stock = LedgerUtils.getStock(ctx, params.getW_id(), i_id);
        LOGGER.info("get stock for items> " + i_id);
        if (stock.getS_quantity() < params.getThreshold()) {
          LOGGER.info("The stock quantity is less than the threshold");
          lowStock++;
        }
        LOGGER.info("LowStock = " + lowStock);
      }

      // 2.8.3.3 The emulated terminal must display, in the appropriate field of the
      // input/output screen, all input data and the output data which results from the
      // execution of the transaction. The following fields are displayed: W_ID, D_ID, threshold,
      // and low_stock.

      final DoStockLevelOutput output =
          DoStockLevelOutput.builder()
              .w_id(params.getW_id())
              .d_id(params.getD_id())
              .threshold(params.getThreshold())
              .low_stock(lowStock)
              .build();

      LOGGER.info("Finished Stock Level TX with output: " + JSON.serialize(output));

      return JSON.serialize(output);

    } catch (Exception err) {
      Common.log(err.toString(), ctx, "error");
      // throw err;
    }
    return null;
  }

  /**
   * Initializes the TPC-C chaincode.
   *
   * @param ctx The TX context.
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public void instantiate(final Context ctx) {
    Common.log("Instantiating TPC-C chaincode", ctx, "info");
    LOGGER.info("Instantiating TPC-C chaincode");
  }

  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void initEntries(final Context ctx) {
    LOGGER.info("Starting initEntries");
    try {
      final Warehouse warehouse =
          Warehouse.builder()
              .id(1)
              .name("W_One")
              .street_1("xyz")
              .street_2("123")
              .city("Budapest")
              .state("LA")
              .zip("00011111")
              .tax(0.1000)
              .ytd(10000)
              .build();
      final String jsonWarehouse = JSON.serialize(warehouse);
      LedgerUtils.createWarehouse(ctx, jsonWarehouse);
      LOGGER.info("Warehouse " + jsonWarehouse + " initialized");

      final District district =
          District.builder()
              .id(1)
              .w_id(1)
              .name("D_One")
              .street_1("abc")
              .street_2("456")
              .city("Budapest")
              .state("BP")
              .zip("00111111")
              .tax(0.0100)
              .ytd(10000)
              .next_o_id(3001)
              .build();
      final String jsonDistrict = JSON.serialize(district);
      LedgerUtils.createDistrict(ctx, jsonDistrict);
      LOGGER.info("District " + jsonDistrict + " initialized");

      final Customer customer1 =
          Customer.builder()
              .id(1)
              .d_id(1)
              .w_id(1)
              .first("Alice")
              .middle("Is")
              .last("Yong")
              .street_1("xyz")
              .street_2("123")
              .city("Budapest")
              .state("Buda")
              .zip("00101111")
              .phone("123456789")
              .since("19/01/2020")
              .credit("GC")
              .credit_lim(50000)
              .discount(0.25)
              .balance(1000.00)
              .ytd_payment(10)
              .payment_cnt(1)
              .delivery_cnt(0)
              .data("Good credit")
              .build();
      final Customer customer2 =
          Customer.builder()
              .id(2)
              .d_id(1)
              .w_id(1)
              .first("Peter")
              .middle("Peet")
              .last("Peter")
              .street_1("ABC")
              .street_2("23")
              .city("Budapest")
              .state("DC")
              .zip("00011111")
              .phone("456712389")
              .since("19/01/2020")
              .credit("GC")
              .credit_lim(50000)
              .discount(0.30)
              .balance(1000.00)
              .ytd_payment(10)
              .payment_cnt(1)
              .delivery_cnt(0)
              .data("Good credit")
              .build();
      final String jsonCustomer1 = JSON.serialize(customer1);
      final String jsonCustomer2 = JSON.serialize(customer2);
      LedgerUtils.createCustomer(ctx, jsonCustomer1);
      LedgerUtils.createCustomer(ctx, jsonCustomer2);
      LOGGER.info("Customers initialized: " + jsonCustomer1 + "," + jsonCustomer2);

      final Item item1 =
          Item.builder().id(1).im_id(123).name("Cup").price(99.50).data("ORIGINAL").build();
      final Item item2 =
          Item.builder().id(2).im_id(234).name("Plate").price(89.50).data("ORIGINAL").build();
      final Item item3 =
          Item.builder().id(3).im_id(456).name("Glass").price(78.00).data("GENERIC").build();
      final String jsonItem1 = JSON.serialize(item1);
      final String jsonItem2 = JSON.serialize(item2);
      final String jsonItem3 = JSON.serialize(item3);
      LedgerUtils.createItem(ctx, jsonItem1);
      LedgerUtils.createItem(ctx, jsonItem2);
      LedgerUtils.createItem(ctx, jsonItem3);
      LOGGER.info("Items initialized: " + jsonItem1 + "," + jsonItem2 + "," + jsonItem3);

      final Stock stock =
          Stock.builder()
              .i_id(1)
              .w_id(1)
              .quantity(100)
              .dist_all("null")
              .dist_01("good")
              .ytd(0)
              .order_cnt(0)
              .remote_cnt(0)
              .data("ORIGINAL")
              .build();
      final Stock stock2 =
          Stock.builder()
              .i_id(2)
              .w_id(1)
              .quantity(90)
              .dist_all("null")
              .dist_01("good")
              .ytd(0)
              .order_cnt(0)
              .remote_cnt(0)
              .data("ORIGINAL")
              .build();
      final Stock stock3 =
          Stock.builder()
              .i_id(3)
              .w_id(1)
              .quantity(99)
              .dist_all("null")
              .dist_01("good")
              .ytd(0)
              .order_cnt(0)
              .remote_cnt(0)
              .data("GENERIC")
              .build();
      final String jsonStock = JSON.serialize(stock);
      final String jsonStock2 = JSON.serialize(stock2);
      final String jsonStock3 = JSON.serialize(stock3);
      LedgerUtils.createStock(ctx, jsonStock);
      LedgerUtils.createStock(ctx, jsonStock2);
      LedgerUtils.createStock(ctx, jsonStock3);
      LOGGER.info("Stocks initialized: " + jsonStock + "," + jsonStock2 + "," + jsonStock3);

      LOGGER.info("ENTRIES INNITIALIZED");
    } catch (Exception e) {
      LOGGER.info("Problem occured while innitializing entries, caused by  " + e);
      e.printStackTrace();
    }
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String readWarehouseEntry(final Context ctx, final int w_id) throws Exception {
    LOGGER.info("Attemp to retrieve warehouse details  for " + w_id);
    final Warehouse warehouse = LedgerUtils.getWarehouse(ctx, w_id);
    LOGGER.info(
        "Warehouse " + warehouse.getW_id() + " Exist. " + JSON.serialize(warehouse) + " returned");
    return JSON.serialize(warehouse);
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String getOrderEntry(final Context ctx, final int w_id, final int d_id, final int o_id)
      throws Exception {
    LOGGER.info("retrieve details  for existing order entry" + w_id);
    final Order order = LedgerUtils.getOrder(ctx, w_id, d_id, o_id);
    LOGGER.info("Order " + order.getO_id() + " Exist. " + JSON.serialize(order) + " returned");
    return JSON.serialize(order);
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String getItemEntry(final Context ctx, final int i_id) throws Exception {
    LOGGER.info("retrieve details  for existing item entry " + i_id);
    final Item item = LedgerUtils.getItem(ctx, i_id);
    LOGGER.info("Item " + item.getI_id() + " Exist. " + JSON.serialize(item) + " returned");
    return JSON.serialize(item);
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String getNewOrderEntry(final Context ctx, final int w_id, final int d_id, final int o_id)
      throws Exception {
    LOGGER.info(
        "Attemp to retrieve oldest new order details  for warehouse"
            + w_id
            + "and District "
            + d_id);
    final NewOrder newOrder = LedgerUtils.getNewOrder(ctx, w_id, d_id, o_id);
    LOGGER.info("Retrieved new order  " + JSON.serialize(newOrder));
    return JSON.serialize(newOrder);
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String ping(final Context _ctx) {
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
  public String OJMLTEST__getCustomer(
      final Context ctx, final int c_w_id, final int c_d_id, final int c_id) throws Exception {
    return JSON.serialize(LedgerUtils.getCustomer(ctx, c_w_id, c_d_id, c_id));
  }
}
