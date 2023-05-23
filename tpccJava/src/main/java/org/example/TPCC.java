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

package main.java.org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import main.java.org.example.ParseUtils;
import main.java.org.example.LedgerUtils;
import main.java.org.example.common;
import main.java.org.example.*;



import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.hyperledger.fabric.contract.Contract;







@Contract(name = "TPCC",
    info = @Info(title = "tpcc contract",
                description = "My Smart Contract",
                version = "0.0.1",
                license =
                        @License(name = "Apache-2.0",
                                url = ""),
                                contact =  @Contact(email = "tpcc@example.com",
                                                name = "tpcc",
                                                url = "http://tpcc.me")))

/**
 * The implementation of the TPC-C benchmark smart contract according to the specification version v5.11.0.
 */

@Default
public class TPCC implements ContractInterface {
    public  TPCC() {

    }
    public void addTxInfo(Context ctx) {
        ctx.getTxInfo().tx_id = ctx.getStub().getTxID();
        ctx.getTxInfo().cc_start_time_epoch_ms = System.currentTimeMillis();
        ctx.getTxInfo().cc_end_time_epoch_ms = 0;
        ctx.getTxInfo().cc_duration_ms = 0;
        ctx.getTxInfo().stat_get_cnt = 0;
        ctx.getTxInfo().stat_get_size_bytes = 0;
        ctx.getTxInfo().stat_get_exec_total_ms = 0;
        ctx.getTxInfo().stat_put_cnt = 0;
        ctx.getTxInfo().stat_put_size_bytes = 0;
        ctx.getTxInfo().stat_put_exec_total_ms = 0;
        ctx.getTxInfo().stat_del_cnt = 0;
        ctx.getTxInfo().stat_del_exec_total_ms = 0;
        ctx.getTxInfo().stat_iterate_cnt = 0;
        ctx.getTxInfo().stat_iterate_size_bytes = 0;
        ctx.getTxInfo().stat_iterate_exec_total_ms = 0;
        ctx.getTxInfo().md_tpcc_delivery_skipped = 0;
        ctx.getTxInfo().md_tpcc_delivery_total_order_lines = 0;
        ctx.getTxInfo().md_tpcc_order_status_order_lines = 0;
        ctx.getTxInfo().md_tpcc_stock_level_recent_items = 0;
    }
    
    public void finishTx(Context ctx) {
        ctx.getTxInfo().cc_end_time_epoch_ms = new Date().getTime();
        ctx.getTxInfo().cc_duration_ms = ctx.getTxInfo().cc_end_time_epoch_ms - ctx.getTxInfo().cc_start_time_epoch_ms;
    
        System.out.println(new Date().toInstant().toString() + " | info | txinfo | " + JSON.stringify(ctx.getTxInfo()));
    }

     /**
     * Creates a new entry during the loading phase of the benchmark.
     * @param ctx The TX context.
     * @param parameters The JSON encoded array of entries.
     * @
     */
    public static void createEntries(Context ctx, String parameters) {
        addTxInfo(ctx);
        JSONObject params = new JSONObject(parameters);
        JSONArray entryList = params.getJSONArray("entries");

        //log("Starting Create Entries TX for " + entryList.length + "entries", ctx, "info");

        try {
            for (int i = 0; i < entryList.length(); i++) {
                JSONObject entry = entryList.getJSONObject(i);

                switch (entry.getString("table")) {
                    case common.TABLES.WAREHOUSE:
                        LedgerUtils.createWarehouse(ctx, entry.getJSONObject("data")).get();
                        break;
                    case common.TABLES.DISTRICT:
                        LedgerUtils.createDistrict(ctx, entry.getJSONObject("data")).get();
                        break;
                    case common.TABLES.CUSTOMER:
                        LedgerUtils.createCustomer(ctx, entry.getJSONObject("data")).get();
                        break;
                    case common.TABLES.HISTORY:
                        LedgerUtils.createHistory(ctx, entry.getJSONObject("data")).get();
                        break;
                    case common.TABLES.NEW_ORDER:
                        LedgerUtils.createNewOrder(ctx, entry.getJSONObject("data")).get();
                        break;
                    case common.TABLES.ORDERS:
                        LedgerUtils.createOrder(ctx, entry.getJSONObject("data")).get();
                        break;
                    case common.TABLES.ORDER_LINE:
                        LedgerUtils.createOrderLine(ctx, entry.getJSONObject("data")).get();
                        break;
                    case common.TABLES.ITEM:
                        LedgerUtils.createItem(ctx, entry.getJSONObject("data")).get();
                        break;
                    case common.TABLES.STOCK:
                        LedgerUtils.createStock(ctx, entry.getJSONObject("data")).get();
                        break;
                    default:
                        throw new Exception("Unknown record type: " + entry.getString("table"));
                }
            }

            // log('Finished Create Entries TX', ctx, 'info');
        } catch (Exception err) {
            log(err.toString(), ctx, "error");
            throw err;
        } finally {
            finishTx(ctx);
        }
    }

