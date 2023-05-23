/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
SPDX-License-Identifier: Apache-2.0
*/

package main.java.org.example;

import main.java.org.example.ParseUtils;
import main.java.org.example.common;
import main.java.org.example.common.TABLES;
import main.java.org.example.Warehouse;


import org.hyperledger.fabric.contract.Context;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Utility functions for accessing the state database.
 */
public class LedgerUtils {
    /**
     * LOW-LEVEL API
     */

    /**
     * Adds a new record to the state database. Throws an error if it already exists.
     * @param ctx The TX context.
     * @param type The type of the record.
     * @param keyParts The parts of the record's primary key.
     * @param entry The JSON string/object representation of the record.
     * @throws Exception
     */
    public static void createEntry(Context ctx, String type, String[] keyParts, Object entry) throws Exception {
        String key = ctx.getStub().createCompositeKey(type, keyParts);
        String entryString = entry instanceof String ? (String) entry : new Gson().toJson(entry);
        byte[] buffer = entryString.getBytes(UTF_8);

        long start = System.currentTimeMillis();
        ctx.getStub().putState(key, buffer);
        long end = System.currentTimeMillis();

        ctx.getTxInfo().stat_put_cnt += 1;
        ctx.getTxInfo().stat_put_size_bytes += buffer.length;
        ctx.getTxInfo().stat_put_exec_total_ms += end - start;
        // log("Created entry: " + key, ctx);
        // log("With value: " + entryString, ctx);
    }

    /**
     * Adds a new record to the state database. Throws an error if it already exists.
     * @param ctx The TX context.
     * @param type The type of the record.
     * @param keyParts The parts of the record's primary key.
     * @param entry The JSON string/object representation of the record.
     * @throws Exception
     */
    public static void updateEntry(Context ctx, String type, String[] keyParts, Object entry) throws Exception {
        String key = ctx.getStub().createCompositeKey(type, keyParts);
        String entryString = entry instanceof String ? (String) entry : new Gson().toJson(entry);
        byte[] buffer = entryString.getBytes(UTF_8);

        long start = System.currentTimeMillis();
        ctx.getStub().putState(key, buffer);
        long end = System.currentTimeMillis();

        ctx.getTxInfo().stat_put_cnt += 1;
        ctx.getTxInfo().stat_put_size_bytes += buffer.length;
        ctx.getTxInfo().stat_put_exec_total_ms += end - start;
        // log("Created entry: " + key, ctx);
        // log("With value: " + entryString, ctx);
    }

    /**
     * Retrieves the given record from the state database.
     * @param ctx The TX context.
     * @param type The type of the record.
     * @param keyParts The attribute values as key parts.
     * @return The retrieved data or null if not found.
     */
    public static String getEntry(Context ctx, String type, String[] keyParts) throws Exception {
        String key = ctx.getStub().createCompositeKey(type, keyParts);

        long start = System.currentTimeMillis();
        byte[] data = ctx.getStub().getState(key);
        long end = System.currentTimeMillis();

        ctx.getTxinfo().stat_get_cnt += 1;
        ctx.getTxinfo().stat_get_exec_total_ms += end - start;

        if (data.length > 0) {
            ctx.getTxinfo().stat_get_size_bytes += data.length;
            String entry = new String(data, UTF_8);
            // log("Retrieved entry: " + key, ctx);
            // log("With value: " + entry, ctx);
            return entry;
        }
        // log("Couldn't find entry: " + key, ctx, "warn");
        return null;
    }

        /**
     * Deletes the given record from the state database.
     * @param ctx The TX context.
     * @param type The type of the record.
     * @param keyParts The attribute values as key parts.
     * @throws Exeption
     */
    public static void deleteEntry(Context ctx, String type, String[] keyParts) throws Exception {
        String key = ctx.getStub().createCompositeKey(type, keyParts);
        long start = System.currentTimeMillis();
        byte[] data = ctx.getStub().deleteState(key);
        long end = System.currentTimeMillis();

        ctx.getTxinfo().stat_del_cnt += 1;
        ctx.getTxinfo().stat_del_exec_total_ms += end - start;
        // log("Deleted entry: " + key, ctx);
    }  
    
