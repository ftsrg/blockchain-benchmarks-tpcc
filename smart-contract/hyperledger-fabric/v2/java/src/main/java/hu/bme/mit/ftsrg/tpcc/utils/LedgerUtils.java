/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.utils;

import com.jcabi.aspects.Loggable;
import hu.bme.mit.ftsrg.tpcc.entities.Customer;
import hu.bme.mit.ftsrg.tpcc.entities.Order;
import hu.bme.mit.ftsrg.tpcc.entities.OrderLine;
import hu.bme.mit.ftsrg.tpcc.stub.EnhancedContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility functions for accessing the state database. */
@Loggable(Loggable.DEBUG)
@UtilityClass
public final class LedgerUtils {

  private static final Logger logger = LoggerFactory.getLogger(LedgerUtils.class);

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
  public static Customer getCustomersByIdOrLastName(
      final EnhancedContext ctx,
      final int c_w_id,
      final int c_d_id,
      final Integer c_id,
      final String c_last)
      throws Exception {
    if (c_id == null && c_last == null) {
      throw new Exception("Customer ID or customer C_LAST should be given");
    }

    if (c_id != null) {
      final Customer custByID = Customer.builder().w_id(c_w_id).d_id(c_d_id).id(c_id).build();
      // Case 1, the customer is selected based on customer number: the row in the CUSTOMER
      // table with matching C_W_ID, C_D_ID, and C_ID is selected and C_BALANCE, C_FIRST,
      // C_MIDDLE, and C_LAST are retrieved.
      return ctx.registry.read(ctx, custByID);
    } else {
      // Case 2, the customer is selected based on customer last name: all rows in the
      // CUSTOMER table with matching C_W_ID, C_D_ID and C_LAST are selected sorted by
      // C_FIRST in ascending order. Let n be the number of rows selected. C_BALANCE,
      // C_FIRST, C_MIDDLE, and C_LAST are retrieved from the row at position n/ 2 rounded up
      // in the sorted set of selected rows from the CUSTOMER table.
      final Customer custByLastName = Customer.builder().w_id(c_w_id).d_id(c_d_id).build();
      final List<Customer> customerList = ctx.registry.readAll(ctx, custByLastName);

      if (customerList.isEmpty()) {
        throw new RuntimeException("Could not find any Customers");
      }
      for (final Customer cust : customerList) {
        if (cust.getC_last().equals(c_last)) {
          return cust;
        }
      }
    }

    return null;
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
  public static Order getLastOrderOfCustomer(
      final EnhancedContext ctx, final int o_w_id, final int o_d_id, final int o_c_id)
      throws Exception {
    final Order custOrder = Order.builder().w_id(o_w_id).d_id(o_d_id).build();
    final List<Order> allOrders = ctx.registry.readAll(ctx, custOrder);
    if (allOrders == null) {
      throw new Exception(
          String.format(
              "Could not find last Order(%d, %d, o_id) of Customer(%d, %d, %d)",
              o_w_id, o_d_id, o_w_id, o_d_id, o_c_id));
    }
    final List<Order> ordersOfCust = new ArrayList<>();
    for (final Order order : allOrders) {
      if (order.getO_c_id() == o_c_id) {
        ordersOfCust.add(order);
      }
    }
    // TODO sort
    return ordersOfCust.get(0);
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
  public static List<Integer> getItemIdsOfRecentOrders(
      final EnhancedContext ctx,
      final int w_id,
      final int d_id,
      final int o_id_min,
      final int o_id_max)
      throws Exception {
    final Set<Integer> itemIds = new HashSet<>();
    for (int current_o_id = o_id_min; current_o_id < o_id_max; current_o_id++) {

      final Order order = Order.builder().w_id(w_id).d_id(d_id).id(current_o_id).build();
      ctx.registry.read(ctx, order);
      logger.debug("Retrieved ORDER: " + order);

      for (int ol_number = 1; ol_number <= order.getO_ol_cnt(); ol_number++) {
        final OrderLine orderLine =
            OrderLine.builder().w_id(w_id).d_id(d_id).o_id(current_o_id).number(ol_number).build();
        ctx.registry.read(ctx, orderLine);
        logger.debug("Retrieved ORDER-LINE: " + orderLine);
        itemIds.add(orderLine.getOl_i_id());
        logger.info("Retrieved ITEM ids: " + itemIds);
        for (Integer strCurrentNumber : itemIds) {
          System.out.println(strCurrentNumber);
        }
      }
    }

    if (itemIds.isEmpty()) {
      throw new Exception(
          String.format(
              "Could not find item IDs of recent Orders(%d, %d, [%d, %d))",
              w_id, d_id, o_id_min, o_id_max));
    }

    return new ArrayList<>(itemIds);
  }
}
