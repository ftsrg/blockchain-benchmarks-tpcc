/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jcabi.aspects.Loggable;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.*;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.extra.*;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.input.*;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.output.*;
import hu.bme.mit.ftsrg.chaincode.tpcc.middleware.TPCCContext;
import hu.bme.mit.ftsrg.hypernate.Registry;
import hu.bme.mit.ftsrg.hypernate.context.ContextWithRegistry;
import hu.bme.mit.ftsrg.hypernate.entity.EntityExistsException;
import hu.bme.mit.ftsrg.hypernate.entity.EntityNotFoundException;
import hu.bme.mit.ftsrg.hypernate.entity.SerializationException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * The implementation of the TPC-C benchmark smart contract according to the specification version
 * v5.11.0.
 */
@Loggable(Loggable.DEBUG)
class TPCCBusinessAPI {

  private final Logger logger = LoggerFactory.getLogger(TPCCBusinessAPI.class);

  private static final int DISTRICT_COUNT = 10;

  /**
   * Performs the Delivery read-write TX profile [TPC-C 2.7].
   *
   * @param ctx The TX context.
   * @param input The input parameters
   * @return The transaction output
   */
  DeliveryOutput delivery(final TPCCContext ctx, final DeliveryInput input)
      throws EntityNotFoundException, SerializationException, JsonProcessingException {

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
      final DeliveredOrder deliveredOrder =
          deliverOldestNewOrderForDistrict(
              ctx, input.getW_id(), d_id, input.getO_carrier_id(), input.getOl_delivery_d());
      if (deliveredOrder == null) {
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
        logger.debug(
            "Could not find new order for District({}, {}); skipping it", input.getW_id(), d_id);
        ++skipped;
      } else {
        deliveredOrders.add(deliveredOrder);
      }
    }