    /**
     * Retrieves entries from the state database that matches certain criteria.
     * @param ctx The TX context.
     * @param type The type of entries to search.
     * @param keyParts The parts of the entries private key.
     * @param matchFunction The function that determines matches.
     * @param firstMatch Indicates whether only the first match should be returned.
     * @return The retrieved entries.
     * @throws Exception
     */
    static Object select(Context ctx, String type, String[] keyParts, Function<String, Object> matchFunction, boolean firstMatch) throws Exception {
        CompositeKey compositeKey = ctx.getStub().createCompositeKey(type, keyParts);
        QueryResultsIterator<KeyValue> iterator = ctx.getStub().getStateByPartialCompositeKey(compositeKey.toString());
        // log("Created partial iterator ");

        List<Object> matches = new ArrayList<>();
        int retrieved = 0;
        
        try {
            while (iterator.hasNext()) {
                long start = System.currentTimeMillis();
                KeyValue res = iterator.next();
                long end = System.currentTimeMillis();

                retrieved += 1;

                byte[] buffer = res.getValue();
                ctx.getTxinfo().stat_iterate_cnt += 1;
                ctx.getTxinfo().stat_iterate_size_bytes += buffer != null ? buffer.length : 0;
                ctx.getTxinfo().stat_iterate_exec_total_ms += end - start;

                String entry = new String(buffer, UTF_8);
                //log("Enumerated entry:");

                Object match = matchFunction.apply(entry);
                if (match != null) {
                    matches.add(match);
                    if (firstMatch) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log(e.toString(), ctx, "error");
            throw e;
        } finally {
            iterator.close();
        }

        //log("Matched " +matches.length + "entries out of" +retrieved, ctx);
        return firstMatch ? matches.get(0) : matches;
    }

    /**
     * WAREHOUSE API
     */

    /**
     * Adds a new warehouse to the state database.
     * @param ctx The TX context.
     * @param entry The JSON string.
     * @throws Exception
     */
    public static void createWarehouse(Context ctx, String entry) throws Exception {
        //Warehouse warehouse = entry instanceof String ? ParseUtils.parseWarehouse(entry) : (Warehouse) entry;
        Warehouse warehouse = ParseUtils.parseWarehouse(entry);
        LedgerUtils.createEntry(ctx, TABLES.WAREHOUSE, new String[]{String.format("%03d", warehouse.w_id)}, entry);
    }

    /**
     * Retrieves a warehouse record from the ledger.
     * @param ctx The TX context.
     * @param w_id The warehouse ID.
     * @return The retrieved warehouse.
     */
    public static Warehouse getWarehouse(Context ctx, int w_id) {
        //String[] entry1 = LedgerUtils.getEntry(ctx, TABLES.WAREHOUSE, new String[]{String.format("%03d", w_id)});
        String[] entry = LedgerUtils.getEntry(ctx, TABLES.WAREHOUSE, new String[]{pad(w_id)});

        if (entry == null) {
            throw new Exception("Could not retrieve Warehouse(" + w_id + ")");
        }
        //return entry != null ? ParseUtils.parseWarehouse(entry) : null;
        return ParseUtils.parseWarehouse(entry);
    }

    /**
     * Updates a warehouse in the state database.
     * @param ctx The TX context.
     * @param entry The warehouse object.
     * @throws Exception
     */
    public static void updateWarehouse(Context ctx, Warehouse entry) throws Exception {
        //LedgerUtils.updateEntry(ctx, TABLES.WAREHOUSE, new String[]{String.format("%03d" , entry.w_id)}, entry);
        LedgerUtils.updateEntry(ctx, TABLES.WAREHOUSE, new String[]{pad(entry.w_id)});
    }

    /**
     * DISTRICT API
     */

    /**
     * Adds a new district to the state database.
     * @param ctx The TX context.
     * @param entry The JSON string or District object.
     * @throws Exception
     */
    public static void createDistrict(Context ctx, String entry) throws Exception {
        District district = ParseUtils.parseDistrict(entry);
        //District district = entry instanceof String ? ParseUtils.parseDistrict(entry) : (District) entry;
        LedgerUtils.createEntry(ctx, TABLES.DISTRICT, new String[]{String.format("%03d", district.d_w_id), String.format("%03d", district.d_id)}, entry);
    }

    /**
     * Retrieves a district record from the ledger.
     * @param ctx The TX context.
     * @param d_w_id The warehouse ID.
     * @param d_id The district ID.
     * @return The retrieved district.     * 
     */
    public static District getDistrict(Context ctx, int d_w_id, int d_id) {
        String entry = LedgerUtils.getEntry(ctx, TABLES.DISTRICT, new String[]{pad(d_w_id), pad(d_id)});
        if (entry == null) {
            throw new Exception("Could not retrieve District(" + d_w_id + ", " + d_id + ")");
        }
        //return entry != null ? ParseUtils.parseDistrict(entry) : null;
        return ParseUtils.parseDistrict(entry);
    }

    /**
     * Updates a district in the state database.
     * @param ctx The TX context.
     * @param entry The district object.
     * @throws Exeption
     */
    public static void updateDistrict() throws Exception {
        LedgerUtils.updateEntry(ctx, TABLES.DISTRICT, new String[]{pad(d_w_id), pad(d_id)});
    }

    /**
     * CUSTOMER API
     */

    /**
     * Adds a new customer to the state database.
     * @param ctx The TX context.
     * @param entry The JSON string.
     * @throws Exception
     */
    public static void createCustomer(Context ctx, String entry) throws Exception {
        Customer customer = entry instanceof String ? ParseUtils.parseCustomer(entry) : (Customer) entry;
        LedgerUtils.createEntry(ctx, TABLES.CUSTOMER, new String[]{pad(customer.c_w_id), pad(customer.c_d_id)}, entry);
        LedgerUtils.createEntry(ctx, TABLES.CUSTOMER_LAST_NAME, new String[]{pad(customer.c_w_id), pad(customer.c_d_id), customer.c_last, pad(customer.c_id)}, entry);
    }

    /**
     * Retrieves a customer record from the ledger.
     * @param ctx The TX context.
     * @param c_w_id The warehouse ID.
     * @param c_d_id The district ID.
     * @param c_id The customer ID.
     * @return The retrieved customer.
     */
    public static Customer getCustomer(Context ctx, int c_w_id, int c_d_id, int c_id) {
        String entry = LedgerUtils.getEntry(ctx, TABLES.CUSTOMER, new String[]{pad(c_w_id), pad(c_d_id), pad(c_id)});
        if (entry == null) {
            throw new Exception("Could not retrieve Customer(" + c_w_id + ", " + c_d_id + " , " + c_id +")");
        }
        //return entry != null ? ParseUtils.parseCustomer(entry) : null;
        return ParseUtils.parseCustomer(entry);
    }

    /**
     * Retrieves the customers from the state database that match the given partial key
     * @param ctx The TX context.
     * @param c_w_id The warehouse ID of the customer.
     * @param c_d_id The district ID of the customer.
     * @param c_last The last name of the customer.
     * @return The retrieved customers.
     * @throws Exception
     */
    public static List<Customer> getCustomersByLastName(Context ctx, int c_w_id, int c_d_id, String c_last) throws Exception {
        Function<byte[], Customer> matchFunction = entry -> {
            Customer customer = ParseUtils.parseCustomer(entry);
            return customer.c_last.equals(c_last) ? customer : null;
        };

        //log("Searching for Customers (" + c_w_id + "," + c_d_id + ", "  + c_id + ") with last name " + c_last  , ctx);
        List<byte[]> entries = LedgerUtils.select(ctx, TABLES.CUSTOMER_LAST_NAME, new String[]{pad(c_w_id), pad(c_d_id), c_last}, matchFunction, false);
        if (entries.size() == 0) {
            throw new Exception(String.format("Could not find Customers(%d, %d, c_id) matching last name \"%s\"", c_w_id, c_d_id, c_last));
        }

        //log("Found" + entries.length + "Customers(" + c_w_id + "," + c_d_id + "," + c_id + ") with last name " + c_last , ctx);
        List<Customer> customers = new ArrayList<>();
        for (byte[] entry : entries) {
            customers.add(ParseUtils.parseCustomer(entry));
        }
        return customers;
    }

    /**
     * Retrieves the customers from the state database that match the given ID or last name
     * @param ctx The TX context.
     * @param c_w_id The warehouse ID of the customer.
     * @param c_d_id The district ID of the customer.
     * @param c_id The customer ID.
     * @param c_last The last name of the customer.
     * @return The retrieved customers.
     * @throws Exception if neither the customer ID nor the customer last name parameter is supplied
     */
    public static Customer getCustomersByIdOrLastName(Context ctx, int c_w_id, int c_d_id, Integer c_id, String c_last) throws Exception {
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
            List<Customer> customerList = LedgerUtils.getCustomersByLastName(ctx, c_w_id, c_d_id, c_last).get();
            customerList.sort((c1, c2) -> c1.c_first.compareTo(c2.c_first));
            int position = (int) Math.ceil(customerList.size() / 2.0);
            return customerList.get(position - 1);
        } else {
            throw new Exception("Neither the customer ID nor the customer last name parameter is supplied");
        }
        //////////////////////////////////////////////
        // static Customer getCustomersByIdOrLastName(Context ctx, int c_w_id, int c_d_id, int c_id, String c_last) throws Exception {
        //     if (c_id != null) {
        //         return LedgerUtils.getCustomer(ctx, c_w_id, c_d_id, c_id).get();
        //     } else if (c_last != null) {
        //         CompletableFuture<List<Customer>> customerListFuture = LedgerUtils.getCustomersByLastName(ctx, c_w_id, c_d_id, c_last);
        //         List<Customer> customerList = customerListFuture.get();
        //         customerList.sort((c1, c2) -> c1.getC_first().compareTo(c2.getC_first()));
        //         int position = (int) Math.ceil(customerList.size() / 2.0);
        //         return customerList.get(position - 1);
        //     } else {
        //         throw new Exception("Neither the customer ID nor the customer last name parameter is supplied");
        //     }
        // }
///////////////////////////////////////////////        

    }

    /**
     * Updates a customer in the state database.
     * @param ctx The TX context.
     * @param entry The customer object.
     * @throws Exception
     */
    public static void updateCustomer(Context ctx, Customer entry) throws Exception {
        LedgerUtils.updateEntry(ctx, TABLES.CUSTOMER, new String[]{pad(entry.c_w_id), pad(entry.c_d_id), pad(entry.c_id)}, entry);
        LedgerUtils.updateEntry(ctx, TABLES.CUSTOMER_LAST_NAME,
        new String[]{pad(entry.c_w_id), pad(entry.c_d_id), entry.c_last, pad(entry.c_id)}, entry);
    } 
    
    /**
     * HISTORY API
     */

    /**
     * Adds a new history to the state database.
     * @param ctx The TX context.
     * @param entry The JSON string.
     * @throws Exception
     */
    public static void createHistory(Context ctx, String entry) throws Exception {
        History history = ParseUtils.parseHistory(entry);
        LedgerUtils.createEntry(ctx, TABLES.HISTORY, new String[]{pad(history.h_c_w_id), pad(history.h_c_d_id), pad(history.h_c_id), history.h_date}, entry);
    }

    /**
     * NEW ORDER API
     */

    /**
     * Adds a new new order to the state database.
     * @param ctx The TX context.
     * @param entry The JSON string.
     * @
     */
    public static void createNewOrder(Context ctx, String entry) {
        NewOrder newOrder = ParseUtils.parseNewOrder(entry);
        LedgerUtils.createEntry(ctx, TABLES.NEW_ORDER, new String[]{pad(newOrder.no_w_id), pad(newOrder.no_d_id), pad(newOrder.no_o_id)}, entry);
    }

    /**
     * Retrieves the oldest new order from the state database that matches the given partial key.
     * @param {Context} ctx The TX context.
     * @param {number} no_w_id The new order's warehouse ID.
     * @param {number} no_d_id The new order's district ID.
     * @return {NewOrder} The oldest new order.
     * @throws Exception 
     */
    public static NewOrder getOldestNewOrder(Context ctx, int no_w_id, int no_d_id) throws Exception {
        //log("Searching for oldest New Order(" + no_w_id + "," + no_d_id + "," + no_o_id , ctx);
        NewOrder oldest = LedgerUtils.select(ctx, TABLES.NEW_ORDER, new String[]{pad(no_w_id), pad(no_d_id)}, ParseUtils.parseNewOrder, true);

        // if (oldest) {
        //     log("Retrieved oldest oldest New Order(" + no_w_id + "," + no_d_id + "," + oldest.no_o_id + ")" , ctx);
        // }

        return oldest;
    }

    /**
     * Deletes a new order from the state database.
     * @param ctx The TX context.
     * @param entry The new order object.
     * @
     */
    public static void deleteNewOrder(Context ctx, NewOrder entry) {
        LedgerUtils.deleteEntry(ctx, TABLES.NEW_ORDER, new String[]{pad(entry.no_w_id), pad(entry.no_d_id), pad(entry.no_o_id)});
    }

    /**
     * ORDER API
     */

    /**
     * Adds a new order to the state database.
     * @param ctx The TX context.
     * @param entry The JSON string.
     * @
     */
    public static void createOrder(ontext ctx, String entry) {
        Order order = entry instanceof String ? ParseUtils.parseOrder(entry) : (Order)entry;
        LedgerUtils.createEntry(ctx, TABLES.ORDERS, new String[] {pad(order.o_w_id), pad(order.o_d_id), pad(Integer.MAX_VALUE - order.o_id)}, entry);
    }

    /**
     * Retrieves an order record from the ledger.
     * @param ctx The TX context.
     * @param o_w_id The order's warehouse ID.
     * @param o_d_id The order's district ID.
     * @param o_id The order ID.
     * @return  The retrieved order.
     */
    public static Order getOrder(Context ctx, int o_w_id, int o_d_id, int o_id) {
        Order entry = LedgerUtils.getEntry(ctx, TABLES.ORDERS, new String[] {pad(o_w_id), pad(o_d_id), pad(Integer.MAX_VALUE - o_id)});
        if (entry == null) {
            throw new Exception("Could not retrieve Order(" + o_w_id + ", " + o_d_id + ", "+ o_id + ")");
        }

        return ParseUtils.parseOrder(entry);
    }

    /**
     * Retrieves the last of a customer from the state database.
     * @param ctx The TX context.
     * @param o_w_id The warehouse ID of the order.
     * @param o_d_id The district ID of the order.
     * @param o_c_id The customer ID for the order.
     * @return The retrieved order.
     * @throws Exception if the last order is not found.
     */
    public static Order getLastOrderOfCustomer(Context ctx, int o_w_id, int o_d_id, int o_c_id) throws Exception {
        Function<byte[], Customer> matchFunction = entry -> {        
            Order order = ParseUtils.parseOrder(entry);
            if (order.o_c_id == o_c_id) {
                return order;
            }

            return null;
        };

        log("Searching for last Order(" + o_w_id + "," + o_d_id + "," + o_id + ") of Customer(" + o_w_id + "," + o_d_id + "," + o_c_id + ")" , ctx);
        Order lastOrder = LedgerUtils.select(ctx, TABLES.ORDERS, new string[] {pad(o_w_id), pad(o_d_id)}, matchFunction, true);
        if (lastOrder == null) {
            throw new Exception(String.format("Could not find last Order(%d, %d, o_id) of Customer(%d, %d, %d)", o_w_id, o_d_id, o_w_id, o_d_id, o_c_id));
        }
        log("Retrieved last Order(" + o_w_id+ ", " + o_d_id + "," + lastOrder.o_id + ") of Customer(" + o_w_id + ", " +o_d_id+ "," + o_c_id + ")", ctx);
        
        return lastOrder;
    }

    /**
     * Updates an order in the state database.
     * @param ctx The TX context.
     * @param entry The order object.
     * @async
     */
    static async updateOrder(Context ctx, Order entry) {
        LedgerUtils.updateEntry(ctx, TABLES.ORDERS, new String[] {pad(entry.o_w_id), pad(entry.o_d_id), pad(Integer.MAX_VALUE - entry.o_id)}, entry);
    }

    /**
     * ORDER LINE API
     */

    /**
     * Adds a new order line to the state database.
     * @param ctx The TX context.
     * @param entry The JSON string.
     * @
     */
    public static void createOrderLine(Context ctx, String entry) throws Exception {
        OrderLine orderLine = entry instanceof String ? ParseUtils.parseOrderLine(entry) : (OrderLine) entry;
        LedgerUtils.createEntry(ctx, TABLES.ORDER_LINE, new String[] {pad(orderLine.ol_w_id), pad(orderLine.ol_d_id),
            pad(orderLine.ol_o_id), pad(orderLine.ol_number)}, entry);
    }

        /**
     * Retrieves an order line record from the ledger.
     * @param ctx The TX context.
     * @param ol_w_id The order's warehouse ID.
     * @param ol_d_id The order's district ID.
     * @param ol_o_id The order ID.
     * @param ol_number The number of the order line.
     * @return {<OrderLine>} The retrieved order line.
     */
    public static OrderLine getOrderLine(Context ctx, int ol_w_id, int ol_d_id, int ol_o_id, int ol_number) {
        OrderLine entry = LedgerUtils.getEntry(ctx, TABLES.ORDER_LINE, new String[] {pad(ol_w_id), pad(ol_d_id), pad(ol_o_id), pad(ol_number)});
        if (entry == null) {
            throw new Exception("Could not retrieve Order Line(" + ol_w_id + "," + ol_d_id + "," + ol_o_id + ", " + ol_number + ")");
        }

        //return entry ? ParseUtils.parseOrderLine(entry) : (OrderLine) null;
        return ParseUtils.parseOrderLine(entry);
    }


    /**
     * Updates an order line in the state database.
     * @param ctx The TX context.
     * @param entry The order line object.
     * @
     */
    public static void updateOrderLine(Context ctx, OrderLine entry) {
        LedgerUtils.updateEntry(ctx, TABLES.ORDER_LINE, new String[] {pad(entry.ol_w_id), pad(entry.ol_d_id), pad(entry.ol_o_id), pad(entry.ol_number)}, entry);
    }


    /**
     * ITEM API
     */

    /**
     * Adds a new item to the state database.
     * @param ctx The TX context.
     * @param entry The JSON string.
     * @throws Exception
     */
    public static void createItem(Context ctx, String entry) {
        Item item = entry instanceof String ? ParseUtils.parseItem(entry) : (Item)entry;
        LedgerUtils.createEntry(ctx, TABLES.ITEM, new String[]{pad(item.i_id)}, entry);
    }

    /**
     * Retrieves an item record from the ledger.
     * @param ctx The TX context.
     * @param i_id The item ID.
     * @return {<Item>} The retrieved item.
     */
    public static Item getItem(Context ctx, int i_id) {
        Item entry = LedgerUtils.getEntry(ctx, TABLES.ITEM, new String[] {pad(i_id)});
        //return entry ? ParseUtils.parseItem(entry) : (Item) null;
        return ParseUtils.parseItem(entry);
    }

    /**
     * Counts the number of items whose stock is below a given threshold.
     * @param ctx The TX context.
     * @param w_id The warehouse ID.
     * @param d_id The district ID.
     * @param o_id_min The oldest/minimum order ID to consider (inclusive).
     * @param o_id_max The newest/maximum order ID to consider (exclusive).
     * @return The unique IDs of items from the recent orders.
     */
    public static List<Integer> getItemIdsOfRecentOrders(Context ctx, int w_id, int d_id, int o_id_min, int o_id_max) throws Exception {
        Set<Integer> itemIds = new HashSet<>();
        
        // log("Retrieving item IDs for Orders(" + w_id + "," + d_id + ", " + o_id_min + ", " + o_id_max + ")"), ctx);
        for (int current_o_id = o_id_min; current_o_id < o_id_max; current_o_id++) {
            Order order = LedgerUtils.getOrder(ctx, w_id, d_id, current_o_id);

            for (int ol_number = 1; ol_number <= order.o_ol_cnt; ol_number++) {
                OrderLine orderLine = LedgerUtils.getOrderLine(ctx, w_id, d_id, current_o_id, ol_number);
                itemIds.add(orderLine.ol_i_id);
            }
        }

        if (itemIds.size() == 0) {
            throw new Exception(String.format("Could not find item IDs of recent Orders(%d, %d, [%d, %d))", w_id, d_id, o_id_min, o_id_max));
        }
        
        //log("Retrieved" + itemIds.size + "item IDs for Orders(" + w_id + "," + d_id + "," +o_id_min + ", " + o_id_max + ")" , ctx);
        
        return new ArrayList<>(itemIds);
    }

    /**
     * STOCK API
     */

    /**
     * Adds a new stock to the state database.
     * @param ctx The TX context.
     * @param entry The JSON string.
     * @async
     */
    public static void createStock(Context ctx, String entry) {
        Stock stock = entry instanceof String ? ParseUtils.parseStock(entry) : (Stock)entry;        
        LedgerUtils.createEntry(ctx, TABLES.STOCK, new String[]{pad(stock.s_w_id), pad(stock.s_i_id)}, entry);
    }

    /**
     * Retrieves a stock record from the ledger.
     * @param ctx The TX context.
     * @param s_w_id The warehouse ID.
     * @param s_i_id The item ID.
     * @return {<Stock>} The retrieved stock.
     */
    public static Stock getStock(Context ctx, int s_w_id, int s_i_id) {
        Stock entry = LedgerUtils.getEntry(ctx, TABLES.STOCK, new String[] {pad(s_w_id), pad(s_i_id)});
        if (entry == null) {
            throw new Exception("Could not retrieve Stock(" + s_w_id + ", " + s_i_id + ")" );
        }

        //return entry ? ParseUtils.parseStock(entry) : (Stock)null;
        return ParseUtils.parseStock(entry);
    }

    /**
     * Updates a stock in the state database.
     * @param ctx The TX context.
     * @param entry The stock object.
     * @async
     */
    static async updateStock(Context ctx, Stock entry) {
        LedgerUtils.updateEntry(ctx, TABLES.STOCK, new String[] {pad(entry.s_w_id), pad(entry.s_i_id)}, entry);
    }
}
