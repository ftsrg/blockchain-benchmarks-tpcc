/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc;

import com.jcabi.aspects.Loggable;
import hu.bme.mit.ftsrg.chaincode.dataaccess.ContextWithRegistry;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.*;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.extra.DeliveredOrder;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.extra.ItemsData;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.extra.OrderLineData;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.input.*;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.output.*;
import hu.bme.mit.ftsrg.chaincode.tpcc.middleware.TPCCContext;
import hu.bme.mit.ftsrg.chaincode.tpcc.util.JSON;
import java.util.*;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(
    name = "TPCC",
    info =
        @Info(
            title = "tpcc contract",
            description = "My Smart Contract",
            version = "0.0.1",
            license = @License(name = "Apache-2.0"),
            contact =
                @Contact(email = "tnnopcc@example.com", name = "tpcc", url = "http://tpcc.me")))

/**
 * The implementation of the TPC-C benchmark smart contract according to the specification version
 * v5.11.0.
 */
@Default
@Loggable(Loggable.DEBUG)
public final class TPCC implements ContractInterface {

  private static final Logger logger = LoggerFactory.getLogger(TPCC.class);

  /**
   * Performs the Delivery read-write TX profile.
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String doDelivery(final TPCCContext ctx, final String parameters) {
    // TPC-C 2.7.4.2
    try {
      final DeliveryInput params = JSON.deserialize(parameters, DeliveryInput.class);
      // For a given warehouse number (W_ID), for each of the districts (D_W_ID, D_ID)
      // within that warehouse, and for a given carrier number (O_CARRIER_ID):
      final List<DeliveredOrder> deliveredOrders = new ArrayList<>();
      int skipped = 0;
      logger.debug("Begin for loop to retrieve oldest NEW-ORDERs from the various districts");
      for (int d_id = 1; d_id <= 10; d_id++) {

        // The row in the NEW-ORDER table with matching NO_W_ID (equals W_ID) and
        // NO_D_ID (equals D_ID) and with the lowest NO_O_ID value is selected.
        // This is the oldest undelivered order of that district.
        // NO_O_ID, the order number, is retrieved.
        final List<NewOrder> allNewOrders =
            ctx.registry.readAll(ctx, NewOrder.builder().w_id(params.getW_id()).d_id(d_id).build());
        if (allNewOrders.isEmpty()) {
          // If no matching row is found, then the delivery of an order for this district
          // is skipped. The condition in which no outstanding order is present at a given
          // district must be handled by skipping the delivery of an order for that
          // district only and resuming the delivery of an order from all remaining districts of
          // the selected warehouse. If this condition occurs in more than 1%, or in more than
          // one, whichever is greater, of the business transactions, it must be reported.
          logger.debug(
              "Could not find new order for District(%d, %d); skipping it"
                  .formatted(params.getW_id(), d_id));
          skipped++;
          continue;
        }
        final NewOrder oldestNewOrder =
            allNewOrders.stream().min(Comparator.comparingInt(NewOrder::getNo_o_id)).get();
        logger.debug("Oldest NEW-ORDER retrieved is: " + oldestNewOrder);
        ctx.registry.delete(ctx, oldestNewOrder);

        // The row in the ORDER table with matching O_W_ID (equals W_ ID), O_D_ID
        // (equals D_ID), and O_ID (equals NO_O_ID) is selected, O_C_ID, the customer number, is
        // retrieved, and O_CARRIER_ID is updated.
        final Order order =
            Order.builder()
                .w_id(params.getW_id())
                .d_id(d_id)
                .id(oldestNewOrder.getNo_o_id())
                .build();
        ctx.registry.read(ctx, order);

        order.setO_carrier_id(params.getO_carrier_id());
        ctx.registry.update(ctx, order);

        // All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID),
        // OL_D_ID
        // (equals O_D_ID), and OL_O_ID (equals O_ID) are selected. All OL_DELIVERY_D,
        // the
        // delivery dates, are updated to the current system time as returned by the
        // operating
        // system and the sum of all OL_AMOUNT is retrieved.
        double orderLineAmountTotal = 0;
        for (int i = 1; i <= order.getO_ol_cnt(); i++) {
          logger.debug("Getting ORDER-LINE #%d".formatted(i));
          final OrderLine orderLine =
              OrderLine.builder()
                  .w_id(params.getW_id())
                  .d_id(d_id)
                  .o_id(order.getO_id())
                  .number(i)
                  .build();
          ctx.registry.read(ctx, orderLine);

          orderLineAmountTotal += orderLine.getOl_amount();
          orderLine.setOl_delivery_d(params.getOl_delivery_d());
          ctx.registry.update(ctx, orderLine);
        }

        // The row in the CUSTOMER table with matching C_W_ID (equals W_ID), C_D_ID
        // (equals D_ID), and C_ID (equals O_C_ID) is selected and C_BALANCE is
        // increased by the sum of all order-line amounts (OL_AMOUNT) previously
        // retrieved.
        // C_DELIVERY_CNT is incremented by 1.
        final Customer customer =
            Customer.builder().w_id(params.getW_id()).d_id(d_id).id(order.getO_c_id()).build();
        ctx.registry.read(ctx, customer);

        customer.setC_balance(customer.getC_balance() + orderLineAmountTotal);
        customer.setC_delivery_cnt(customer.getC_delivery_cnt() + 1);
        ctx.registry.update(ctx, customer);

        deliveredOrders.add(new DeliveredOrder(d_id, order.getO_id()));
      }

      final DeliveryOutput output =
          new DeliveryOutput(params.getW_id(), params.getO_carrier_id(), deliveredOrders, skipped);

      return JSON.serialize(output);
    } catch (Exception err) {
      logger.error(err.toString());
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
  public String doNewOrder(final TPCCContext ctx, final String parameters) {
    // TPC-C 2.4.2.2
    try {
      final NewOrderInput params = JSON.deserialize(parameters, NewOrderInput.class);

      // The row in the WAREHOUSE table with matching W_ID is selected and W_TAX,
      // the warehouse tax rate, is retrieved.
      final Warehouse warehouse = Warehouse.builder().id(params.getW_id()).build();
      ctx.registry.read(ctx, warehouse);

      // The row in the DISTRICT table with matching D_W_ID and D_ ID is selected,
      // D_TAX, the district tax rate, is retrieved, and D_NEXT_O_ID, the next
      // available order number for the district, is retrieved and incremented by one.
      final District district =
          District.builder().w_id(warehouse.getW_id()).id(params.getD_id()).build();
      ctx.registry.read(ctx, district);

      final int nextOrderId = district.getD_next_o_id();
      district.setD_next_o_id(nextOrderId + 1);
      ctx.registry.update(ctx, district);
      logger.debug(
          "Next available order number for DISTRICT with D_ID=%d incremented; new DISTRICT: %s"
              .formatted(params.getD_id(), district));

      // The row in the CUSTOMER table with matching C_W_ID, C_D_ID, and C_ID is
      // selected and C_DISCOUNT, the customer's discount rate, C_LAST, the customer's
      // last name, and C_CREDIT, the customer's credit status, are retrieved.
      final Customer customer =
          Customer.builder()
              .w_id(warehouse.getW_id())
              .d_id(district.getD_id())
              .id(params.getC_id())
              .build();
      ctx.registry.read(ctx, customer);

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
      ctx.registry.create(ctx, newOrder);

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
      ctx.registry.create(ctx, order);

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
        final Item item = Item.builder().id(i_id).build();
        ctx.registry.read(ctx, item);

        // The row in the STOCK table with matching S_I_ID (equals OL_I_ID) and
        // S_W_ID (equals OL_SUPPLY_W_ID) is selected. S_QUANTITY, the quantity in
        // stock, S_DIST_xx, where xx represents the district number, and S_DATA are
        // retrieved. If the retrieved value for S_QUANTITY exceeds OL_QUANTITY by
        // 10 or more, then S_QUANTITY is decreased by OL_QUANTITY; otherwise
        // S_QUANTITY is updated to (S_QUANTITY - OL_QUANTITY)+91. S_YTD is
        // increased by OL_QUANTITY and S_ORDER_CNT is incremented by 1. If the
        // order-line is remote, then S_REMOTE_CNT is incremented by 1.
        final Stock stock = Stock.builder().w_id(i_w_id).i_id(i_id).build();
        ctx.registry.read(ctx, stock);

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

        ctx.registry.update(ctx, stock);

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
        ctx.registry.create(ctx, orderLine);

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
        logger.debug("Created ItemsData: " + itemsData);
      }

      // The total-amount for the complete order is computed as:
      // sum(OL_AMOUNT) * (1 - C_DISCOUNT) * (1 + W_TAX + D_TAX)
      final double totalAmount =
          totalOrderLineAmount
              * (1 - customer.getC_discount())
              * (1 + warehouse.getW_tax() + district.getD_tax());
      logger.debug("Total amount is " + totalAmount);

      // 2.4.3.3 The emulated terminal must display, in the appropriate fields of
      // the input/ output screen, all input data and the output data resulting
      // from the execution of the transaction. The display field s are divided in
      // two groups as follows:
      // - One non-repeating group of fields: W_ID, D_ID, C_ID, O_ID, O_OL_CNT,
      // C_LAST, C_CREDIT, C_DISCOUNT, W_TAX, D_TAX, O_ENTRY_D, total_amount, and an
      // optional execution status message other than "Item number is not valid".
      final NewOrderOutput output =
          NewOrderOutput.builder()
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

      return JSON.serialize(output);
    } catch (Exception err) {
      logger.error(err.toString());
    }

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
  public String doOrderStatus(final TPCCContext ctx, final String parameters) {
    // TPC-C 2.6.2.2
    try {
      final OrderStatusInput params = JSON.deserialize(parameters, OrderStatusInput.class);

      final Customer customer =
          getCustomersByIdOrLastName(
              ctx, params.getW_id(), params.getD_id(), params.getC_id(), params.getC_last());
      // The row in the ORDER table with matching O_W_ID (equals C_W_ID), O_D_ID
      // (equals C_D_ID),
      // O_C_ID (equals C_ID), and with the largest existing O_ID, is selected. This
      // is the most
      // recent order placed by that customer. O_ID, O_ENTRY_D, and O_CARRIER_ID are
      // retrieved.
      final Order order =
          getLastOrderOfCustomer(
              ctx, customer.getC_w_id(), customer.getC_d_id(), customer.getC_id());

      // All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID),
      // OL_D_ID (equals O_D_ID), and OL_O_ID (equals O_ID) are selected and the corresponding sets
      // of OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, and OL_DELIVERY_D are retrieved.
      // ctx.txinfo.md_tpcc_order_status_order_lines = order.o_ol_cnt;
      final List<OrderLineData> orderLineData = new ArrayList<>();

      for (int i = 1; i <= order.getO_ol_cnt(); i++) {
        final OrderLine orderLine =
            OrderLine.builder()
                .w_id(order.getO_w_id())
                .d_id(order.getO_d_id())
                .o_id(order.getO_id())
                .number(i)
                .build();
        ctx.registry.read(ctx, orderLine);

        OrderLineData olData =
            OrderLineData.builder()
                .ol_supply_w_id(orderLine.getOl_supply_w_id())
                .ol_i_id(orderLine.getOl_i_id())
                .ol_quantity(orderLine.getOl_quantity())
                .ol_amount(orderLine.getOl_amount())
                .ol_delivery_d(orderLine.getOl_delivery_d())
                .build();
        logger.debug("Created ORDER-LINE data: " + orderLineData);
        orderLineData.add(olData);
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
      final OrderStatusOutput output =
          OrderStatusOutput.builder()
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

      return JSON.serialize(output);
    } catch (Exception err) {
      logger.error(err.toString());
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
  public String doPayment(final TPCCContext ctx, final String parameters) {
    // TPC-C 2.5.2.2
    try {
      final PaymentInput params = JSON.deserialize(parameters, PaymentInput.class);

      // The row in the WAREHOUSE table with matching W_ID is selected. W_NAME,
      // W_STREET_1, W_STREET_2, W_CITY, W_STATE, and W_ZIP are retrieved and W_YTD,
      // the
      // warehouse's year-to-date balance, is increased by H_ AMOUNT.
      final Warehouse warehouse = Warehouse.builder().id(params.getW_id()).build();
      ctx.registry.read(ctx, warehouse);
      warehouse.setW_ytd(warehouse.getW_ytd() + params.getH_amount());
      ctx.registry.update(ctx, warehouse);

      // The row in the DISTRICT table with matching D_W_ID and D_ID is selected.
      // D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE, and D_ZIP are retrieved and
      // D_YTD, the district's year-to-date balance, is increased by H_AMOUNT.
      final District district =
          District.builder().w_id(warehouse.getW_id()).id(params.getD_id()).build();
      ctx.registry.read(ctx, district);

      district.setD_ytd(district.getD_ytd() + params.getH_amount());
      ctx.registry.update(ctx, district);

      final Customer customer =
          getCustomersByIdOrLastName(
              ctx, warehouse.getW_id(), district.getD_id(), params.getC_id(), params.getC_last());
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
            "%d %d %d %d %d %s"
                .formatted(
                    customer.getC_id(),
                    customer.getC_d_id(),
                    customer.getC_w_id(),
                    district.getD_id(),
                    warehouse.getW_id(),
                    params.getH_amount());
        customer.setC_data("%s|%s".formatted(history, customer.getC_data()));
        logger.debug(
            "HISTORY information: %d, %d, %d, %d, %d and %s inserted at the left of the C_DATA"
                .formatted(
                    customer.getC_id(),
                    customer.getC_d_id(),
                    customer.getC_w_id(),
                    district.getD_id(),
                    warehouse.getW_id(),
                    params.getH_amount()));

        if (customer.getC_data().length() > 500)
          customer.setC_data(customer.getC_data().substring(0, 500));
      }
      ctx.registry.update(ctx, customer);

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
      ctx.registry.create(ctx, history);

      // 2.5.3.3 The emulated terminal must display, in the appropriate fields of the
      // input/output screen, all input data and the output data resulting from the
      // execution of the transaction. The following fields are displayed: W_ID, D_ID,
      // C_ID, C_D_ID, C_W_ID, W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP,
      // D_STREET_1, D_STREET_2, D_CITY, D_STATE, D_ZIP, C_FIRST, C_MIDDLE, C_LAST,
      // C_STREET_1, C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT,
      // C_CREDIT_LIM, C_DISCOUNT, C_BALANCE, the first 200 characters of C_DATA (only
      // if C_CREDIT = "BC"), H_AMOUNT, and H_DATE.
      final PaymentOutput output =
          PaymentOutput.builder()
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

      return JSON.serialize(output);
    } catch (Exception err) {
      logger.error(err.toString());
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
  public String doStockLevel(final TPCCContext ctx, final String parameters) {
    // addTxInfo(ctx);
    // TPC-C 2.8.2.2
    try {
      final StockLevelInput params = JSON.deserialize(parameters, StockLevelInput.class);
      // The row in the DISTRICT table with matching D_W_ID and D_ID is selected and
      // D_NEXT_O_ID is retrieved.
      final District district =
          District.builder().w_id(params.getW_id()).id(params.getD_id()).build();
      ctx.registry.read(ctx, district);

      // All rows in the ORDER-LINE table with matching OL_W_ID (equals W_ID), OL_D_ID
      // (equals D_ID), and OL_O_ID (lower than D_NEXT_O_ID and greater than or equal to
      // D_NEXT_O_ID minus 5) are selected. They are the items for 5 recent orders of the
      // district.
      final int o_id_min = district.getD_next_o_id() - 5;
      final int o_id_max = district.getD_next_o_id();
      logger.debug("O_ID_MIN=%d, O_ID_MAX=%d".formatted(o_id_min, o_id_max));
      logger.debug("Getting the most recent 5 orders");
      final List<Integer> recentItemIds =
          getItemIdsOfRecentOrders(ctx, params.getW_id(), district.getD_id(), o_id_min, o_id_max);

      // All rows in the STOCK table with matching S_I_ID (equals OL_I_ID) and S_W_ID (equals W_ID)
      // from the list of distinct item numbers and with S_QUANTITY lower than threshold are counted
      // (giving low_stock).
      int lowStock = 0;
      for (final int i_id : recentItemIds) {
        final Stock stock = Stock.builder().w_id(params.getW_id()).i_id(i_id).build();
        ctx.registry.read(ctx, stock);
        if (stock.getS_quantity() < params.getThreshold()) {
          logger.debug("The stock quantity is less than the threshold");
          lowStock++;
        }
      }
      logger.debug("lowStock is " + lowStock);

      // 2.8.3.3 The emulated terminal must display, in the appropriate field of the
      // input/output screen, all input data and the output data which results from the
      // execution of the transaction. The following fields are displayed: W_ID, D_ID, threshold,
      // and low_stock.

      final StockLevelOutput output =
          StockLevelOutput.builder()
              .w_id(params.getW_id())
              .d_id(params.getD_id())
              .threshold(params.getThreshold())
              .low_stock(lowStock)
              .build();

      return JSON.serialize(output);

    } catch (Exception err) {
      logger.error(err.toString());
    }
    return null;
  }

  /**
   * Initializes the TPC-C chaincode.
   *
   * @param ctx The TX context.
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public void instantiate(final TPCCContext ctx) {
    logger.debug("Instantiating TPC-C chaincode");
  }

  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void initEntries(final TPCCContext ctx) {
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
      ctx.registry.create(ctx, warehouse);

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
      ctx.registry.create(ctx, district);

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
      ctx.registry.create(ctx, customer1);
      ctx.registry.create(ctx, customer2);

      final Item item1 =
          Item.builder().id(1).im_id(123).name("Cup").price(99.50).data("ORIGINAL").build();
      final Item item2 =
          Item.builder().id(2).im_id(234).name("Plate").price(89.50).data("ORIGINAL").build();
      final Item item3 =
          Item.builder().id(3).im_id(456).name("Glass").price(78.00).data("GENERIC").build();
      ctx.registry.create(ctx, item1);
      ctx.registry.create(ctx, item2);
      ctx.registry.create(ctx, item3);

      final Stock stock1 =
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
      ctx.registry.create(ctx, stock1);
      ctx.registry.create(ctx, stock2);
      ctx.registry.create(ctx, stock3);
    } catch (Exception err) {
      logger.error(err.toString());
    }
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String readWarehouseEntry(final TPCCContext ctx, final int w_id) {
    final Warehouse warehouse = Warehouse.builder().id(w_id).build();
    ctx.registry.read(ctx, warehouse);
    return JSON.serialize(warehouse);
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String getOrderEntry(
      final TPCCContext ctx, final int w_id, final int d_id, final int o_id) {
    final Order order = Order.builder().w_id(w_id).d_id(d_id).id(o_id).build();
    ctx.registry.read(ctx, order);
    return JSON.serialize(order);
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String getItemEntry(final TPCCContext ctx, final int i_id) {
    final Item item = Item.builder().id(i_id).build();
    ctx.registry.read(ctx, item);
    return JSON.serialize(item);
  }

  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String getNewOrderEntry(
      final TPCCContext ctx, final int w_id, final int d_id, final int o_id) {
    final NewOrder newOrder = NewOrder.builder().w_id(w_id).d_id(d_id).o_id(o_id).build();
    ctx.registry.read(ctx, newOrder);
    return JSON.serialize(newOrder);
  }

  @SuppressWarnings("SameReturnValue")
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String ping(final TPCCContext _ctx) {
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
  public String OJMTEST__getCustomer(
      final TPCCContext ctx, final int c_w_id, final int c_d_id, final int c_id) {
    final Customer customer = Customer.builder().w_id(c_w_id).d_id(c_d_id).id(c_id).build();
    return JSON.serialize(ctx.registry.read(ctx, customer));
  }

  /**
   * Retrieves the customers from the state database that match the given ID or last name
   *
   * @param ctx The TX context.
   * @param c_w_id The warehouse ID of the customer.
   * @param c_d_id The district ID of the customer.
   * @param c_id The customer ID.
   * @param c_last The last name of the customer.
   * @return The retrieved customers.
   * @throws Exception if neither the customer ID nor the customer last name parameter is supplied
   */
  private static Customer getCustomersByIdOrLastName(
      final ContextWithRegistry ctx,
      final int c_w_id,
      final int c_d_id,
      final Integer c_id,
      final String c_last)
      throws Exception {
    if (c_id == null && c_last == null) {
      throw new Exception("At least one of c_id and c_last must be specified");
    }

    if (c_id != null) {
      /* Case 1, the CUSTOMER is selected based on CUSTOMER number: the row in the CUSTOMER table with matching C_W_ID, C_D_ID, and C_ID is selected and C_BALANCE, C_FIRST, C_MIDDLE, and C_LAST are retrieved. */
      return ctx.registry.read(ctx, Customer.builder().w_id(c_w_id).d_id(c_d_id).id(c_id).build());
    } else {
      /* Case 2, the customer is selected based on customer last name: all rows in the CUSTOMER table with matching C_W_ID, C_D_ID and C_LAST are selected sorted by C_FIRST in ascending order. Let n be the number of rows selected. C_BALANCE, C_FIRST, C_MIDDLE, and C_LAST are retrieved from the row at position n/ 2 rounded up in the sorted set of selected rows from the CUSTOMER table. */
      final List<Customer> allCustomers =
          ctx.registry.readAll(ctx, Customer.builder().w_id(c_w_id).d_id(c_d_id).build());

      final List<Customer> matchingCustomers =
          allCustomers.stream().filter(customer -> customer.getC_last().equals(c_last)).toList();
      if (matchingCustomers.isEmpty()) {
        throw new Exception("Customer matching last name '%s' not found".formatted(c_last));
      }

      final double N = Math.ceil(matchingCustomers.size() / 2d);
      if (N > Integer.MAX_VALUE) {
        throw new Exception("Size of matching CUSTOMER list is out of range");
      }
      final int n = (int) N;

      return matchingCustomers.get(n);
    }
  }

