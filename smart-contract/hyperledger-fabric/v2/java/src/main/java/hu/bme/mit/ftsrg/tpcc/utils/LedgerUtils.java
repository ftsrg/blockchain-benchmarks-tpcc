/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.utils;

import hu.bme.mit.ftsrg.tpcc.TPCC;
import hu.bme.mit.ftsrg.tpcc.entries.*;
import hu.bme.mit.ftsrg.tpcc.utils.Common.TABLES;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import lombok.experimental.UtilityClass;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;

/** Utility functions for accessing the state database. */
@UtilityClass
public final class LedgerUtils {
  private static final Logger LOGGER = Logger.getLogger(TPCC.class.getName());

  /** LOW-LEVEL API */

  /**
   * Adds a new record to the state database. Throws an error if it already exists.
   *
   * @param ctx The TX context.
   * @param type The type of the record.
   * @param keyParts The parts of the record's primary key.
   * @param entry The JSON string/object representation of the record.
   * @throws Exception
   */
  public static void createEntry(
      final Context ctx, final String type, final String[] keyParts, final Object entry)
      throws Exception {
    LOGGER.info("Begin createEntry for input " + type + " and " + keyParts);

    final CompositeKey key = ctx.getStub().createCompositeKey(type, keyParts);
    LOGGER.info("Created composite key " + key.toString());

    final String entryString = entry instanceof String ? (String) entry : JSON.serialize(entry);
    final byte[] buffer = entryString.getBytes(StandardCharsets.UTF_8);
    LOGGER.info("Entry string " + entryString + " to bytes " + buffer);

    // final long start = System.currentTimeMillis();
    ctx.getStub().putState(key.toString(), buffer);

    LOGGER.info(
        "createEntry() Created entry for key "
            + key.toString()
            + " With value: "
            + new String(buffer, StandardCharsets.UTF_8));
  }

  /**
   * Adds a new record to the state database. Throws an error if it already exists.
   *
   * @param ctx The TX context.
   * @param type The type of the record.
   * @param keyParts The parts of the record's primary key.
   * @param entry The JSON string/object representation of the record.
   * @throws Exception
   */
  public static void updateEntry(
      final Context ctx, final String type, final String[] keyParts, final Object entry)
      throws Exception {
    LOGGER.info("Beginning updateEntry for type " + type + " " + keyParts);

    final CompositeKey key = ctx.getStub().createCompositeKey(type, keyParts);
    LOGGER.info("COMPOSITE KEY: " + key.toString());

    final String entryString = entry instanceof String ? (String) entry : JSON.serialize(entry);
    final byte[] buffer = entryString.getBytes(StandardCharsets.UTF_8);
    ctx.getStub().putState(key.toString(), buffer);

    LOGGER.info(
        "UpdateEntry complete for Key "
            + key.toString()
            + " Value: "
            + new String(buffer, StandardCharsets.UTF_8));
  }

  /**
   * Retrieves the given record from the state database.
   *
   * @param ctx The TX context.
   * @param type The type of the record.
   * @param keyParts The attribute values as key parts.
   * @return The retrieved data or null if not found.
   */
  public static String getEntry(final Context ctx, final String type, final String[] keyParts)
      throws Exception {
    final CompositeKey key = ctx.getStub().createCompositeKey(type, keyParts);
    LOGGER.info("LedgerUtils.getEntry for COMPOSITE KEY: " + key.toString());

    // final long start = System.currentTimeMillis();
    final byte[] data = ctx.getStub().getState(key.toString());
    // final long end = System.currentTimeMillis();
    final String datastr = new String(data, StandardCharsets.UTF_8);
    // ctx.getTxinfo().stat_get_cnt += 1;
    // ctx.getTxinfo().stat_get_exec_total_ms += end - start;
    LOGGER.info("getEntry bytes retieved is " + datastr + "For key " + key.toString());
    if (data.length > 0) {
      // ctx.getTxinfo().stat_get_size_bytes += data.length;

      final String entry = new String(data, StandardCharsets.UTF_8);
      LOGGER.info("LedgerUtils.getEntry Retrieved entry " + entry);
      return entry;
    }
    LOGGER.info("getEntry for: " + key.toString() + " FAILED");
    return null;
  }