    /**
     * Performs the Delivery read-write TX profile.
     * @param ctx The TX context.
     * @param parameters The JSON encoded parameters of the TX profile.
     * @return {<{object}>} The JSON encoded query results according to the specification.
     */
    public static Object doDelivery(Context ctx, String parameters) {
        addTxInfo(ctx);

        // TPC-C 2.7.4.2
        // log(`Starting Delivery TX with parameters: ${parameters}`, ctx, 'info');
        try {
            final DeliveryParameters params = ParseUtils.parseDeliveryParameters(parameters);

            // For a given warehouse number (W_ID), for each of the 10 districts (D_W_ID , D_ID)
            // within that warehouse, and for a given carrier number (O_CARRIER_ID):
            List<Object> deliveredOrders = new ArrayList<>();
            int skipped = 0;

            for (int d_id = 1; d_id <= 10; d_id++) {
                // The row in the NEW-ORDER table with matching NO_W_ID (equals W_ID) and NO_D_ID
                // (equals D_ID) and with the lowest NO_O_ID value is selected. This is the oldest
                // undelivered order of that district. NO_O_ID, the order number, is retrieved.
                NewOrder newOrder = LedgerUtils.getOldestNewOrder(ctx, params.w_id, d_id);
                if (newOrder == null) {
                    // If no matching row is found, then the delivery of an order for this district
                    // is skipped. The condition in which no outstanding order is present at a given
                    // district must be handled by skipping the delivery of an order for that district
                    // only and resuming the delivery of an order from all remaining districts of the
                    // selected warehouse. If this condition occurs in more than 1%, or in more than one,
                    // whichever is greater, of the business transactions, it must be reported.
                    log("Could not find new order for District( " + params.w_id + "," + d_id + " ) skipping it" , ctx, "info");
                    skipped += 1;
                    continue;
                }

                LedgerUtils.deleteNewOrder(ctx, newOrder);

                // The row in the ORDER table with matching O_W_ID (equals W_ ID), O_D_ID (equals D_ID),
                // and O_ID (equals NO_O_ID) is selected, O_C_ID, the customer number, is retrieved,
                // and O_CARRIER_ID is updated.
                Order order = LedgerUtils.getOrder(ctx, params.w_id, d_id, newOrder.no_o_id);
                order.o_carrier_id = params.o_carrier_id;
                LedgerUtils.updateOrder(ctx, order);

                // All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID), OL_D_ID
                // (equals O_D_ID), and OL_O_ID (equals O_ID) are selected. All OL_DELIVERY_D, the
                // delivery dates, are updated to the current system time as returned by the operating
                // system and the sum of all OL_AMOUNT is retrieved.
                float orderLineAmountTotal = 0;
                ctx.getTxinfo().md_tpcc_delivery_total_order_lines += order.o_ol_cnt;

                for (int i = 1; i <= order.o_ol_cnt; i++) {
                    OrderLine orderLine = LedgerUtils.getOrderLine(ctx, params.w_id, d_id, order.o_id, i);
                    orderLineAmountTotal += orderLine.ol_amount;
                    orderLine.ol_delivery_d = params.ol_delivery_d;

                    LedgerUtils.updateOrderLine(ctx, orderLine);
                }

                // The row in the CUSTOMER table with matching C_W_ID (equals W_ID), C_D_ID
                // (equals D_ID), and C_ID (equals O_C_ID) is selected and C_BALANCE is increased by
                // the sum of all order-line amounts (OL_AMOUNT) previously retrieved. C_DELIVERY_CNT
                // is incremented by 1.
                Customer customer = LedgerUtils.getCustomer(ctx, params.w_id, d_id, order.o_c_id);
                customer.c_balance += orderLineAmountTotal;
                customer.c_delivery_cnt += 1;
                LedgerUtils.updateCustomer(ctx, customer);

                deliveredOrders.add("d_id", d_id);
                deliveredOrders.add("o_id", order.o_id);
            }
////////////////////////////
            Object output = new Object(params.w_id, params.o_carrier_id, deliveredOrders, skipped);
            // output.w_id = params.w_id;
            // output.o_carrier_id = params.o_carrier_id;
            // output.delivered = deliveredOrders;
            // output.skipped = skipped;
//////////////////////////////////////////////////////
            ctx.getTxinfo().md_tpcc_delivery_skipped = skipped;

            // log(`Finished Delivery TX with output: ${JSON.stringify(output)}`, ctx, 'info');
            return output;
        } catch (Exception err) {
            log(err.toString(), ctx, "error");
            throw err;
        } finally {
            finishTx(ctx);
        }
    }

