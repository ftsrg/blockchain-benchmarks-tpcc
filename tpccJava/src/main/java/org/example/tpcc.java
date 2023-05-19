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

import main.java.org.example.LedgerUtils;
import main.java.org.example.ParseUtils;
import main.java.org.example.common;

// import org.hyperledger.fabric.contract.Context;
// import org.hyperledger.fabric.contract.ContractInterface;
// import org.hyperledger.fabric.contract.annotation.Contract;
// import org.hyperledger.fabric.contract.annotation.Default;
// import org.hyperledger.fabric.contract.annotation.Transaction;
// import org.hyperledger.fabric.contract.annotation.Info;
// import org.hyperledger.fabric.contract.annotation.License;
// import static java.nio.charset.StandardCharsets.UTF_8;
import org.hyperledger.fabric.contract.Contract;
import java.util.Date;






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
    public static createEntries(Context ctx, String parameters) {
        addTxInfo(ctx);
        JSONObject params = new JSONObject(parameters);
        JSONArray entryList = params.getJSONArray("entries");

        //log("Starting Create Entries TX for " + entryList.length + "entries", ctx, "info");

        try {
            for (int i = 0; i < entryList.length(); i++) {
                JSONObject entry = entryList.getJSONObject(i);

                switch (entry.getString("table")) {
                    case TABLES.WAREHOUSE:
                        LedgerUtils.createWarehouse(ctx, entry.getJSONObject("data")).get();
                        break;
                    case TABLES.DISTRICT:
                        LedgerUtils.createDistrict(ctx, entry.getJSONObject("data")).get();
                        break;
                    case TABLES.CUSTOMER:
                        LedgerUtils.createCustomer(ctx, entry.getJSONObject("data")).get();
                        break;
                    case TABLES.HISTORY:
                        LedgerUtils.createHistory(ctx, entry.getJSONObject("data")).get();
                        break;
                    case TABLES.NEW_ORDER:
                        LedgerUtils.createNewOrder(ctx, entry.getJSONObject("data")).get();
                        break;
                    case TABLES.ORDERS:
                        LedgerUtils.createOrder(ctx, entry.getJSONObject("data")).get();
                        break;
                    case TABLES.ORDER_LINE:
                        LedgerUtils.createOrderLine(ctx, entry.getJSONObject("data")).get();
                        break;
                    case TABLES.ITEM:
                        LedgerUtils.createItem(ctx, entry.getJSONObject("data")).get();
                        break;
                    case TABLES.STOCK:
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
            Object params = ParseUtils.parseDeliveryParameters(parameters);

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
                Double orderLineAmountTotal = 0;
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

            output.w_id = params.w_id;
            output.o_carrier_id = params.o_carrier_id;
            output.delivered = deliveredOrders;
            output.skipped = skipped;

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
}