  /**
   * Deletes the given record from the state database.
   *
   * @param ctx The TX context.
   * @param type The type of the record.
   * @param keyParts The attribute values as key parts.
   * @throws Exeption
   */
  public static void deleteEntry(final Context ctx, final String type, final String[] keyParts)
      throws Exception {
    final CompositeKey key = ctx.getStub().createCompositeKey(type, keyParts);

    // final long start = System.currentTimeMillis();
    ctx.getStub().delState(key.toString());
    LOGGER.info("Deleted state for entry with COMPOSITE KEY: " + key.toString());
    // final long end = System.currentTimeMillis();

    // ctx.getTxinfo().stat_del_cnt += 1;
    // ctx.getTxinfo().stat_del_exec_total_ms += end - start;
    // log("Deleted entry: " + key, ctx);
  }

  /**
   * Retrieves entries from the state database that matches certain criteria.
   *
   * @param ctx The TX context.
   * @param type The type of entries to search.
   * @param keyParts The parts of the entries private key.
   * @param matchData Data structure that determines what to match.
   * @param firstMatch Indicates whether only the first match should be returned.
   * @return The retrieved entries.
   * @throws Exception
   */
  public static List<Object> select(
      final Context ctx,
      final String type,
      final String[] keyParts,
      final MatchData matchData,
      final boolean firstMatch)
      throws Exception {
    LOGGER.info("Start of select function:");

    final CompositeKey compositeKey = ctx.getStub().createCompositeKey(type, keyParts);
    LOGGER.info("Reading ledger using COMPOSITE KEY: " + compositeKey.toString());

    final Iterator<KeyValue> iterator =
        ctx.getStub().getStateByPartialCompositeKey(compositeKey.toString()).iterator();
    LOGGER.info("iterator: " + iterator);

    final List<Object> matches = new ArrayList<>();
    // int retrieved = 0;
    try {
      LOGGER.info("iteration to retrieve next entry");
      while (iterator.hasNext()) {
        LOGGER.info("Begin while loop");
        final KeyValue res = iterator.next();
        if (res == null) {
          LOGGER.info("ERROR: NULL ITERATOR");
          break;
        }
        // retrieved += 1;
        final byte[] buffer = res.getValue();
        final String entry = new String(buffer, StandardCharsets.UTF_8);
        LOGGER.info("Enumerated entry:" + entry);

        Object match = null;
        switch (matchData.type) {
          case CUSTOMER_LAST_NAME:
            final Customer customer = ParseUtils.parseCustomer(entry);
            if (customer.getC_last().equals(matchData.c_last)) {
              LOGGER.info(
                  "Customer's last name "
                      + customer.getC_last()
                      + "== "
                      + "Last Name"
                      + matchData.c_last);
              match = customer;
            } else {
              LOGGER.info(
                  "Customer's last name "
                      + customer.getC_last()
                      + "!= to"
                      + "Last Name"
                      + matchData.c_last);
            }
            LOGGER.info("SELECT() CASE CUSTOMER_LAST_NAME");
            break;

          case ORDER_CUSTOMER_ID:
            final Order order = ParseUtils.parseOrder(entry);
            if (order.getO_c_id() == matchData.o_c_id) {
              match = order;
            }
            LOGGER.info("SELECT() CASE ORDER_CUSTOMER_ID");
            break;

          case PARSEABLE_NEWORDER:
            match = ParseUtils.parseNewOrder(entry);
            LOGGER.info("SELECT() CASE PARSEABLE_NEWORDER");
            break;
        }
        if (match != null) {
          LOGGER.info("Add matches to list");
          matches.add(match);
          if (firstMatch) {
            break;
          }
        }
      }
      LOGGER.info("Exit SELECT()");
    } catch (Exception e) {
      Common.log(e.toString(), ctx, "error");
      throw e;
    }
    // return firstMatch ? matches.subList(0, 1) : matches;
    return matches;
  }

  /** WAREHOUSE API */

  /**
   * Adds a new warehouse to the state database.
   *
   * @param ctx The TX context.
   * @param entry The JSON string.
   * @throws Exception
   */
  public static void createWarehouse(final Context ctx, final Object entry) throws Exception {
    LOGGER.info("Starting create warehouse with entry " + entry);

    final Warehouse warehouse =
        entry instanceof String ? ParseUtils.parseWarehouse((String) entry) : (Warehouse) entry;

    LOGGER.info("Call to createEntry() for warehouse with ID " + warehouse.getW_id());
    LedgerUtils.createEntry(
        ctx, TABLES.WAREHOUSE, new String[] {Common.pad(warehouse.getW_id())}, entry);
    LOGGER.info("createEntry() for warehouse " + warehouse.getW_id() + "COMPLETE!!!!");
  }