  /**
   * Retrieves the last of a customer from the state database.
   *
   * @param ctx The TX context.
   * @param o_w_id The warehouse ID of the order.
   * @param o_d_id The district ID of the order.
   * @param o_c_id The customer ID for the order.
   * @return The retrieved order.
   * @throws Exception if the last order is not found.
   */
  private static Order getLastOrderOfCustomer(
      final ContextWithRegistry ctx, final int o_w_id, final int o_d_id, final int o_c_id)
      throws Exception {
    final List<Order> allOrders =
        ctx.registry.readAll(ctx, Order.builder().w_id(o_w_id).d_id(o_d_id).build());
    if (allOrders.isEmpty()) {
      throw new Exception("No orders found");
    }

    return allOrders.stream()
        .filter(order -> order.getO_c_id() == o_c_id)
        .max(Comparator.comparingInt(Order::getO_id))
        .orElseThrow(() -> new Exception("Could not find last order of customer"));
  }

  /**
   * Counts the number of items whose stock is below a given threshold.
   *
   * @param ctx The TX context.
   * @param w_id The warehouse ID.
   * @param d_id The district ID.
   * @param o_id_min The oldest/minimum order ID to consider (inclusive).
   * @param o_id_max The newest/maximum order ID to consider (exclusive).
   * @return The unique IDs of items from the recent orders.
   */
  private static List<Integer> getItemIdsOfRecentOrders(
      final ContextWithRegistry ctx,
      final int w_id,
      final int d_id,
      final int o_id_min,
      final int o_id_max)
      throws Exception {
    final Set<Integer> itemIds = new HashSet<>();
    for (int current_o_id = o_id_min; current_o_id < o_id_max; current_o_id++) {
      final Order order = Order.builder().w_id(w_id).d_id(d_id).id(current_o_id).build();
      ctx.registry.read(ctx, order);

      for (int ol_number = 1; ol_number <= order.getO_ol_cnt(); ol_number++) {
        final OrderLine orderLine =
            OrderLine.builder().w_id(w_id).d_id(d_id).o_id(current_o_id).number(ol_number).build();
        ctx.registry.read(ctx, orderLine);
        itemIds.add(orderLine.getOl_i_id());
      }
    }
    if (itemIds.isEmpty()) {
      throw new Exception("Could not find item IDs of recent ORDERs");
    }

    return new ArrayList<>(itemIds);
  }
}