    /**
     * Performs the New Order read-write TX profile.
     * @param ctx The TX context.
     * @param parameters The JSON encoded parameters of the TX profile.
     * @return {Promise<{object}>} The JSON encoded query results according to the specification.
     */
    public static Object doNewOrder(Context ctx, String parameters) {
        addTxInfo(ctx);

        // TPC-C 2.4.2.2
        // log(`Starting New Order TX with parameters: ${parameters}`, ctx, 'info');
        try {
            final NewOrderParameters params = ParseUtils.parseNewOrderParameters(parameters);

            // The row in the WAREHOUSE table with matching W_ID is selected and W_TAX,
            // the warehouse tax rate, is retrieved.
            final Warehouse warehouse = LedgerUtils.getWarehouse(ctx, params.w_id);

            // The row in the DISTRICT table with matching D_W_ID and D_ ID is selected,
            // D_TAX, the district tax rate, is retrieved, and D_NEXT_O_ID, the next
            // available order number for the district, is retrieved and incremented by one.
            District district = LedgerUtils.getDistrict(ctx, warehouse.w_id, params.d_id);
            int nextOrderId = district.d_next_o_id;
            district.d_next_o_id += 1;
            LedgerUtils.updateDistrict(ctx, district);

            // The row in the CUSTOMER table with matching C_W_ID, C_D_ID, and C_ID is
            // selected and C_DISCOUNT, the customer's discount rate, C_LAST, the customer's
            // last name, and C_CREDIT, the customer's credit status, are retrieved.
            final Customer customer = LedgerUtils.getCustomer(ctx, warehouse.w_id, district.d_id, params.c_id);

            // A new row is inserted into both the NEW-ORDER table and the ORDER table to
            // reflect the creation of the new order. O_CARRIER_ID is set to a null value.
            // If the order includes only home order-lines, then O_ALL_LOCAL is set to 1,
            // otherwise O_ALL_LOCAL is set to 0.
            final NewOrder  newOrder = new NewOrder(); 
                newOrder.no_o_id = nextOrderId;
                newOrder.no_d_id = district.d_id;
                newOrder.no_w_id = warehouse.w_id;
            
            LedgerUtils.createNewOrder(ctx, newOrder);

            boolean allItemsLocal = Arrays.stream(params.i_w_ids).allMatch(i_w_id -> i_w_id == warehouse.w_id);
                        
            //Order order = new Order(nextOrderId, district.d_id, warehouse.w_id, customer.c_id, params.o_entry_d, null, params.i_ids.length, allItemsLocal ? 1 : 0);
            final Order order = new Order();
                order.o_id = nextOrderId;
                order.o_d_id = district.d_id;
                order.o_w_id = warehouse.w_id;
                order.o_c_id = customer.c_id;
                order.o_entry_d = params.o_entry_d;
                order.o_carrier_id = 0;
                order.o_ol_cnt = params.i_ids.length;
                order.o_all_local = allItemsLocal ? 1 : 0;
            
            LedgerUtils.createOrder(ctx, order);

            List<ItemData> itemsData = new ArrayList<>();
            double totalOrderLineAmount = 0.0;
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
                Item item = LedgerUtils.getItem(ctx, i_id);
                if (item == null) {
                    // 2.4.3.4 For transactions that are rolled back as a result of an
                    // unused item number (1% of all New-Order transactions), the emulated
                    // terminal must display in the appropriate fields of the input/output
                    // screen the fields: W_ID, D_ID, C_ID, C_LAST, C_CREDIT, O_ID, and the
                    // execution status message "Item number is not valid".
                    
                                                        //Item errorOutput = new Item(warehouse.w_id, district.d_id, customer.c_id, customer.c_last, customer.c_credit, 
                                                    // order.o_id, "Item number is not valid");
                                                        //Object errorOutput = new Object(warehouse.w_id, district.d_id, customer.c_id, customer.c_last, customer.c_credit, order.o_id, "Item number is not valid");                    
                    Object errorOutput = new Object();
                    errorOutput.w_id = warehouse.w_id;
                    errorOutput.d_id = district.d_id;
                    errorOutput.c_id = customer.c_id;
                    errorOutput.c_last = customer.c_last;
                    errorOutput.c_credit = customer.c_credit;
                    errorOutput.o_id = order.o_id;
                    errorOutput.message = "Item number is not valid";
                    
                    log("Item not found. Error output:" +JSON.stringify(errorOutput) , ctx, "info");
                    throw new Exception(JSON.stringify(errorOutput));
                }

                // The row in the STOCK table with matching S_I_ID (equals OL_I_ID) and
                // S_W_ID (equals OL_SUPPLY_W_ID) is selected. S_QUANTITY, the quantity in
                // stock, S_DIST_xx, where xx represents the district number, and S_DATA are
                // retrieved. If the retrieved value for S_QUANTITY exceeds OL_QUANTITY by
                // 10 or more, then S_QUANTITY is decreased by OL_QUANTITY; otherwise
                // S_QUANTITY is updated to (S_QUANTITY - OL_QUANTITY)+91. S_YTD is
                // increased by OL_QUANTITY and S_ORDER_CNT is incremented by 1. If the
                // order-line is remote, then S_REMOTE_CNT is incremented by 1.
                Stock stock = LedgerUtils.getStock(ctx, i_w_id, i_id);

                if (stock.s_quantity >= i_qty + 10) {
                    stock.s_quantity -= i_qty;
                } 
                else {
                    stock.s_quantity = (stock.s_quantity - i_qty) + 91;
                }

                stock.s_ytd += i_qty;
                stock.s_order_cnt += 1;

                if (i_w_id != warehouse.w_id) {
                    stock.s_remote_cnt += 1;
                }

                LedgerUtils.updateStock(ctx, stock);

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
                
                //final OrderLine orderLine = new OrderLine(nextOrderId, district.d_id, warehouse.w_id, i + 1, i_id, i_w_id, null, i_qty, orderLineAmount, "s_dist_" + stockDistrictId);
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
                
                LedgerUtils.createOrderLine(ctx, orderLine);

                // 2.4.3.3 The emulated terminal must display, in the appropriate fields of
                // the input/ output screen, all input data and the output data resulting
                // from the execution of the transaction. The display field s are divided in
                // two groups as follows:
                // - One repeating group of fields: OL_SUPPLY_W_ID, OL_I_ID, I_NAME,
                // OL_QUANTITY, S_QUANTITY, brand_generic, I_PRICE, and OL_AMOUNT. The group
                // is repeated O_OL_CNT times (once per item in the order), equal to the
                // computed value of ol_cnt.
                itemsData.add("ol_supply_w_id" , orderLine.ol_supply_w_id);
                itemsData.add("ol_i_id" , orderLine.ol_i_id);
                itemsData.add("i_name" , item.i_name);
                itemsData.add("ol_quantity", orderLine.ol_quantity);
                itemsData.add("s_quantity", stock.s_quantity);
                itemsData.add("brand_generic", brandGeneric);
                itemsData.add("i_price", item.i_price);
                itemsData.add("ol_amount", orderLine.ol_amount);
                
            }

            // The total-amount for the complete order is computed as:
            // sum(OL_AMOUNT) * (1 - C_DISCOUNT) * (1 + W_TAX + D_TAX)
            Double  totalAmount = totalOrderLineAmount * (1 - customer.c_discount) * (1 + warehouse.w_tax + district.d_tax);

            // 2.4.3.3 The emulated terminal must display, in the appropriate fields of
            // the input/ output screen, all input data and the output data resulting
            // from the execution of the transaction. The display field s are divided in
            // two groups as follows:
            // - One non-repeating group of fields: W_ID, D_ID, C_ID, O_ID, O_OL_CNT,
            // C_LAST, C_CREDIT, C_DISCOUNT, W_TAX, D_TAX, O_ENTRY_D, total_amount, and an
            // optional execution status message other than "Item number is not valid".
            
/////////////////////////////////////////////////////////////////////
            Object output = new Object(warehouse.w_id, district.d_id, customer.c_id, customer.c_last,            
            customer.c_credit,customer.c_discount, warehouse.w_tax,district.d_tax,order.o_ol_cnt,order.o_id,
            order.o_entry_d,totalAmount,itemsData
            );

            // log(`Finished New Order TX with output: ${JSON.stringify(output)}`, ctx, 'info');
            return output;
        } catch (Exception err) {
            log(err.toString(), ctx, "error");
            throw err;
        } finally {
            finishTx(ctx);
        }
    }
}