  /**
   * Retrieves a warehouse record from the ledger.
   *
   * @param ctx The TX context.
   * @param w_id The warehouse ID.
   * @return The retrieved warehouse.
   */
  public static Warehouse getWarehouse(final Context ctx, final int w_id) throws Exception {
    LOGGER.info(
        "LedgerUtils.getWarehouse: Attempt to retrieve warehouse details  for warehouse: " + w_id);
    final String entry =
        LedgerUtils.getEntry(ctx, TABLES.WAREHOUSE, new String[] {Common.pad(w_id)});

    if (entry == null) {
      throw new Exception("Could not retrieve Warehouse(" + w_id + ")");
    }
    LOGGER.info("LedgerUtils.getWarehouse: retrieved warehouse details " + entry);
    return entry != null ? ParseUtils.parseWarehouse(entry) : null;
  }

  /**
   * Updates a warehouse in the state database.
   *
   * @param ctx The TX context.
   * @param entry The warehouse object.
   * @throws Exception
   */
  public static void updateWarehouse(final Context ctx, final Warehouse entry) throws Exception {
    LOGGER.info("Begin update Warehouse ");
    LedgerUtils.updateEntry(
        ctx, TABLES.WAREHOUSE, new String[] {Common.pad(entry.getW_id())}, entry);
    LOGGER.info("updateWarehouse COMPLETE!!!");
  }

  /** DISTRICT API */

  /**
   * Adds a new district to the state database.
   *
   * @param ctx The TX context.
   * @param entry The JSON string or District object.
   * @throws Exception
   */
  public static void createDistrict(final Context ctx, final Object entry) throws Exception {
    LOGGER.info("Begin create entry for District with entry " + entry);
    final District district =
        entry instanceof String ? ParseUtils.parseDistrict((String) entry) : (District) entry;
    LedgerUtils.createEntry(
        ctx,
        TABLES.DISTRICT,
        new String[] {Common.pad(district.getD_w_id()), Common.pad(district.getD_id())},
        entry);
    LOGGER.info("CreateDistrict SUCCESS!!!");
  }

  /**
   * Retrieves a district record from the ledger.
   *
   * @param ctx The TX context.
   * @param d_w_id The warehouse ID.
   * @param d_id The district ID.
   * @return The retrieved district.
   */
  public static District getDistrict(final Context ctx, final int d_w_id, final int d_id)
      throws Exception {
    final String[] keyParts = new String[] {Common.pad(d_w_id), Common.pad(d_id)};
    LOGGER.info("Begin getDistrict with key " + keyParts);

    final String entry = LedgerUtils.getEntry(ctx, TABLES.DISTRICT, keyParts);
    if (entry == null) {
      throw new Exception("Could not retrieve District(" + d_w_id + ", " + d_id + ")");
    }
    LOGGER.info("getDistrict for " + keyParts + " returned with entry " + entry);
    return entry != null ? ParseUtils.parseDistrict(entry) : null;
  }

  /**
   * Updates a district in the state database.
   *
   * @param ctx The TX context.
   * @param entry The district object.
   * @throws Exeption
   */
  public static void updateDistrict(final Context ctx, final District entry) throws Exception {
    final String[] keyParts =
        new String[] {Common.pad(entry.getD_w_id()), Common.pad(entry.getD_id())};
    LOGGER.info("updateDistrict entry: " + keyParts);
    LedgerUtils.updateEntry(ctx, TABLES.DISTRICT, keyParts, entry);
    LOGGER.info("Distruct entry updated");
  }

  /** CUSTOMER API */