    final DeliveryOutput output =
        DeliveryOutput.builder()
            .w_id(input.getW_id())
            .o_carrier_id(input.getO_carrier_id())
            .delivered(deliveredOrders)
            .skipped(skipped)
            .build();
    ctx.commit();
    return output;
  }

  /**
   * Performs the New-Order read-write TX profile [TPC-C 2.4].
   *
   * @param ctx The TX context.
   * @param input The input parameters
   * @return The transaction output
   */
  NewOrderOutput newOrder(final TPCCContext ctx, final NewOrderInput input)
      throws EntityNotFoundException, EntityExistsException, SerializationException {
    /*
     * [TPC-C 2.4.2.2]
     * For a given warehouse number (W_ID), district number (D_W_ID,
     * D_ID), customer number (C_W_ID, C_D_ID, C_ ID), count of items
     * (ol_cnt, not communicated to the SUT), and for a given set of
     * items (OL_I_ID), supplying warehouses (OL_SUPPLY_W_ID), and
     * quantities (OL_QUANTITY): ...
     */

    final Registry registry = ctx.getRegistry();

    /*
     * [TPC-C 2.4.2.2 (3)]
     * The row in the WAREHOUSE table with matching W_ID is selected
     * and W_TAX, the warehouse tax rate, is retrieved.
     */
    final Warehouse warehouse = Warehouse.builder().id(input.getW_id()).build();
    registry.read(warehouse);

    /*
     * [TPC-C 2.4.2.2 (4)]
     * The row in the DISTRICT table with matching D_W_ID and D_ ID is
     * selected, D_TAX, the district tax rate, is retrieved, [...]
     */
    final District district =
        District.builder().w_id(warehouse.getW_id()).id(input.getD_id()).build();
    registry.read(district);
    /*
     * [TPC-C 2.4.2.2 (4) (continued)]
     * ... and D_NEXT_O_ID, the next available order number for the
     * district, is retrieved and incremented by one.
     */
    final int nextOrderId = district.getD_next_o_id();
    district.incrementNextOrderID();
    registry.update(district);
    logger.debug(
        "Next available order number for DISTRICT with D_ID={} incremented; new DISTRICT: {}",
        district.getD_id(),
        district);

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
            .id(input.getC_id())
            .build();
    registry.read(customer);

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
    registry.create(newOrder);
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
            .entry_d(input.getO_entry_d())
            .carrier_id(0)
            .ol_cnt(input.getI_ids().length)
            .all_local(allMatch(input.getI_w_ids(), warehouse.getW_id()) ? 1 : 0)
            .build();
    registry.create(order);

    /*
     * [TPC-C 2.4.2.2 (8)]
     * For each O_OL_CNT item on the order: ...
     */
    final List<ItemsData> itemsDataList = new ArrayList<>();
    double totalOrderLineAmount = 0;
    final int[] i_ids = input.getI_ids();
    final int[] i_w_ids = input.getI_w_ids();
    final int[] i_qtys = input.getI_qtys();
    for (int i = 0; i <= input.getI_ids().length; ++i) {
      /*
       * [TPC-C 2.4.2.2 (8.1)]
       * The row in the ITEM table with matching I_ID (equals OL_I_ID)
       * is selected and I_PRICE, the price of the item, I_NAME, the
       * name of the item, and I_DATA are retrieved.  If I_ID has an
       * unused value (see Clause 2.4.1.5), a "not-found" condition is
       * signaled, resulting in a rollback of the database transaction
       * (see Clause 2.4.2.3).
       */
      final Item item = Item.builder().id(i_ids[i]).build();
      try {
        registry.read(item);
      } catch (EntityNotFoundException e) {
        /*
         * [TPC-C 2.4.2.3]
         * For transactions that rollback as a result of an unused item
         * number, the complete transaction profile must be executed
         * with the exception that the follow ing steps need not be
         * done:
         * - Selecting and retrieving the row in the STOCK table with
         *   S_I_ID matching the unused item number.
         * - Examining the strings I_DATA and S_DATA for the unused
         *   item.
         * - Inserting a new row into the ORDER-LINE table for the
         *   unused item.
         * - Adding the amount for the unused item to the sum of all
         *   OL_AMOUNT.
         */
        /*
         * [TPC-C 2.4.3.4]
         * For transactions that are rolled back as a result of an
         * unused item number (1% of all New-Order transactions), the
         * emulated terminal must display in the appropriate fields of
         * the input/output screen the fields: W_ID, D_ID, C_ID, C_LAST,
         * C_CREDIT, O_ID, and the execution status message
         * "Item number is not valid".  Note that no execution status
         * message is required for successfully committed transactions.
         * However, this field may not display
         * "Item number is not valid" if the transaction is successful.
         */
        return NewOrderOutput.builder()
            .fromWarehouse(warehouse)
            .fromDistrict(district)
            .fromCustomer(customer)
            .fromOrder(order)
            .message("Item number is not valid")
            .build();
      }

      totalOrderLineAmount +=
          createOrderLineAndGetAmount(
              ctx,
              item,
              i_ids[i],
              i_w_ids[i],
              i_qtys[i],
              warehouse.getW_id(),
              district.getD_id(),
              nextOrderId,
              i,
              itemsDataList);
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
    logger.debug("Total amount is {}", totalAmount);

    /*
     * [TPC-C 2.4.3.3 (1)]
     * One non-repeating group of fields: W_ID, D_ID, C_ID, O_ID,
     * O_OL_CNT, C_LAST, C_CREDIT, C_DISCOUNT, W_TAX, D_TAX,
     * O_ENTRY_D, total_amount, and an optional execution status
     * message other than "Item number is not valid".
     */
    final NewOrderOutput output =
        NewOrderOutput.builder()
            .fromWarehouse(warehouse)
            .fromDistrict(district)
            .fromCustomer(customer)
            .fromOrder(order)
            .total_amount(totalAmount)
            .items(itemsDataList)
            .build();
    ctx.commit();
    return output;
  }

  /**
   * Performs the Order-Status read TX profile [TPC-C 2.6].
   *
   * @param ctx The TX context.
   * @param input The input parameters
   * @return The transaction output
   */
  OrderStatusOutput orderStatus(final TPCCContext ctx, final OrderStatusInput input)
      throws NotFoundException,
          EntityNotFoundException,
          SerializationException,
          JsonProcessingException {
    /*
     * [TPC-C 2.6.2.2]
     * For a given customer number (C_W_ID, C_D_ID, C_ID): ...
     */

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
        getCustomerByIDOrLastName(
            ctx, input.getW_id(), input.getD_id(), input.getC_id(), input.getC_last());
    /*
     * [TPC-C 2.6.2.2 (4)]
     * The row in the ORDER table with matching O_W_ID (equals
     * C_W_ID), O_D_ID (equals C_D_ID), O_C_ID (equals C_ID), and with
     * the largest existing O_ID, is selected. This is the most recent
     * order placed by that customer. O_ID, O_ENTRY_D, and
     * O_CARRIER_ID are retrieved.
     */
    final Order order =
        getLastOrderOfCustomer(ctx, customer.getC_w_id(), customer.getC_d_id(), customer.getC_id());

    /*
     * [TPC-C 2.6.2.2 (5)]
     * All rows in the ORDER-LINE table with matching OL_W_ID (equals
     * O_W_ID), OL_D_ID (equals O_D_ID), and OL_O_ID (equals O_ID) are
     * selected and the corresponding sets of OL_I_ID, OL_SUPPLY_W_ID,
     * OL_QUANTITY, OL_AMOUNT, and OL_DELIVERY_D are retrieved.
     */
    final List<OrderLineData> orderLineDataList = new ArrayList<>();
    for (int i = 1; i <= order.getO_ol_cnt(); ++i) {
      final OrderLineData orderLineData = getOrderLineDataForOrder(ctx, order, i);
      logger.debug("Created ORDER-LINE data: {}", orderLineData);
      orderLineDataList.add(orderLineData);
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
            .w_id(input.getW_id())
            .d_id(input.getD_id())
            .fromCustomer(customer)
            .fromOrder(order)
            .order_lines(orderLineDataList)
            .build();
    ctx.commit();
    return output;
  }

  /**
   * Performs the Payment read-write TX profile [TPC-C 2.5].
   *
   * @param ctx The TX context.
   * @param input The input parameters
   * @return The JSON encoded query results according to the specification.
   */
  PaymentOutput payment(final TPCCContext ctx, final PaymentInput input)
      throws EntityNotFoundException,
          EntityExistsException,
          NotFoundException,
          SerializationException,
          JsonProcessingException {
    /*
     * [TPC-C 2.5.2.2]
     * For a given warehouse number (W_ID), district number (D_W_ID,
     * D_ID), customer number (C_W_ID , C_D_ID, C_ ID) or customer last
     * name (C_W_ID, C_D_ID, C_LAST), and payment amount (H_AMOUNT): ...
     *
     */

    final Registry registry = ctx.getRegistry();

    /*
     * [TPC-C 2.5.2.2 (3)]
     * The row in the WAREHOUSE table with matching W_ID is selected.
     * W_NAME, W_STREET_1, W_STREET_2, W_CITY, W_STATE, and W_ZIP are
     * retrieved [...]
     */
    final Warehouse warehouse = Warehouse.builder().id(input.getW_id()).build();
    registry.read(warehouse);
    /*
     * [TPC-C 2.5.2.2 (3) (continued)]
     * ... and W_YTD, the warehouse's year-to-date balance, is
     * increased by H_AMOUNT.
     */
    warehouse.increaseYTD(input.getH_amount());
    registry.update(warehouse);

    /*
     * [TPC-C 2.5.2.2 (4)]
     * The row in the DISTRICT table with matching D_W_ID and D_ID is
     * selected. D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE, and
     * D_ZIP are retrieved [...]
     */
    final District district =
        District.builder().w_id(warehouse.getW_id()).id(input.getD_id()).build();
    registry.read(district);
    /*
     * [TPC-C 2.5.2.2 (4) (continued)]
     * ... and D_YTD, the district's year-to-date balance, is
     * increased by H_AMOUNT.
     */
    district.increaseYTD(input.getH_amount());
    registry.update(district);

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
        getCustomerByIDOrLastName(
            ctx, warehouse.getW_id(), district.getD_id(), input.getC_id(), input.getC_last());
    /*
     * [TPC-C 2.5.2.2 (5.1-2) (continued)]
     * ... C_BALANCE is decreased by H_AMOUNT. [...]
     */
    customer.decreaseBalance(input.getH_amount());
    /*
     * [TPC-C 2.5.2.2 (5.1-2) (continued)]
     * ... C_YTD_PAYMENT is increased by H_AMOUNT. [...]
     */
    customer.increaseYTDPayment(input.getH_amount());
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
      final String historyInfo =
          generateHistoryInformation(customer, warehouse, district, input.getH_amount());
      customer.setC_data(String.format("%s|%s", historyInfo, customer.getC_data()));
      logger.debug("HISTORY information: '{}' inserted at the left of the C_DATA", historyInfo);
      /*
       * [TPC-C 2.5.2.2 (6) (continued)]
       * ... and by discarding the bytes that are shifted out of the
       * right side of the C_DATA field. The content of the C_DATA
       * field never exceeds 500 characters.
       */
      if (customer.getC_data().length() > 500) {
        customer.setC_data(customer.getC_data().substring(0, 500));
      }
    }
    /*
     * [TPC-C 2.5.2.2 (6) (continued)]
     * ... The selected customer is updated with the new C_DATA field.
     */
    registry.update(customer);

    /*
     * [TPC-C 2.5.2.2 (7)]
     * H_DATA is built by concatenating W_NAME and D_NAME separated by
     * 4 spaces.
     */
    final String h_data =
        String.format("%s%s%s", warehouse.getW_name(), "    ", district.getD_name());

    /*
     * [TPC-C 2.5.2.2 (8)]
     * A new row is inserted into the HISTORY table with H_C_ID =
     * C_ID, H_C_D_ID = C_D_ID, H_C_W_ID = C_W_ID, H_D_ID = D_ID, and
     * H_W_ID = W_ID.
     */
    final History history =
        History.builder()
            .fromCustomer(customer)
            .d_id(district.getD_id())
            .w_id(warehouse.getW_id())
            .date(input.getH_date())
            .amount(input.getH_amount())
            .data(h_data)
            .build();
    registry.create(history);

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
            .fromWarehouse(warehouse)
            .fromCustomer(customer)
            .fromDistrict(district)
            .fromHistory(history)
            .build();
    if (customer.getC_credit().equals("BC")) {
      output.setC_data(customer.getC_data().substring(0, 200));
    }
    ctx.commit();
    return output;
  }

  /**
   * Performs the Stock-Level read TX profile [TPC-C 2.8].
   *
   * @param ctx The TX context.
   * @param input The input parameters
   * @return The transaction output
   */
  StockLevelOutput stockLevel(final TPCCContext ctx, final StockLevelInput input)
      throws EntityNotFoundException, NotFoundException, SerializationException {
    /*
     * [TPC-C 2.8.2.2]
     * For a given warehouse number (W_ID), district number (D_W_ID,
     * D_ID), and stock level threshold (threshold): ...
     */

    /*
     * [TPC-C 2.8.2.2 (3)]
     * The row in the DISTRICT table with matching D_W_ID and D_ID is
     * selected and D_NEXT_O_ID is retrieved.
     */
    final District district = District.builder().w_id(input.getW_id()).id(input.getD_id()).build();
    ctx.getRegistry().read(district);

    /*
     * [TPC-C 2.8.2.2 (4)]
     * All rows in the ORDER-LINE table with matching OL_W_ID (equals
     * W_ID), OL_D_ID (equals D_ID), and OL_O_ID (lower than
     * D_NEXT_O_ID and greater than or equal to D_NEXT_O_ID minus 5)
     * are selected. They are the items for 5 recent orders of the
     * district.
     */
    final int o_id_min = Math.max(district.getD_next_o_id() - 5, 0);
    final int o_id_max = district.getD_next_o_id();
    logger.debug("O_ID_MIN={}, O_ID_MAX={}", o_id_min, o_id_max);
    logger.debug("Getting the most recent 5 orders");
    final List<Integer> recentItemIds =
        getItemIdsOfRecentOrders(ctx, input.getW_id(), district.getD_id(), o_id_min, o_id_max);

    /*
     * [TPC-C 2.8.2.2 (5)]
     * All rows in the STOCK table with matching S_I_ID (equals
     * OL_I_ID) and S_W_ID (equals W_ID) from the list of distinct
     * item numbers and with S_QUANTITY lower than threshold are
     * counted (giving low_stock).
     */
    int lowStock = 0;
    for (final int i_id : recentItemIds) {
      final Stock stock = Stock.builder().w_id(input.getW_id()).i_id(i_id).build();
      ctx.getRegistry().read(stock);
      if (stock.getS_quantity() < input.getThreshold()) {
        logger.debug("The stock quantity is less than the threshold");
        ++lowStock;
      }
    }
    logger.debug("lowStock is {}", lowStock);

    /*
     * [TPC-C 2.8.3.3]
     * The emulated terminal must display, in the appropriate field of
     * the input/output screen, all input data and the output data
     * which results from the execution of the transaction.  The
     * following fields are displayed: W_ID, D_ID, threshold, and
     * low_stock.
     */
    final StockLevelOutput output =
        StockLevelOutput.builder().fromInput(input).low_stock(lowStock).build();
    ctx.commit();
    return output;
  }

  /**
   * Creates some dummy initial entities for testing.
   *
   * @param ctx The transaction context
   */
  void init(final TPCCContext ctx) throws EntityExistsException, SerializationException {
    initWarehouses(ctx);
    initDistricts(ctx);
    initCustomers(ctx);
    initItems(ctx);
    initStocks(ctx);

    ctx.commit();
  }

  /**
   * Check whether all elements in <code>arr</code> are equal to the passed <code>value</code>.
   *
   * @param arr The array to check
   * @param value The value to check against
   * @return <code>true</code> if every element of <code>arr</code> is equal to <code>value</code>,
   *     <code>false</code> otherwise
   */
  private static boolean allMatch(final int[] arr, final int value) {
    for (final int x : arr) {
      if (x != value) {
        return false;
      }
    }
    return true;
  }

  /**
   * Pad a district info string to a required length.
   *
   * @param info The district info string
   * @return The string padded to a length of 24 with leading spaces
   * @throws IllegalArgumentException if the info string is already longer than 24 characters
   */
  private static String padDistrictInfo(final String info) {
    if (info.length() > 24) {
      throw new IllegalArgumentException("District info is too long (maximum 24 chars)");
    }

    return String.format("%24s", info);
  }

  /**
   * Initialize WAREHOUSE entities.
   *
   * <p><b>NOTE:</b> this code has been factored out of {@link TPCCContractAPI#init(TPCCContext)}
   * only so that OpenJML won't choke on the exceedingly long method.
   *
   * @param ctx The transaction context
   * @throws EntityExistsException if a warehouse entry already exists on the ledger
   */
  @Loggable(Loggable.DEBUG)
  private void initWarehouses(final ContextWithRegistry ctx)
      throws EntityExistsException, SerializationException {
    final Warehouse warehouse =
        Warehouse.builder()
            .id(1)
            .name("W_One")
            .street_1("xyz")
            .street_2("123")
            .city("Budapest")
            .state("LA")
            .zip("000011111")
            .tax(0.1000)
            .ytd(10000)
            .build();

    ctx.getRegistry().create(warehouse);
  }

  /**
   * Initialize DISTRICTS entities.
   *
   * <p><b>NOTE:</b> this code has been factored out of {@link TPCCContractAPI#init(TPCCContext)}
   * only so that OpenJML won't choke on the exceedingly long method.
   *
   * @param ctx The transaction context
   * @throws EntityExistsException if a district entry already exists on the ledger
   */
  @Loggable(Loggable.DEBUG)
  private void initDistricts(final ContextWithRegistry ctx)
      throws EntityExistsException, SerializationException {
    final District district =
        District.builder()
            .id(1)
            .w_id(1)
            .name("D_One")
            .street_1("abc")
            .street_2("456")
            .city("Budapest")
            .state("BP")
            .zip("000111111")
            .tax(0.0100)
            .ytd(10000)
            .next_o_id(3001)
            .build();

    ctx.getRegistry().create(district);
  }

  /**
   * Initialize CUSTOMER entities.
   *
   * <p><b>NOTE:</b> this code has been factored out of {@link TPCCContractAPI#init(TPCCContext)}
   * only so that OpenJML won't choke on the exceedingly long method.
   *
   * @param ctx The transaction context
   * @throws EntityExistsException if a customer entry already exists on the ledger
   */
  @Loggable(Loggable.DEBUG)
  private void initCustomers(final ContextWithRegistry ctx)
      throws EntityExistsException, SerializationException {
    final Customer alice =
        Customer.builder()
            .id(1)
            .d_id(1)
            .w_id(1)
            .first("Alice")
            .middle("IS")
            .last("Yong")
            .street_1("xyz")
            .street_2("123")
            .city("Budapest")
            .state("HU")
            .zip("000101111")
            .phone("1234567890123456")
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
    final Customer peter =
        Customer.builder()
            .id(2)
            .d_id(1)
            .w_id(1)
            .first("Peter")
            .middle("XX")
            .last("Peter")
            .street_1("ABC")
            .street_2("23")
            .city("Budapest")
            .state("DC")
            .zip("000011111")
            .phone("6123456789012345")
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

    final Registry registry = ctx.getRegistry();
    registry.create(alice);
    registry.create(peter);
  }

  /**
   * Initialize ITEM entities.
   *
   * <p><b>NOTE:</b> this code has been factored out of {@link TPCCContractAPI#init(TPCCContext)}
   * only so that OpenJML won't choke on the exceedingly long method.
   *
   * @param ctx The transaction context
   * @throws EntityExistsException if an item entry already exists on the ledger
   */
  @Loggable(Loggable.DEBUG)
  private void initItems(final ContextWithRegistry ctx)
      throws EntityExistsException, SerializationException {
    final Item cup =
        Item.builder().id(1).im_id(123).name("Cup").price(99.50).data("ORIGINAL").build();
    final Item plate =
        Item.builder().id(2).im_id(234).name("Plate").price(89.50).data("ORIGINAL").build();
    final Item glass =
        Item.builder().id(3).im_id(456).name("Glass").price(78.00).data("GENERIC").build();

    final Registry registry = ctx.getRegistry();
    registry.create(cup);
    registry.create(plate);
    registry.create(glass);
  }

  /**
   * Initialize STOCK entities.
   *
   * <p><b>NOTE:</b> this code has been factored out of {@link TPCCContractAPI#init(TPCCContext)}
   * only so that OpenJML won't choke on the exceedingly long method.
   *
   * @param ctx The transaction context
   * @throws EntityExistsException if a stock entry already exists on the ledger
   */
  @Loggable(Loggable.DEBUG)
  private void initStocks(final ContextWithRegistry ctx)
      throws EntityExistsException, SerializationException {
    final Stock stock1 =
        Stock.builder()
            .i_id(1)
            .w_id(1)
            .quantity(100)
            .dist_all(padDistrictInfo("null"))
            .dist_01(padDistrictInfo("good"))
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
            .dist_all(padDistrictInfo("null"))
            .dist_01(padDistrictInfo("good"))
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
            .dist_all(padDistrictInfo("null"))
            .dist_01(padDistrictInfo("good"))
            .ytd(0)
            .order_cnt(0)
            .remote_cnt(0)
            .data("GENERIC")
            .build();

    final Registry registry = ctx.getRegistry();
    registry.create(stock1);
    registry.create(stock2);
    registry.create(stock3);
  }

  /**
   * Generate history information as per [TPC-C 2.5.2.2 (6)].
   *
   * <p><b>NOTE:</b> this code has been factored out of {@link TPCCContractAPI#payment(TPCCContext,
   * String)} only so that OpenJML won't choke on the exceedingly long method.
   *
   * @param customer The relevant customer entity
   * @param warehouse The relevant warehouse entity
   * @param district the relevant district entity
   * @param h_amount The relevant <code>H_AMOUNT</code> value
   * @return A history information string from the parameters
   */
  @Loggable(Loggable.DEBUG)
  private static String generateHistoryInformation(
      final Customer customer,
      final Warehouse warehouse,
      final District district,
      final double h_amount) {
    return String.format("%d %d %d %d %d %g",
            customer.getC_id(),
            customer.getC_d_id(),
            customer.getC_w_id(),
            district.getD_id(),
            warehouse.getW_id(),
            h_amount);
  }

  /**
   * Builds an {@link OrderLineData} instance from an {@link Order} an order <code>number</code>.
   *
   * <p><b>NOTE:</b> this code has been factored out of {@link
   * TPCCContractAPI#orderStatus(TPCCContext, String)} only so that OpenJML won't choke on the
   * exceedingly long method.
   *
   * @param ctx The transaction context
   * @param order The order entity to build from
   * @param number The order number
   * @return The {@link OrderLineData} built
   */
  @Loggable(Loggable.DEBUG)
  private OrderLineData getOrderLineDataForOrder(
      final ContextWithRegistry ctx, final Order order, final int number)
      throws EntityNotFoundException, SerializationException, JsonProcessingException {
    final OrderLine orderLine = OrderLine.builder().fromOrder(order).number(number).build();
    ctx.getRegistry().read(orderLine);

    return OrderLineData.builder().fromOrderLine(orderLine).build();
  }

  /**
   * Retrieves the oldest NEW-ORDER entry for a given warehouse and district.
   *
   * <p><b>NOTE:</b> this code has been factored out of {@link TPCCContractAPI#delivery(TPCCContext,
   * String)} only so that OpenJML won't choke on the exceedingly long method.
   *
   * @param ctx The transaction context
   * @param w_id The warehouse's ID
   * @param d_id The district's ID
   * @param o_carrier_id The relevant carrier ID
   * @param ol_delivery_d The delivery date
   * @return The oldest NEW-ORDER entry with matching parameters
   */
  @Loggable(Loggable.DEBUG)
  private DeliveredOrder deliverOldestNewOrderForDistrict(
      final ContextWithRegistry ctx,
      final int w_id,
      final int d_id,
      final int o_carrier_id,
      final String ol_delivery_d)
      throws EntityNotFoundException, SerializationException, JsonProcessingException {
    final Registry registry = ctx.getRegistry();

    /*
     * [TPC-C 2.7.4.2 (3)]
     * The row in the NEW-ORDER table with matching NO_W_ID (equals
     * W_ID) and NO_D_ID (equals D_ID) and with the lowest NO_O_ID
     * value is selected.  This is the oldest undelivered order of
     * that district.  NO_O_ID, the order number, is retrieved. [...]
     */
    final List<NewOrder> matchingNewOrders =
        registry
            .select(new NewOrder())
            /* TODO this code causes a StackOverflowError for some reason
            .matching(
                new Registry.Matcher<NewOrder>() {
                  @Override
                  public boolean match(NewOrder entity) {
                    return entity.getNo_w_id() == w_id && entity.getNo_d_id() == d_id;
                  }
                })
            */
            .sortedBy(new NewOrderComparator())
            .get();
    /* Manually remove non-matching NewOrders, see above... */
    final Iterator<NewOrder> it = matchingNewOrders.iterator();
    while (it.hasNext()) {
      final NewOrder no = it.next();
      if (no.getNo_w_id() != w_id || no.getNo_d_id() != d_id) {
        it.remove();
      }
    }
    logger.debug("matchingNewOrders={}", matchingNewOrders);

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
      return null;
    }
    final NewOrder oldestNewOrder =
        matchingNewOrders.get(0); // after the sorting, the first one is the oldest
    logger.debug("Oldest NEW-ORDER retrieved is: {}", oldestNewOrder);

    /*
     * [TPC-C 2.7.4.2 (4)]
     * The selected row in the NEW-ORDER table is deleted.
     */
    registry.delete(oldestNewOrder);

    /*
     * [TPC-C 2.7.4.2 (5)]
     * The row in the ORDER table with matching O_W_ID (equals W_ ID),
     * O_D_ID (equals D_ID), and O_ID (equals NO_O_ID) is selected,
     * O_C_ID, the customer number, is retrieved, [...]
     */
    final Order order =
        Order.builder().w_id(w_id).d_id(d_id).id(oldestNewOrder.getNo_o_id()).build();
    registry.read(order);
    /*
     * [TPC-C 2.7.4.2 (5) (continued)]
     * ... and O_CARRIER_ID is updated.
     */
    order.setO_carrier_id(o_carrier_id);
    registry.update(order);

    /*
     * [TPC-C 2.7.4.2 (6)]
     * All rows in the ORDER-LINE table with matching OL_W_ID (equals
     * O_W_ID), OL_D_ID (equals O_D_ID), and OL_O_ID (equals O_ID) are
     * selected. [...]
     */
    double orderLineAmountTotal = 0;
    for (int i = 1; i <= order.getO_ol_cnt(); ++i) {
      orderLineAmountTotal +=
          getOrderLineAmountAndUpdateTime(ctx, w_id, d_id, order.getO_id(), i, ol_delivery_d);
    }

    /*
     * [TPC-C 2.7.4.2 (7)]
     * The row in the CUSTOMER table with matching C_W_ID (equals
     * W_ID), C_D_ID (equals D_ID), and C_ID (equals O_C_ID) is
     * selected [...]
     */
    final Customer customer =
        Customer.builder().w_id(w_id).d_id(d_id).id(order.getO_c_id()).build();
    registry.read(customer);
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
    registry.update(customer);

    return DeliveredOrder.builder().d_id(d_id).o_id(order.getO_id()).build();
  }

  /**
   * Get the OL_AMOUNT field of a matching ORDER-LINE entity.
   *
   * <p><b>SIDE EFFECTS:</b> As per [TPC-C 2.7.4.2 (6)], the OL_DELIVERY_D values of the matching
   * records are updated.
   *
   * <p><b>NOTE:</b> this code has been factored out of {@link TPCCContractAPI#delivery(TPCCContext,
   * String)} (and then consequently from {@link
   * TPCCBusinessAPI#deliverOldestNewOrderForDistrict(ContextWithRegistry, int, int, int, String)})
   * only so that OpenJML won't choke on the exceedingly long method.
   *
   * @param ctx The transaction context
   * @param w_id The warehouse's ID
   * @param d_id The district's ID
   * @param o_id The order's ID
   * @param number The order's number
   * @param ol_delivery_d The delivery date
   * @return The OL_AMOUNT field of the matching ORDER-LINE
   */
  @Loggable(Loggable.DEBUG)
  private double getOrderLineAmountAndUpdateTime(
      final ContextWithRegistry ctx,
      final int w_id,
      final int d_id,
      final int o_id,
      final int number,
      final String ol_delivery_d)
      throws EntityNotFoundException, SerializationException {
    /*
     * [TPC-C 2.7.4.2 (6)]
     * All rows in the ORDER-LINE table with matching OL_W_ID (equals
     * O_W_ID), OL_D_ID (equals O_D_ID), and OL_O_ID (equals O_ID) are
     * selected. [...]
     */
    final OrderLine orderLine =
        OrderLine.builder().w_id(w_id).d_id(d_id).o_id(o_id).number(number).build();
    ctx.getRegistry().read(orderLine);
    /*
     * [TPC-C 2.7.4.2 (6) (continued)]
     * ... All OL_DELIVERY_D, the delivery dates, are updated to the
     * current system time as returned by the operating system [...]
     */
    orderLine.setOl_delivery_d(ol_delivery_d);
    /*
     * [TPC-C 2.7.4.2 (6) (continued)]
     * ... and the sum of all OL_AMOUNT is retrieved.
     */
    ctx.getRegistry().update(orderLine);

    return orderLine.getOl_amount();
  }

  /**
   * Get the OL_AMOUNT field of a matching ORDER-LINE entity.
   *
   * <h2>SIDE EFFECTS</h2>
   *
   * <p>As per [TPC-C 2.4.2.2 (8)], the relevant STOCK entity is changed in the following manner:
   *
   * <ul>
   *   <li>the value of <code>S_QUANTITY</code> is either decreased by <code>i_qty</code> or
   *       decreased by <code>i_qty</code> and then increased by a value of <code>91</code>
   *   <li>the value of the <code>S_YTD</code> field is increased by <code>i_qty</code>
   *   <li>the value of <code>S_ORDER_CNT</code> is incremented
   *   <li>if the item must be retrieved from another warehouse (<code>w_id</code> is not <code>
   *       i_w_id</code>), <code>S_REMOTE_CNT</code> is incremented
   * </ul>
   *
   * A new {@link ItemsData} entry is added to the collection passed as <code>itemsDataCollection
   * </code>. Finally, a new ORDER-LINE entity is created (but this is less of a side effect than
   * the main purpose of this method).
   *
   * <p><b>NOTE:</b> this code has been factored out of {@link TPCCContractAPI#newOrder(TPCCContext,
   * String)} only so that OpenJML won't choke on the exceedingly long method.
   *
   * @param ctx The transaction context
   * @param i_id The item's ID
   * @param i_w_id The item's warehouse's ID
   * @param i_qty The item quantity
   * @param w_id The warehouse's ID
   * @param d_id The district's ID
   * @param nextOrderId The next order ID
   * @param number The order number
   * @param itemsDataCollection The {@link ItemsData} collection to add an entry into
   * @return The OL_AMOUNT field of the resulting ORDER-LINE
   */
  @Loggable(Loggable.DEBUG)
  private double createOrderLineAndGetAmount(
      final ContextWithRegistry ctx,
      final Item item,
      final int i_id,
      final int i_w_id,
      final int i_qty,
      final int w_id,
      final int d_id,
      final int nextOrderId,
      final int number,
      final Collection<ItemsData> itemsDataCollection)
      throws EntityNotFoundException, EntityExistsException, SerializationException {
    final Registry registry = ctx.getRegistry();

    /*
     * [TPC-C 2.4.2.2 (8.2)]
     * The row in the STOCK table with matching S_I_ID (equals
     * OL_I_ID) and S_W_ID (equals OL_SUPPLY_W_ID) is selected.
     * S_QUANTITY, the quantity in stock, S_DIST_xx, where xx
     * represents the district number, and S_DATA are retrieved.
     * [...]
     */
    final Stock stock = Stock.builder().w_id(i_w_id).i_id(i_id).build();
    registry.read(stock);
    /*
     * [TPC-C 2.4.2.2 (8.2) (continued)]
     * ... If the retrieved value for S_QUANTITY exceeds OL_QUANTITY
     * by 10 or more, then S_QUANTITY is decreased by OL_QUANTITY;
     * otherwise S_QUANTITY is updated to (S_QUANTITY - OL_QUANTITY)
     * + 91. [...]
     */
    if (stock.getS_quantity() >= i_qty + 10) {
      stock.decreaseQuantity(i_qty);
    } else {
      stock.setS_quantity(stock.getS_quantity() - i_qty + 91);
    }
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
    if (i_w_id != w_id) {
      stock.incrementRemoteCount();
    }
    registry.update(stock);

    /*
     * [TPC-C 2.4.2.2 (8.3)]
     * The amount for the item in the order (OL_AMOUNT) is computed
     * as: OL_QUANTITY * I_PRICE
     */
    final double orderLineAmount = i_qty * item.getI_price();

    /*
     * [TPC-C 2.4.2.2 (8.4)]
     * The strings in I_DATA and S_DATA are examined.  If they both
     * include the string "ORIGINAL", the brand-generic field for
     * that item is set to "B", otherwise, the brand-generic field
     * is set to "G".
     */
    final String brandGeneric =
        item.getI_data().contains("ORIGINAL") && stock.getS_data().contains("ORIGINAL") ? "B" : "G";

    /*
     * [TPC-C 2.4.2.2 (8.5)]
     * A new row is inserted into the ORDER-LINE table to reflect
     * the item on the order.  OL_DELIVERY_D is set to a null value,
     * OL_NUMBER is set to a unique value within all the ORDER-LINE
     * rows that have the same OL_O_ID value, and OL_DIST_INFO is
     * set to the content of S_DIST_xx, where xx represents the
     * district number (OL_D_ID)
     */
    final OrderLine orderLine =
        OrderLine.builder()
            .o_id(nextOrderId)
            .d_id(d_id)
            .w_id(w_id)
            .number(number + 1)
            .i_id(i_id)
            .supply_w_id(i_w_id)
            .delivery_d(null)
            .quantity(i_qty)
            .amount(orderLineAmount)
            .dist_info(padDistrictInfo(stock.getS_dist(d_id)))
            .build();
    registry.create(orderLine);

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
    final ItemsData itemsData =
        ItemsData.builder()
            .ol_supply_w_id(orderLine.getOl_supply_w_id())
            .ol_i_id(orderLine.getOl_i_id())
            .i_name(item.getI_name())
            .ol_quantity(orderLine.getOl_quantity())
            .s_quantity(stock.getS_quantity())
            .brand_generic(brandGeneric)
            .i_price(item.getI_price())
            .ol_amount(orderLine.getOl_amount())
            .build();
    itemsDataCollection.add(itemsData);
    logger.debug("Created ItemsData: {}", itemsData);

    return orderLineAmount;
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
   * @throws IllegalArgumentException if neither the customer ID nor the customer last name
   *     parameter is supplied
   */
  @Loggable(Loggable.DEBUG)
  private Customer getCustomerByIDOrLastName(
      final ContextWithRegistry ctx,
      final int c_w_id,
      final int c_d_id,
      final Integer c_id,
      final String c_last)
      throws EntityNotFoundException,
          NotFoundException,
          SerializationException,
          JsonProcessingException {
    if (c_id == null && c_last == null) {
      throw new IllegalArgumentException("At least one of c_id and c_last must be specified");
    }

    if (c_id != null) {
      final Customer customer =
          ctx.getRegistry().read(Customer.builder().w_id(c_w_id).d_id(c_d_id).id(c_id).build());
      return customer;
    } else {
      final List<Customer> allCustomers =
          ctx.getRegistry().readAll(Customer.builder().w_id(c_w_id).d_id(c_d_id).build());

      // Stream-based one-liner replaced with below code to accommodate OpenJML...
      final List<Customer> matchingCustomers = new ArrayList<>();
      for (final Customer c : allCustomers) {
        if (c.getC_last().equals(c_last)) {
          matchingCustomers.add(c);
        }
      }
      if (matchingCustomers.isEmpty()) {
        throw new NotFoundException(String.format("Customer matching last name '%s' not found", c_last));
      }
      matchingCustomers.sort(new CustomerComparator());

      final double N = Math.ceil(matchingCustomers.size() / 2d);
      if (N > Integer.MAX_VALUE) {
        logger.warn("Size of matching CUSTOMER list is out of range");
      }
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
   * @throws NotFoundException if the order is not found
   */
  @Loggable(Loggable.DEBUG)
  private Order getLastOrderOfCustomer(
      final ContextWithRegistry ctx, final int o_w_id, final int o_d_id, final int o_c_id)
      throws NotFoundException, SerializationException, JsonProcessingException {
    Registry registry = ctx.getRegistry();
    final List<Order> allOrders =
        registry.readAll(Order.builder().w_id(o_w_id).d_id(o_d_id).build());
    if (allOrders.isEmpty()) {
      throw new NotFoundException("No orders found");
    }

    // Stream-based one-liner replaced with below code to accommodate OpenJML...
    final List<Order> matchingOrders = new ArrayList<>();
    for (final Order o : allOrders) {
      if (o.getO_c_id() == o_c_id) {
        matchingOrders.add(o);
      }
    }
    if (matchingOrders.isEmpty()) {
      throw new NotFoundException("Could not find last order of customer");
    }
    matchingOrders.sort(new OrderComparator());

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
  @Loggable(Loggable.DEBUG)
  private List<Integer> getItemIdsOfRecentOrders(
      final ContextWithRegistry ctx,
      final int w_id,
      final int d_id,
      final int o_id_min,
      final int o_id_max)
      throws EntityNotFoundException, NotFoundException, SerializationException {
    final Set<Integer> itemIds = new HashSet<>();
    for (int current_o_id = o_id_min; current_o_id < o_id_max; current_o_id++) {
      final Order order = Order.builder().w_id(w_id).d_id(d_id).id(current_o_id).build();

      try {
        ctx.getRegistry().read(order);
      } catch (EntityNotFoundException _e) {
        logger.warn(
            "Order with o_id={} not found while looking up recent orders; ignoring", current_o_id);
        continue;
      }

      for (int ol_number = 1; ol_number <= order.getO_ol_cnt(); ol_number++) {
        final OrderLine orderLine =
            OrderLine.builder().w_id(w_id).d_id(d_id).o_id(current_o_id).number(ol_number).build();
        ctx.getRegistry().read(orderLine);
        itemIds.add(orderLine.getOl_i_id());
      }
    }
    if (itemIds.isEmpty()) {
      throw new NotFoundException("Could not find item IDs of recent ORDERs");
    }

    return new ArrayList<>(itemIds);
  }

  @Loggable(Loggable.DEBUG)
  private static final class NewOrderComparator implements Comparator<NewOrder> {

    @Override
    public int compare(final NewOrder a, final NewOrder b) {
      return a.getNo_o_id() - b.getNo_o_id();
    }
  }

  @Loggable(Loggable.DEBUG)
  private static final class CustomerComparator implements Comparator<Customer> {

    @Override
    public int compare(final Customer a, final Customer b) {
      return a.getC_last().compareTo(b.getC_last());
    }
  }

  @Loggable(Loggable.DEBUG)
  private static final class OrderComparator implements Comparator<Order> {

    @Override
    public int compare(final Order a, final Order b) {
      return a.getO_id() - b.getO_id();
    }
  }
}
