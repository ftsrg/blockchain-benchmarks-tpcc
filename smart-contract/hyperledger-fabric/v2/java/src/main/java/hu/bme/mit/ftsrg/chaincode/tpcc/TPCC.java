/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc;

import com.jcabi.aspects.Loggable;
import hu.bme.mit.ftsrg.chaincode.dataaccess.ContextWithRegistry;
import hu.bme.mit.ftsrg.chaincode.dataaccess.Registry;
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
  public static final int DISTRICT_COUNT = 10;

  /**
   * Performs the Delivery read-write TX profile [TPC-C 2.7].
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String doDelivery(final TPCCContext ctx, final String parameters) {
    final DeliveryInput params = JSON.deserialize(parameters, DeliveryInput.class);

    /*
     * [TPC-C 2.7.4.2]
     * For a given warehouse number (W_ID), for each of the districts
     * (D_W_ID, D_ID) within that warehouse, and for a given carrier
     * number (O_CARRIER_ID): ...
     */
    final List<DeliveredOrder> deliveredOrders = new ArrayList<>();
    int skipped = 0;
    logger.debug("Begin for loop to retrieve oldest NEW-ORDERs from the various districts");
    for (int d_id = 1; d_id <= DISTRICT_COUNT; ++d_id) {
      /*
       * [TPC-C 2.7.4.2 (3)]
       * The row in the NEW-ORDER table with matching NO_W_ID (equals
       * W_ID) and NO_D_ID (equals D_ID) and with the lowest NO_O_ID
       * value is selected.  This is the oldest undelivered order of
       * that district.  NO_O_ID, the order number, is retrieved. [...]
       */
      final int currentD_id = d_id;
      final List<NewOrder> matchingNewOrders =
          ctx.registry
              .select(ctx, new NewOrder())
              .matching(
                  new Registry.Matcher<NewOrder>() {
                    @Override
                    public boolean match(NewOrder entity) {
                      return entity.getNo_w_id() == params.getW_id()
                          && entity.getNo_d_id() == currentD_id;
                    }
                  })
              .sortedBy(
                  new Comparator<NewOrder>() {
                    @Override
                    public int compare(final NewOrder a, final NewOrder b) {
                      return a.getNo_o_id() - b.getNo_o_id();
                    }
                  })
              .get();
      /*
       * [TPC-C 2.7.4.2 (3) (continued)]
       * ... If no matching row is found, then the delivery of an order
       * for this district is skipped.  The condition in which no
       * outstanding order is present at a given district must be
       * handled by skipping the delivery of an order for that district
       * only and resuming the delivery of an order from all remaining
       * districts of the selected warehouse. If this condition occurs
       * in more than 1%, or in more than one, whichever is greater, of
       * the business transactions, it must be reported. [...]
       */
      if (matchingNewOrders.isEmpty()) {
        logger.debug(
            "Could not find new order for District(%d, %d); skipping it"
                .formatted(params.getW_id(), d_id));
        ++skipped;
        continue;
      }
      final NewOrder oldestNewOrder = matchingNewOrders.get(0);
      logger.debug("Oldest NEW-ORDER retrieved is: " + oldestNewOrder);

      /*
       * [TPC-C 2.7.4.2 (4)]
       * The selected row in the NEW-ORDER table is deleted.
       */
      ctx.registry.delete(ctx, oldestNewOrder);

      /*
       * [TPC-C 2.7.4.2 (5)]
       * The row in the ORDER table with matching O_W_ID (equals W_ ID),
       * O_D_ID (equals D_ID), and O_ID (equals NO_O_ID) is selected,
       * O_C_ID, the customer number, is retrieved, [...]
       */
      final Order order =
          Order.builder().w_id(params.getW_id()).d_id(d_id).id(oldestNewOrder.getNo_o_id()).build();
      ctx.registry.read(ctx, order);
      /*
       * [TPC-C 2.7.4.2 (5) (continued)]
       * ... and O_CARRIER_ID is updated.
       */
      order.setO_carrier_id(params.getO_carrier_id());
      ctx.registry.update(ctx, order);

      /*
       * [TPC-C 2.7.4.2 (6)]
       * All rows in the ORDER-LINE table with matching OL_W_ID (equals
       * O_W_ID), OL_D_ID (equals O_D_ID), and OL_O_ID (equals O_ID) are
       * selected. [...]
       */
      double orderLineAmountTotal = 0;
      for (int i = 1; i <= order.getO_ol_cnt(); ++i) {
        logger.debug("Getting ORDER-LINE #%d".formatted(i));
        final OrderLine orderLine =
            OrderLine.builder()
                .w_id(params.getW_id())
                .d_id(d_id)
                .o_id(order.getO_id())
                .number(i)
                .build();
        ctx.registry.read(ctx, orderLine);
        /*
         * [TPC-C 2.7.4.2 (6) (continued)]
         * ... All OL_DELIVERY_D, the delivery dates, are updated to the
         * current system time as returned by the operating system [...]
         */
        orderLine.setOl_delivery_d(params.getOl_delivery_d());
        /*
         * [TPC-C 2.7.4.2 (6) (continued)]
         * ... and the sum of all OL_AMOUNT is retrieved.
         */
        orderLineAmountTotal += orderLine.getOl_amount();
        ctx.registry.update(ctx, orderLine);
      }

      /*
       * [TPC-C 2.7.4.2 (7)]
       * The row in the CUSTOMER table with matching C_W_ID (equals
       * W_ID), C_D_ID (equals D_ID), and C_ID (equals O_C_ID) is
       * selected [...]
       */
      final Customer customer =
          Customer.builder().w_id(params.getW_id()).d_id(d_id).id(order.getO_c_id()).build();
      ctx.registry.read(ctx, customer);
      /*
       * [TPC-C 2.7.4.2 (7) (continued)]
       * ... and C_BALANCE is increased by the sum of all order-line
       * amounts (OL_AMOUNT) previously retrieved. [...]
       */
      customer.increaseBalance(orderLineAmountTotal);
      /*
       * [TPC-C 2.7.4.2 (7) (continued)]
       * ... C_DELIVERY_CNT is incremented by 1.
       */
      customer.incrementDeliveryCount();
      ctx.registry.update(ctx, customer);

      deliveredOrders.add(DeliveredOrder.builder().d_id(d_id).o_id(order.getO_id()).build());
    }

    return JSON.serialize(
        DeliveryOutput.builder()
            .w_id(params.getW_id())
            .o_carrier_id(params.getO_carrier_id())
            .delivered(deliveredOrders)
            .skipped(skipped)
            .build());
  }

  /**
   * Performs the New-Order read-write TX profile [TPC-C 2.4].
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String doNewOrder(final TPCCContext ctx, final String parameters) {
    final NewOrderInput params = JSON.deserialize(parameters, NewOrderInput.class);
    /*
     * [TPC-C 2.4.2.2]
     * For a given warehouse number (W_ID), district number (D_W_ID,
     * D_ID), customer number (C_W_ID, C_D_ID, C_ ID), count of items
     * (ol_cnt, not communicated to the SUT), and for a given set of
     * items (OL_I_ID), supplying warehouses (OL_SUPPLY_W_ID), and
     * quantities (OL_QUAN TITY): ...
     */
    try {
      /*
       * [TPC-C 2.4.2.2 (3)]
       * The row in the WAREHOUSE table with matching W_ID is selected
       * and W_TAX, the warehouse tax rate, is retrieved.
       */
      final Warehouse warehouse = Warehouse.builder().id(params.getW_id()).build();
      ctx.registry.read(ctx, warehouse);

      /*
       * [TPC-C 2.4.2.2 (4)]
       * The row in the DISTRICT table with matching D_W_ID and D_ ID is
       * selected, D_TAX, the district tax rate, is retrieved, [...]
       */
      final District district =
          District.builder().w_id(warehouse.getW_id()).id(params.getD_id()).build();
      ctx.registry.read(ctx, district);
      /*
       * [TPC-C 2.4.2.2 (4) (continued)]
       * ... and D_NEXT_O_ID, the next available order number for the
       * district, is retrieved and incremented by one.
       */
      final int nextOrderId = district.getD_next_o_id();
      district.incrementNextOrderID();
      ctx.registry.update(ctx, district);
      logger.debug(
          "Next available order number for DISTRICT with D_ID=%d incremented; new DISTRICT: %s"
              .formatted(district.getD_id(), district));

      /*
       * [TPC-C 2.4.2.2 (5)]
       * The row in the CUSTOMER table with matching C_W_ID, C_D_ID, and
       * C_ID is selected and C_DISCOUNT, the customer's discount rate,
       * C_LAST, the customer's last name, and C_CREDIT, the customer's
       * credit status, are retrieved.
       */
      final Customer customer =
          Customer.builder()
              .w_id(warehouse.getW_id())
              .d_id(district.getD_id())
              .id(params.getC_id())
              .build();
      ctx.registry.read(ctx, customer);

      /*
       * [TPC-C 2.4.2.2 (6)]
       * A new row is inserted into both the NEW-ORDER table and the
       * ORDER table to reflect the creation of the new order. [...]
       */
      final NewOrder newOrder =
          NewOrder.builder()
              .o_id(nextOrderId)
              .d_id(district.getD_id())
              .w_id(warehouse.getW_id())
              .build();
      ctx.registry.create(ctx, newOrder);
      boolean allItemsLocal = true;
      for (final int id : params.getI_w_ids())
        if (id != warehouse.getW_id()) {
          allItemsLocal = false;
          break;
        }
      /*
       * [TPC-C 2.4.2.2 (6) (continued)]
       * ... O_CARRIER_ID is set to a null value.  If the order includes
       * only home order-lines, then O_ALL_LOCAL is set to 1, otherwise
       * O_ALL_LOCAL is set to 0.
       */
      /*
       * [TPC-C 2.4.2.2 (7)]
       * The number of items, O_OL_CNT, is computed to match ol_cnt.
       */
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

      /*
       * [TPC-C 2.4.2.2 (8)]
       * For each O_OL_CNT item on the order: ...
       */
      final List<ItemsData> itemsData = new ArrayList<>();
      double totalOrderLineAmount = 0;
      for (int i = 0; i < params.getI_ids().length; ++i) {
        final int i_id = params.getI_ids()[i];
        final int i_w_id = params.getI_w_ids()[i];
        final int i_qty = params.getI_qtys()[i];

        /*
         * [TPC-C 2.4.2.2 (8.1)]
         * The row in the ITEM table with matching I_ID (equals OL_I_ID)
         * is selected and I_PRICE, the price of the item, I_NAME, the
         * name of the item, and I_DATA are retrieved.  If I_ID has an
         * unused value (see Clause 2.4.1.5), a "not-found" condition is
         * signaled, resulting in a rollback of the database transaction
         * (see Clause 2.4.2.3).
         */
        final Item item = Item.builder().id(i_id).build();
        ctx.registry.read(ctx, item);

        /*
         * [TPC-C 2.4.2.2 (8.2)]
         * The row in the STOCK table with matching S_I_ID (equals
         * OL_I_ID) and S_W_ID (equals OL_SUPPLY_W_ID) is selected.
         * S_QUANTITY, the quantity in stock, S_DIST_xx, where xx
         * represents the district number, and S_DATA are retrieved.
         * [...]
         */
        final Stock stock = Stock.builder().w_id(i_w_id).i_id(i_id).build();
        ctx.registry.read(ctx, stock);
        /*
         * [TPC-C 2.4.2.2 (8.2) (continued)]
         * ... If the retrieved value for S_QUANTITY exceeds OL_QUANTITY
         * by 10 or more, then S_QUANTITY is decreased by OL_QUANTITY;
         * otherwise S_QUANTITY is updated to (S_QUANTITY - OL_QUANTITY)
         * + 91. [...]
         */
        if (stock.getS_quantity() >= i_qty + 10) stock.decreaseQuantity(i_qty);
        else stock.setS_quantity(stock.getS_quantity() - i_qty + 91);
        /*
         * [TPC-C 2.4.2.2 (8.2) (continued)]
         * ... S_YTD is increased by OL_QUANTITY [...]
         */
        stock.increaseYTD(i_qty);
        /*
         * [TPC-C 2.4.2.2 (8.2) (continued)]
         * ... and S_ORDER_CNT is incremented by 1. [...]
         */
        stock.incrementOrderCount();
        /*
         * [TPC-C 2.4.2.2 (8.2) (continued)]
         * ... If the order-line is remote, then S_REMOTE_CNT is
         * incremented by 1.
         */
        if (i_w_id != warehouse.getW_id()) stock.incrementRemoteCount();
        ctx.registry.update(ctx, stock);

        /*
         * [TPC-C 2.4.2.2 (8.3)]
         * The amount for the item in the order (OL_AMOUNT) is computed
         * as: OL_QUANTITY * I_PRICE
         */
        final double orderLineAmount = i_qty * item.getI_price();
        totalOrderLineAmount += orderLineAmount;

        /*
         * [TPC-C 2.4.2.2 (8.4)]
         * The strings in I_DATA and S_DATA are examined.  If they both
         * include the string "ORIGINAL", the brand-generic field for
         * that item is set to "B", otherwise, the brand-generic field
         * is set to "G".
         */
        final String brandGeneric =
            item.getI_data().contains("ORIGINAL") && stock.getS_data().contains("ORIGINAL")
                ? "B"
                : "G";

        /*
         * [TPC-C 2.4.2.2 (8.5)]
         * A new row is inserted into the ORDER-LINE table to reflect
         * the item on the order.  OL_DELIVERY_D is set to a null value,
         * OL_NUMBER is set to a unique value within all the ORDER-LINE
         * rows that have the same OL_O_ID value, and OL_DIST_INFO is
         * set to the content of S_DIST_xx, where xx represents the
         * district number (OL_D_ID)
         */
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

        /*
         * [TPC-C 2.4.3.3]
         * The emulated terminal must display, in the appropriate fields
         * of the input/output screen, all input data and the output
         * data resulting from the execution of the transaction. The
         * display field s are divided in two groups as follows: [...]
         */
        /*
         * [TPC-C 2.4.3.3 (2)]
         * One repeating group of fields: OL_SUPPLY_W_ID, OL_I_ID,
         * I_NAME, OL_QUANTITY, S_QUANTITY, brand_generic, I_PRICE, and
         * OL_AMOUNT. The group is repeated O_OL_CNT times (once per
         * item in the order), equal to the computed value of ol_cnt.
         */
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

      /*
       * [TPC-C 2.4.2.2 (9)]
       * The total-amount for the complete order is computed as:
       * sum(OL_AMOUNT) * (1 - C_DISCOUNT) * (1 + W_TAX + D_TAX)
       */
      final double totalAmount =
          totalOrderLineAmount
              * (1 - customer.getC_discount())
              * (1 + warehouse.getW_tax() + district.getD_tax());
      logger.debug("Total amount is " + totalAmount);

      /*
       * [TPC-C 2.4.3.3 (1)]
       * One non-repeating group of fields: W_ID, D_ID, C_ID, O_ID,
       * O_OL_CNT, C_LAST, C_CREDIT, C_DISCOUNT, W_TAX, D_TAX,
       * O_ENTRY_D, total_amount, and an optional execution status
       * message other than "Item number is not valid".
       */
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
   * Performs the Order-Status read TX profile [2.6].
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String doOrderStatus(final TPCCContext ctx, final String parameters) {
    /*
     * [TPC-C 2.6.2.2]
     * For a given customer number (C_W_ID, C_D_ID, C_ID): ...
     */
    final OrderStatusInput params = JSON.deserialize(parameters, OrderStatusInput.class);
    try {
      /*
       * [TPC-C 2.6.2.2 (3.1)]
       * Case 1, the CUSTOMER is selected based on CUSTOMER number: the
       * row in the CUSTOMER table with matching C_W_ID, C_D_ID, and
       * C_ID is selected and C_BALANCE, C_FIRST, C_MIDDLE, and C_LAST
       * are retrieved. */
      /*
       * [TPC-C 2.6.2.2 (3.2)]
       * Case 2, the customer is selected based on customer last name:
       * all rows in the CUSTOMER table with matching C_W_ID, C_D_ID and
       * C_LAST are selected sorted by C_FIRST in ascending order. Let n
       * be the number of rows selected. C_BALANCE, C_FIRST, C_MIDDLE,
       * and C_LAST are retrieved from the row at position n/ 2 rounded
       * up in the sorted set of selected rows from the CUSTOMER table.
       */
      final Customer customer =
          getCustomersByIdOrLastName(
              ctx, params.getW_id(), params.getD_id(), params.getC_id(), params.getC_last());
      /*
       * [TPC-C 2.6.2.2 (4)]
       * The row in the ORDER table with matching O_W_ID (equals
       * C_W_ID), O_D_ID (equals C_D_ID), O_C_ID (equals C_ID), and with
       * the largest existing O_ID, is selected. This is the most recent
       * order placed by that customer. O_ID, O_ENTRY_D, and
       * O_CARRIER_ID are retrieved.
       */
      final Order order =
          getLastOrderOfCustomer(
              ctx, customer.getC_w_id(), customer.getC_d_id(), customer.getC_id());

      /*
       * [TPC-C 2.6.2.2 (5)]
       * All rows in the ORDER-LINE table with matching OL_W_ID (equals
       * O_W_ID), OL_D_ID (equals O_D_ID), and OL_O_ID (equals O_ID) are
       * selected and the corresponding sets of OL_I_ID, OL_SUPPLY_W_ID,
       * OL_QUANTITY, OL_AMOUNT, and OL_DELIVERY_D are retrieved.
       */
      final List<OrderLineData> orderLineData = new ArrayList<>();
      for (int i = 1; i <= order.getO_ol_cnt(); ++i) {
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

      /*
       * [TPC-C 2.6.3.3]
       * The emulated terminal must display, in the appropriate fields
       * of the input/output screen, all input data and the output data
       * resulting from the execution of the transaction. The display
       * fields are divided in two groups as follows: ...
       */
      /*
       * [TPC-C 2.6.3.3 (1)]
       * One non-repeating group of fields: W_ID, D_ID, C_ID, C_FIRST,
       * C_MIDDLE, C_LAST, C_BALANCE, O_ID, O_ENTRY_D, and O_CARRIER_ID;
       *
       */
      /*
       * [TPC-C 2.6.3.3 (2)]
       * One repeating group of fields: OL_SUPPLY_W_ID, OL_I_ID,
       * OL_QUANTITY, OL_AMOUNT, and OL_DELIVERY_D. The group is
       * repeated O_OL_CNT times (once per item in the order).
       */
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
   * Performs the Payment read-write TX profile [TPC-C 2.5].
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String doPayment(final TPCCContext ctx, final String parameters) {
    /*
     * [TPC-C 2.5.2.2]
     * For a given warehouse number (W_ID), district number (D_W_ID,
     * D_ID), customer number (C_W_ID , C_D_ID, C_ ID) or customer last
     * name (C_W_ID, C_D_ID, C_LAST), and payment amount (H_AMOUNT): ...
     *
     */
    final PaymentInput params = JSON.deserialize(parameters, PaymentInput.class);
    try {
      /*
       * [TPC-C 2.5.2.2 (3)]
       * The row in the WAREHOUSE table with matching W_ID is selected.
       * W_NAME, W_STREET_1, W_STREET_2, W_CITY, W_STATE, and W_ZIP are
       * retrieved [...]
       */
      final Warehouse warehouse = Warehouse.builder().id(params.getW_id()).build();
      ctx.registry.read(ctx, warehouse);
      /*
       * [TPC-C 2.5.2.2 (3) (continued)]
       * ... and W_YTD, the warehouse's year-to-date balance, is
       * increased by H_AMOUNT.
       */
      warehouse.increaseYTD(params.getH_amount());
      ctx.registry.update(ctx, warehouse);

      /*
       * [TPC-C 2.5.2.2 (4)]
       * The row in the DISTRICT table with matching D_W_ID and D_ID is
       * selected. D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE, and
       * D_ZIP are retrieved [...]
       */
      final District district =
          District.builder().w_id(warehouse.getW_id()).id(params.getD_id()).build();
      ctx.registry.read(ctx, district);
      /*
       * [TPC-C 2.5.2.2 (4) (continued)]
       * ... and D_YTD, the district's year-to-date balance, is
       * increased by H_AMOUNT.
       */
      district.increaseYTD(params.getH_amount());
      ctx.registry.update(ctx, district);

      /*
       * [TPC-C 2.5.2.2 (5.1)]
       * Case 1, the customer is selected based on customer number: the
       * row in the CUSTOMER table with matching C_W_ID, C_D_ID and C_ID
       * is selected. C_FIRST, C_MIDDLE, C_LAST, C_STREET_1, C_STREET_2,
       * C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT,
       * C_CREDIT_LIM, C_DISCOUNT, and C_BALANCE are retrieved. [...]
       */
      /*
       * [TPC-C 2.5.2.2 (5.2)]
       * Case 2, the customer is selected based on customer last name:
       * all rows in the CUSTOMER table with matching C_W_ID, C_D_ID and
       * C_LAST are selected sorted by C_FIRST in ascending order. Let n
       * be the number of rows selected. C_ID, C_FIRST, C_MIDDLE,
       * C_STREET_1, C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE,
       * C_SINCE, C_CREDIT, C_CREDIT_LIM, C_DISCOUNT, and C_BALANCE are
       * retrieved from the row at position (n/2 rounded up to the next
       * integer) in the sorted set of selected rows from the CUSTOMER
       * table. [...]
       */
      final Customer customer =
          getCustomersByIdOrLastName(
              ctx, warehouse.getW_id(), district.getD_id(), params.getC_id(), params.getC_last());
      /*
       * [TPC-C 2.5.2.2 (5.1-2) (continued)]
       * ... C_BALANCE is decreased by H_AMOUNT. [...]
       */
      customer.decreaseBalance(params.getH_amount());
      /*
       * [TPC-C 2.5.2.2 (5.1-2) (continued)]
       * ... C_YTD_PAYMENT is increased by H_AMOUNT. [...]
       */
      customer.increaseYTDPayment(params.getH_amount());
      /*
       * [TPC-C 2.5.2.2 (5.1-2) (continued)]
       * ... C_PAYMENT_CNT is incremented by 1.
       */
      customer.incrementPaymentCount();

      /*
       * [TPC-C 2.5.2.2 (6)]
       * If the value of C_CREDIT is equal to "BC", [...]
       */
      if (customer.getC_credit().equals("BC")) {
        /*
         * [TPC-C 2.5.2.2 (6) (continued)]
         * ... then C_DATA is also retrieved from the selected customer
         * and the following history information: C_ID, C_D_ID, C_W_ID,
         * D_ID, W_ID, and H_AMOUNT, are inserted at the left of the
         * C_DATA field by shifting the existing content of C_DATA to
         * the right by an equal number of bytes [...]
         */
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
        /*
         * [TPC-C 2.5.2.2 (6) (continued)]
         * ... and by discarding the bytes that are shifted out of the
         * right side of the C_DATA field. The content of the C_DATA
         * field never exceeds 500 characters.
         */
        if (customer.getC_data().length() > 500)
          customer.setC_data(customer.getC_data().substring(0, 500));
      }
      /*
       * [TPC-C 2.5.2.2 (6) (continued)]
       * ... The selected customer is updated with the new C_DATA field.
       */
      ctx.registry.update(ctx, customer);

      /*
       * [TPC-C 2.5.2.2 (7)]
       * H_DATA is built by concatenating W_NAME and D_NAME separated by
       * 4 spaces.
       */
      final String h_data =
          "%s%s%s".formatted(warehouse.getW_name(), " ".repeat(4), district.getD_name());

      /*
       * [TPC-C 2.5.2.2 (8)]
       * A new row is inserted into the HISTORY table with H_C_ID =
       * C_ID, H_C_D_ID = C_D_ID, H_C_W_ID = C_W_ID, H_D_ID = D_ID, and
       * H_W_ID = W_ID.
       */
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

      /*
       * [TPC-C 2.5.3.3]
       * The emulated terminal must display, in the appropriate fields
       * of the input/output screen, all input data and the output data
       * resulting from the execution of the transaction.  The following
       * fields are displayed: W_ID, D_ID, C_ID, C_D_ID, C_W_ID,
       * W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP, D_STREET_1,
       * D_STREET_2, D_CITY, D_STATE, D_ZIP, C_FIRST, C_MIDDLE, C_LAST,
       * C_STREET_1, C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE,
       * C_SINCE, C_CREDIT, C_CREDIT_LIM, C_DISCOUNT, C_BALANCE, the
       * first 200 characters of C_DATA (only if C_CREDIT = "BC"),
       * H_AMOUNT, and H_DATE.
       */
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
      if (customer.getC_credit().equals("BC"))
        output.setC_data(customer.getC_data().substring(0, 200));

      return JSON.serialize(output);
    } catch (Exception err) {
      logger.error(err.toString());
    }
    return null;
  }

  /**
   * Performs the Stock-Level read TX profile [TPC-C 2.8].
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String doStockLevel(final TPCCContext ctx, final String parameters) {
    /*
     * [TPC-C 2.8.2.2]
     * For a given warehouse number (W_ID), district number (D_W_ID,
     * D_ID), and stock level threshold (threshold): ...
     */
    final StockLevelInput params = JSON.deserialize(parameters, StockLevelInput.class);
    try {
      /*
       * [TPC-C 2.8.2.2 (3)]
       * The row in the DISTRICT table with matching D_W_ID and D_ID is
       * selected and D_NEXT_O_ID is retrieved.
       */
      final District district =
          District.builder().w_id(params.getW_id()).id(params.getD_id()).build();
      ctx.registry.read(ctx, district);

      /*
       * [TPC-C 2.8.2.2 (4)]
       * All rows in the ORDER-LINE table with matching OL_W_ID (equals
       * W_ID), OL_D_ID (equals D_ID), and OL_O_ID (lower than
       * D_NEXT_O_ID and greater than or equal to D_NEXT_O_ID minus 5)
       * are selected. They are the items for 5 recent orders of the
       * district.
       */
      final int o_id_min = district.getD_next_o_id() - 5;
      final int o_id_max = district.getD_next_o_id();
      logger.debug("O_ID_MIN=%d, O_ID_MAX=%d".formatted(o_id_min, o_id_max));
      logger.debug("Getting the most recent 5 orders");
      final List<Integer> recentItemIds =
          getItemIdsOfRecentOrders(ctx, params.getW_id(), district.getD_id(), o_id_min, o_id_max);

      /*
       * [TPC-C 2.8.2.2 (5)]
       * All rows in the STOCK table with matching S_I_ID (equals
       * OL_I_ID) and S_W_ID (equals W_ID) from the list of distinct
       * item numbers and with S_QUANTITY lower than threshold are
       * counted (giving low_stock).
       */
      int lowStock = 0;
      for (final int i_id : recentItemIds) {
        final Stock stock = Stock.builder().w_id(params.getW_id()).i_id(i_id).build();
        ctx.registry.read(ctx, stock);
        if (stock.getS_quantity() < params.getThreshold()) {
          logger.debug("The stock quantity is less than the threshold");
          ++lowStock;
        }
      }
      logger.debug("lowStock is " + lowStock);

      /*
       * [TPC-C 2.8.3.3]
       * The emulated terminal must display, in the appropriate field of
       * the input/output screen, all input data and the output data
       * which results from the execution of the transaction.  The
       * following fields are displayed: W_ID, D_ID, threshold, and
       * low_stock.
       */
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
   * Creates some dummy initial entities for testing.
   *
   * @param ctx The transaction context
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void init(final TPCCContext ctx) {
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

  /**
   * Returns a warehouse entity (for debugging).
   *
   * @param ctx The transaction context
   * @param w_id The W_ID of the warehouse
   * @return The warehouse with matching W_ID
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String readWarehouse(final TPCCContext ctx, final int w_id) {
    final Warehouse warehouse = Warehouse.builder().id(w_id).build();
    ctx.registry.read(ctx, warehouse);
    return JSON.serialize(warehouse);
  }

  /**
   * Returns an order entity (for debugging).
   *
   * @param ctx The transaction context
   * @param w_id The W_ID of the order
   * @param d_id The D_ID of the order
   * @param o_id The O_ID of the order
   * @return The order with matching (W_ID, D_ID, O_ID)
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String readOrder(final TPCCContext ctx, final int w_id, final int d_id, final int o_id) {
    final Order order = Order.builder().w_id(w_id).d_id(d_id).id(o_id).build();
    ctx.registry.read(ctx, order);
    return JSON.serialize(order);
  }

  /**
   * Returns an item entity (for debugging).
   *
   * @param ctx The transaction context
   * @param i_id The I_ID of the item
   * @return The item with matchign I_ID
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String readItem(final TPCCContext ctx, final int i_id) {
    final Item item = Item.builder().id(i_id).build();
    ctx.registry.read(ctx, item);
    return JSON.serialize(item);
  }

  /**
   * Returns a new-order entity (for debugging).
   *
   * @param ctx The transaction context
   * @param w_id The W_ID of the new-order
   * @param d_id The D_ID of the new-order
   * @param o_id The O_ID of the new-order
   * @return The new-order with matching (W_ID, D_ID, O_ID)
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String readNewOrder(
      final TPCCContext ctx, final int w_id, final int d_id, final int o_id) {
    final NewOrder newOrder = NewOrder.builder().w_id(w_id).d_id(d_id).o_id(o_id).build();
    ctx.registry.read(ctx, newOrder);
    return JSON.serialize(newOrder);
  }

  /**
   * Should always return 'pong' (for diagnostics).
   *
   * @param _ctx The transaction context (unused)
   * @return 'pong'
   */
  @SuppressWarnings("SameReturnValue")
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String ping(final TPCCContext _ctx) {
    return "pong";
  }

  /**
   * Dummy OpenJML test.
   *
   * <p>Should only allow getting the details of customer #1, but not customer #2
   *
   * <p>MAY ONLY be called only after {@link TPCC#init}
   *
   * @param ctx The transaction context
   * @param c_w_id The C_W_ID of the customer
   * @param c_d_id The C_D_ID of the customer
   * @param c_id The C_ID of the customer
   * @return The customer with matching (C_W_ID, C_D_ID, C_ID), unless C_ID >= 2, in which case an
   *     exception should be thrown by OpenJML
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
   * Retrieves the customers from the state database that match the given ID or last name.
   *
   * <p>At least one of <code>c_id</code> and/or <code>c_last</code> must be a non-null value.
   *
   * @param ctx The transaction context
   * @param c_w_id The C_W_ID of the customer
   * @param c_d_id The C_D_ID of the customer
   * @param c_id The C_ID of the customer
   * @param c_last The C_LAST of the customer
   * @return The customer with matching (C_W_ID, C_D_ID, C_ID) or (C_W_ID, C_D_ID, C_LAST)
   *     (according to the TPC-C spec)
   * @throws Exception if neither the customer ID nor the customer last name parameter is supplied
   */
  private static Customer getCustomersByIdOrLastName(
      final ContextWithRegistry ctx,
      final int c_w_id,
      final int c_d_id,
      final Integer c_id,
      final String c_last)
      throws Exception {
    if (c_id == null && c_last == null)
      throw new Exception("At least one of c_id and c_last must be specified");

    if (c_id != null)
      return ctx.registry.read(ctx, Customer.builder().w_id(c_w_id).d_id(c_d_id).id(c_id).build());
    else {
      final List<Customer> allCustomers =
          ctx.registry.readAll(ctx, Customer.builder().w_id(c_w_id).d_id(c_d_id).build());

      // Stream-based one-liner replaced with below code to accommodate OpenJML...
      final List<Customer> matchingCustomers = new ArrayList<>();
      for (final Customer c : allCustomers)
        if (c.getC_last().equals(c_last)) matchingCustomers.add(c);
      if (matchingCustomers.isEmpty())
        throw new Exception("Customer matching last name '%s' not found".formatted(c_last));

      final double N = Math.ceil(matchingCustomers.size() / 2d);
      if (N > Integer.MAX_VALUE)
        throw new Exception("Size of matching CUSTOMER list is out of range");
      final int n = (int) N;

      return matchingCustomers.get(n);
    }
  }

  /**
   * Retrieves the last order of a customer from the state database.
   *
   * @param ctx The transaction context
   * @param o_w_id The O_W_ID of the order
   * @param o_d_id The O_D_ID of the order
   * @param o_c_id The O_C_ID of the order
   * @return The order with highest O_ID from the orders matching (O_W_ID, O_D_ID, O_C_ID)
   * @throws Exception if the order is not found
   */
  private static Order getLastOrderOfCustomer(
      final ContextWithRegistry ctx, final int o_w_id, final int o_d_id, final int o_c_id)
      throws Exception {
    final List<Order> allOrders =
        ctx.registry.readAll(ctx, Order.builder().w_id(o_w_id).d_id(o_d_id).build());
    if (allOrders.isEmpty()) throw new Exception("No orders found");

    // Stream-based one-liner replaced with below code to accommodate OpenJML...
    final List<Order> matchingOrders = new ArrayList<>();
    for (final Order o : allOrders) if (o.getO_c_id() == o_c_id) matchingOrders.add(o);
    if (matchingOrders.isEmpty()) throw new Exception("Could not find last order of customer");
    matchingOrders.sort(
        new Comparator<Order>() {
          @Override
          public int compare(final Order a, final Order b) {
            return b.getO_id() - a.getO_id();
          }
        });
    return matchingOrders.get(0);
  }

  /**
   * Counts the number of items whose stock is below a given threshold.
   *
   * @param ctx The transaction context
   * @param w_id The W_ID to match on
   * @param d_id The D_ID to match on
   * @param o_id_min The oldest/minimum order ID to consider (inclusive)
   * @param o_id_max The newest/maximum order ID to consider (exclusive)
   * @return The unique IDs of items from the recent orders
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
    if (itemIds.isEmpty()) throw new Exception("Could not find item IDs of recent ORDERs");

    return new ArrayList<>(itemIds);
  }
}