  /**
   * Adds a new customer to the state database.
   *
   * @param ctx The TX context.
   * @param entry The JSON string.
   * @throws Exception
   */
  public static void createCustomer(final Context ctx, final Object entry) throws Exception {
    LOGGER.info("Starting createCustomer");
    final Customer customer =
        entry instanceof String ? ParseUtils.parseCustomer((String) entry) : (Customer) entry;

    final String[] keyParts =
        new String[] {
          Common.pad(customer.getC_w_id()),
          Common.pad(customer.getC_d_id()),
          Common.pad(customer.getC_id())
        };
    LOGGER.info("Begin createCustomer entry with keypart: " + keyParts);
    LedgerUtils.createEntry(ctx, TABLES.CUSTOMER, keyParts, entry);

    final String[] lastNameKeyParts =
        new String[] {
          Common.pad(customer.getC_w_id()),
          Common.pad(customer.getC_d_id()),
          customer.getC_last(),
          Common.pad(customer.getC_id())
        };
    LOGGER.info("createCustomer entry with last name : " + lastNameKeyParts);
    LedgerUtils.createEntry(ctx, TABLES.CUSTOMER_LAST_NAME, lastNameKeyParts, entry);

    LOGGER.info("Create customer Complete for " + keyParts + " last name " + lastNameKeyParts);
  }

  /**
   * Retrieves a customer record from the ledger.
   *
   * @param ctx The TX context.
   * @param c_w_id The warehouse ID.
   * @param c_d_id The district ID.
   * @param c_id The customer ID.
   * @return The retrieved customer.
   */
  public static Customer getCustomer(
      final Context ctx, final int c_w_id, final int c_d_id, final int c_id) throws Exception {
    final String[] keyParts =
        new String[] {Common.pad(c_w_id), Common.pad(c_d_id), Common.pad(c_id)};
    LOGGER.info("getCustomer with key " + keyParts);
    final String entry = LedgerUtils.getEntry(ctx, TABLES.CUSTOMER, keyParts);
    if (entry == null) {
      throw new Exception(
          "Could not retrieve Customer(" + c_w_id + ", " + c_d_id + " , " + c_id + ")");
    }
    LOGGER.info("Customer entry" + entry + "retrieved");
    return entry != null ? ParseUtils.parseCustomer(entry) : null;
  }

  /**
   * Retrieves the customers from the state database that match the given partial key
   *
   * @param ctx The TX context.
   * @param c_w_id The warehouse ID of the customer.
   * @param c_d_id The district ID of the customer.
   * @param c_last The last name of the customer.
   * @return The retrieved customers.
   * @throws Exception
   */
  public static List<Customer> getCustomersByLastName(
      final Context ctx, final int c_w_id, final int c_d_id, final String c_last) throws Exception {
    LOGGER.info("getCustomerByLastName");
    final String[] keyParts = new String[] {Common.pad(c_w_id), Common.pad(c_d_id), c_last};
    List<Object> entries =
        LedgerUtils.select(
            ctx, TABLES.CUSTOMER_LAST_NAME, keyParts, MatchData.CLastMatchData(c_last), false);

    if (entries.size() == 0) {
      throw new Exception(
          String.format(
              "Could not find Customers(%d, %d, c_id) matching last name \"%s\"",
              c_w_id, c_d_id, c_last));
    }

    final List<Customer> customers = new ArrayList<>();
    for (Object entry : entries) {
      customers.add((Customer) entry);
      LOGGER.info("List of Customers");
    }

    return customers;
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
  public static Customer getCustomersByIdOrLastName(
      final Context ctx,
      final int c_w_id,
      final int c_d_id,
      final Integer c_id,
      final String c_last)
      throws Exception {
    if (c_id != null) {
      // Case 1, the customer is selected based on customer number: the row in the CUSTOMER
      // table with matching C_W_ID, C_D_ID, and C_ID is selected and C_BALANCE, C_FIRST,
      // C_MIDDLE, and C_LAST are retrieved.
      return LedgerUtils.getCustomer(ctx, c_w_id, c_d_id, c_id);
    } else if (c_last != null) {
      // Case 2, the customer is selected based on customer last name: all rows in the
      // CUSTOMER table with matching C_W_ID, C_D_ID and C_LAST are selected sorted by
      // C_FIRST in ascending order. Let n be the number of rows selected. C_BALANCE,
      // C_FIRST, C_MIDDLE, and C_LAST are retrieved from the row at position n/ 2 rounded up
      // in the sorted set of selected rows from the CUSTOMER table.
      final List<Customer> customerList =
          LedgerUtils.getCustomersByLastName(ctx, c_w_id, c_d_id, c_last);
      customerList.sort(
          new Comparator<Customer>() {
            @Override
            public int compare(final Customer c1, final Customer c2) {
              return c1.getC_first().compareTo(c2.getC_first());
            }
          });
      int position = (int) Math.ceil(customerList.size() / 2.0);
      return customerList.get(position - 1);
    } else {
      throw new Exception(
          "Neither the customer ID nor the customer last name parameter is supplied");
    }
  }

  /**
   * Updates a customer in the state database.
   *
   * @param ctx The TX context.
   * @param entry The customer object.
   * @throws Exception
   */
  public static void updateCustomer(final Context ctx, final Customer entry) throws Exception {
    final String[] keyParts =
        new String[] {
          Common.pad(entry.getC_w_id()), Common.pad(entry.getC_d_id()), Common.pad(entry.getC_id())
        };
    LOGGER.info("update customer entry" + keyParts);
    LedgerUtils.updateEntry(ctx, TABLES.CUSTOMER, keyParts, entry);

    final String[] lastNameKeyParts =
        new String[] {
          Common.pad(entry.getC_w_id()),
          Common.pad(entry.getC_d_id()),
          entry.getC_last(),
          Common.pad(entry.getC_id())
        };
    LOGGER.info("update customer Last name entry table with" + lastNameKeyParts);
    LedgerUtils.updateEntry(ctx, TABLES.CUSTOMER_LAST_NAME, lastNameKeyParts, entry);
  }

  /** HISTORY API */

  /**
   * Adds a new history to the state database.
   *
   * @param ctx The TX context.
   * @param entry The JSON string.
   * @throws Exception
   */
  public static void createHistory(final Context ctx, final Object entry) throws Exception {
    final History history =
        entry instanceof String ? ParseUtils.parseHistory((String) entry) : (History) entry;

    final String[] keyParts =
        new String[] {
          Common.pad(history.getH_c_w_id()),
          Common.pad(history.getH_c_d_id()),
          Common.pad(history.getH_c_id()),
          history.getH_date()
        };
    LedgerUtils.createEntry(ctx, TABLES.HISTORY, keyParts, entry);
  }

  /** NEW ORDER API */

  /**
   * Adds a new new order to the state database.
   *
   * @param ctx The TX context.
   * @param entry The JSON string. @
   */
  public static void createNewOrder(final Context ctx, final Object entry) throws Exception {
    LOGGER.info("Create new order entry");
    final NewOrder newOrder =
        entry instanceof String ? ParseUtils.parseNewOrder((String) entry) : (NewOrder) entry;

    final String[] keyParts =
        new String[] {
          Common.pad(newOrder.getNo_w_id()),
          Common.pad(newOrder.getNo_d_id()),
          Common.pad(newOrder.getNo_o_id())
        };
    LedgerUtils.createEntry(ctx, TABLES.NEW_ORDER, keyParts, entry);
    LOGGER.info("Create new order entry COMPLETE!!");
  }

  /**
   * Retrieves the oldest new order from the state database that matches the given partial key.
   *
   * @param ctx The TX context.
   * @param no_w_id The new order's warehouse ID.
   * @param no_d_id The new order's district ID.
   * @return The oldest new order.
   */
  public static NewOrder getOldestNewOrder(final Context ctx, final int no_w_id, final int no_d_id)
      throws Exception {
    LOGGER.info(
        "Searching for oldest New Order for warehouse " + no_w_id + " and district " + no_d_id);
    final List<Object> oldestNewOrders =
        LedgerUtils.select(
            ctx,
            TABLES.NEW_ORDER,
            new String[] {Common.pad(no_w_id), Common.pad(no_d_id)},
            MatchData.ParseNewOrderMatchData(),
            true);
    // if (oldest != null) {
    //     LOGGER.info("Retrieved oldest oldest New Order( " + no_w_id + "," + no_d_id + "," +
    // oldest.no_o_id + ")");
    // }
    final Object oldest = oldestNewOrders.get(0);
    LOGGER.info("Retrieved oldest New Order( " + JSON.serialize(oldest));
    // SOMETHING WRONG HERE
    // Retrieved oldest New Order(
    // [{"o_id":0,"o_d_id":0,"o_w_id":0,"o_c_id":0,"o_carrier_id":0,"o_ol_cnt":0,"o_all_local":0}]
    return (NewOrder) oldest;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Retrieves new order from the state database that matches the given partial key.
   *
   * @param ctx The TX context.
   * @param no_w_id The new order's warehouse ID.
   * @param no_d_id The new order's district ID.
   * @return The new order entry.
   */
  public static NewOrder getNewOrder(
      final Context ctx, final int no_w_id, final int no_d_id, final int no_o_id) throws Exception {
    final String[] keyParts =
        new String[] {Common.pad(no_w_id), Common.pad(no_d_id), Common.pad(no_o_id)};
    LOGGER.info("Begin getNewOrder with entry: " + keyParts);
    final String entry = LedgerUtils.getEntry(ctx, TABLES.NEW_ORDER, keyParts);

    if (entry == null) {
      throw new Exception(
          "Could not retrieve NewOrder(" + no_w_id + ", " + no_d_id + ", " + no_o_id + ")");
    }
    LOGGER.info("getNewOrder returned " + entry);
    return entry != null ? ParseUtils.parseNewOrder(entry) : null;
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * Deletes a new order from the state database.
   *
   * @param ctx The TX context.
   * @param entry The new order object. @
   */
  public static void deleteNewOrder(final Context ctx, final NewOrder entry) throws Exception {
    LOGGER.info("Begin delete oldest NewOrder" + JSON.serialize(entry));
    final String[] keyParts =
        new String[] {
          Common.pad(entry.getNo_w_id()),
          Common.pad(entry.getNo_d_id()),
          Common.pad(entry.getNo_o_id())
        };
    LOGGER.info("delete oldest NewOrder entry with key " + keyParts);
    LedgerUtils.deleteEntry(ctx, TABLES.NEW_ORDER, keyParts);
    LOGGER.info("Oldest NewOrder entry with key " + keyParts + "deleted");
  }

  /** ORDER API */

  /**
   * Adds a new order to the state database.
   *
   * @param ctx The TX context.
   * @param entry The JSON string. @
   */
  public static void createOrder(final Context ctx, final Object entry) throws Exception {
    LOGGER.info("CREATE ORDER ENTRY");
    final Order order =
        entry instanceof String ? ParseUtils.parseOrder((String) entry) : (Order) entry;
    final String[] keyParts =
        new String[] {
          Common.pad(order.getO_w_id()),
          Common.pad(order.getO_d_id()),
          Common.pad(Integer.MAX_VALUE - order.getO_id())
        };
    LedgerUtils.createEntry(ctx, TABLES.ORDERS, keyParts, entry);
    LOGGER.info(
        "CREATE ORDER ENTRY COMPLETED FOR "
            + order.getO_w_id()
            + "-"
            + order.getO_d_id()
            + "-"
            + order.getO_id());
  }

  /**
   * Retrieves an order record from the ledger.
   *
   * @param ctx The TX context.
   * @param o_w_id The order's warehouse ID.
   * @param o_d_id The order's district ID.
   * @param o_id The order ID.
   * @return The retrieved order.
   */
  public static Order getOrder(
      final Context ctx, final int o_w_id, final int o_d_id, final int o_id) throws Exception {
    String[] keyParts =
        new String[] {Common.pad(o_w_id), Common.pad(o_d_id), Common.pad(Integer.MAX_VALUE - o_id)};
    LOGGER.info("Begin getOrder with entry: " + keyParts);
    String entry = LedgerUtils.getEntry(ctx, TABLES.ORDERS, keyParts);

    if (entry == null) {
      throw new Exception("Could not retrieve Order(" + o_w_id + ", " + o_d_id + ", " + o_id + ")");
    }
    LOGGER.info("getOrder returned " + entry);
    return entry != null ? ParseUtils.parseOrder(entry) : null;
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
      final Context ctx, final int o_w_id, final int o_d_id, final int o_c_id) throws Exception {
    LOGGER.info("Searching for last Order of Customer(" + o_w_id + "," + o_d_id + "," + o_c_id);

    final String[] keyParts = new String[] {Common.pad(o_w_id), Common.pad(o_d_id)};
    final List<Object> lastOrders =
        LedgerUtils.select(ctx, TABLES.ORDERS, keyParts, MatchData.OCIDMatchData(o_c_id), true);
    if (lastOrders == null) {
      throw new Exception(
          String.format(
              "Could not find last Order(%d, %d, o_id) of Customer(%d, %d, %d)",
              o_w_id, o_d_id, o_w_id, o_d_id, o_c_id));
    }
    final Object lastOrder = lastOrders.get(0);
    LOGGER.info("Retrieved last Order of Customer " + o_c_id);
    return (Order) lastOrder;
  }

  /**
   * Updates an order in the state database.
   *
   * @param ctx The TX context.
   * @param entry The order object.
   * @throws Exception
   */
  public static void updateOrder(final Context ctx, final Order entry) throws Exception {
    LOGGER.info("Begin update Order");
    final String[] keyParts =
        new String[] {
          Common.pad(entry.getO_w_id()),
          Common.pad(entry.getO_d_id()),
          Common.pad(Integer.MAX_VALUE - entry.getO_id())
        };
    LedgerUtils.updateEntry(ctx, TABLES.ORDERS, keyParts, entry);
    LOGGER.info("Order updated");
  }

  /** ORDER LINE API */

  /**
   * Adds a new order line to the state database.
   *
   * @param ctx The TX context.
   * @param entry The JSON string. @
   */
  public static void createOrderLine(final Context ctx, final Object entry) throws Exception {
    LOGGER.info("BEGIN CREATE ORDERLINE");
    final OrderLine orderLine =
        entry instanceof String ? ParseUtils.parseOrderLine((String) entry) : (OrderLine) entry;
    final String[] keyParts =
        new String[] {
          Common.pad(orderLine.getOl_w_id()),
          Common.pad(orderLine.getOl_d_id()),
          Common.pad(orderLine.getOl_o_id()),
          Common.pad(orderLine.getOl_number())
        };
    LedgerUtils.createEntry(ctx, TABLES.ORDER_LINE, keyParts, entry);
    LOGGER.info("CREATED ORDERLINE ENTRY");
  }

  /**
   * Retrieves an order line record from the ledger.
   *
   * @param ctx The TX context.
   * @param ol_w_id The order's warehouse ID.
   * @param ol_d_id The order's district ID.
   * @param ol_o_id The order ID.
   * @param ol_number The number of the order line.
   * @return {<OrderLine>} The retrieved order line.
   */
  public static OrderLine getOrderLine(
      final Context ctx,
      final int ol_w_id,
      final int ol_d_id,
      final int ol_o_id,
      final int ol_number)
      throws Exception {
    final String[] keyParts =
        new String[] {
          Common.pad(ol_w_id), Common.pad(ol_d_id), Common.pad(ol_o_id), Common.pad(ol_number)
        };
    LOGGER.info("Begin getOrdeline entry for keyparts " + keyParts);
    final String entry = LedgerUtils.getEntry(ctx, TABLES.ORDER_LINE, keyParts);

    if (entry == null) {
      throw new Exception(
          "Could not retrieve Order Line("
              + ol_w_id
              + ","
              + ol_d_id
              + ","
              + ol_o_id
              + ", "
              + ol_number
              + ")");
    }
    LOGGER.info("getOrdeline entry for keyparts " + keyParts + "Returned " + entry);
    // return entry ? ParseUtils.parseOrderLine(entry) : (OrderLine) null;
    return entry != null ? ParseUtils.parseOrderLine(entry) : null;
  }

  /**
   * Updates an order line in the state database.
   *
   * @param ctx The TX context.
   * @param entry The order line object. @
   */
  public static void updateOrderLine(final Context ctx, final OrderLine entry) throws Exception {
    LOGGER.info("UPDATE ORDERLINE");
    final String[] keyParts =
        new String[] {
          Common.pad(entry.getOl_w_id()),
          Common.pad(entry.getOl_d_id()),
          Common.pad(entry.getOl_o_id()),
          Common.pad(entry.getOl_number())
        };
    LedgerUtils.updateEntry(ctx, TABLES.ORDER_LINE, keyParts, entry);
    LOGGER.info("UPDATED ORDERLINE ENTRY" + entry.getOl_o_id() + "," + entry.getOl_number());
  }

  /** ITEM API */

  /**
   * Adds a new item to the state database.
   *
   * @param ctx The TX context.
   * @param entry The JSON string.
   * @throws Exception
   */
  public static void createItem(final Context ctx, final Object entry) throws Exception {
    LOGGER.info("CREATE ITEM");
    final Item item = entry instanceof String ? ParseUtils.parseItem((String) entry) : (Item) entry;
    LedgerUtils.createEntry(ctx, TABLES.ITEM, new String[] {Common.pad(item.getI_id())}, entry);
    LOGGER.info("CREATED ITEM ENTRY");
  }

  /**
   * Retrieves an item record from the ledger.
   *
   * @param ctx The TX context.
   * @param i_id The item ID.
   * @return {<Item>} The retrieved item.
   */
  public static Item getItem(Context ctx, int i_id) throws Exception {
    LOGGER.info("GET ITEM ENTRY FROM THE ITEM TABLE");
    final String entry = LedgerUtils.getEntry(ctx, TABLES.ITEM, new String[] {Common.pad(i_id)});
    LOGGER.info("GET ITEM ENTRY RETURNED" + entry);
    LOGGER.info("parse ITEM ENTRY RETURNED" + ParseUtils.parseItem(entry));
    return entry != null ? ParseUtils.parseItem(entry) : null;
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
      final Context ctx, final int w_id, final int d_id, final int o_id_min, final int o_id_max)
      throws Exception {
    LOGGER.info("Counts the number of items whose stock is below a given threshold.");
    final Set<Integer> itemIds = new HashSet<>();
    LOGGER.info("Retrieving item IDs for Orders with w_id " + w_id + " and d_id " + d_id);
    for (int current_o_id = o_id_min; current_o_id < o_id_max; current_o_id++) {

      final Order order = LedgerUtils.getOrder(ctx, w_id, d_id, current_o_id);
      LOGGER.info("RETRIEVED ORDER > " + JSON.serialize(order));

      for (int ol_number = 1; ol_number <= order.getO_ol_cnt(); ol_number++) {
        final OrderLine orderLine =
            LedgerUtils.getOrderLine(ctx, w_id, d_id, current_o_id, ol_number);
        LOGGER.info("RETRIEVED ORDERLINE " + JSON.serialize(orderLine));
        itemIds.add(orderLine.getOl_i_id());
        LOGGER.info("RETRIEVED ITEM IDS: " + itemIds);
        for (Integer strCurrentNumber : itemIds) {
          System.out.println(strCurrentNumber);
        }
      }
    }

    if (itemIds.size() == 0) {
      throw new Exception(
          String.format(
              "Could not find item IDs of recent Orders(%d, %d, [%d, %d))",
              w_id, d_id, o_id_min, o_id_max));
    }

    LOGGER.info(
        "Retrieved"
            + itemIds.size()
            + "item IDs for Orders("
            + w_id
            + ","
            + d_id
            + ","
            + o_id_min
            + ", "
            + o_id_max
            + ")");

    return new ArrayList<>(itemIds);
  }

  /** STOCK API */

  /**
   * Adds a new stock to the state database.
   *
   * @param ctx The TX context.
   * @param entry The JSON string.
   * @async
   */
  public static void createStock(final Context ctx, final Object entry) throws Exception {
    LOGGER.info("CREATE STOCK ENTRY" + entry);
    final Stock stock =
        entry instanceof String ? ParseUtils.parseStock((String) entry) : (Stock) entry;
    LedgerUtils.createEntry(
        ctx,
        TABLES.STOCK,
        new String[] {Common.pad(stock.getS_w_id()), Common.pad(stock.getS_i_id())},
        entry);
    LOGGER.info("CREATED STOCK ENTRY" + JSON.serialize(stock));
  }

  /**
   * Retrieves a stock record from the ledger.
   *
   * @param ctx The TX context.
   * @param s_w_id The warehouse ID.
   * @param s_i_id The item ID.
   * @return {<Stock>} The retrieved stock.
   */
  public static Stock getStock(final Context ctx, final int s_w_id, final int s_i_id)
      throws Exception {
    LOGGER.info("get stock entry");
    final String entry =
        LedgerUtils.getEntry(
            ctx, TABLES.STOCK, new String[] {Common.pad(s_w_id), Common.pad(s_i_id)});
    if (entry == null) {
      throw new Exception("Could not retrieve Stock(" + s_w_id + ", " + s_i_id + ")");
    }

    // return entry ? ParseUtils.parseStock(entry) : (Stock)null;
    LOGGER.info("RETRIEVED ENTRY " + entry);
    return entry != null ? ParseUtils.parseStock(entry) : null;
  }

  /**
   * Updates a stock in the state database.
   *
   * @param ctx The TX context.
   * @param entry The stock object.
   * @async
   */
  public static void updateStock(final Context ctx, final Stock entry) throws Exception {
    LOGGER.info("UPDATE STOCK ENTRY");
    LedgerUtils.updateEntry(
        ctx,
        TABLES.STOCK,
        new String[] {Common.pad(entry.getS_w_id()), Common.pad(entry.getS_i_id())},
        entry);
  }
}
