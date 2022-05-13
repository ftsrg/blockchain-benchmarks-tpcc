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

'use strict';

const Contract = require('fabric-contract-api').Contract;
const LedgerUtils = require('./ledgerUtils');
const ParseUtils = require('./parseUtils');
const {TABLES, log} = require('./common');

function addTxInfo(ctx) {
    ctx.txinfo = {
        tx_id: ctx.stub.getTxID(),
        cc_start_time_epoch_ms: Date.now(),
        cc_end_time_epoch_ms: 0,
        cc_duration_ms: 0,
        stat_get_cnt: 0,
        stat_get_size_bytes: 0,
        stat_get_exec_total_ms: 0,
        stat_put_cnt: 0,
        stat_put_size_bytes: 0,
        stat_put_exec_total_ms: 0,
        stat_del_cnt: 0,
        stat_del_exec_total_ms: 0,
        stat_iterate_cnt: 0,
        stat_iterate_size_bytes: 0,
        stat_iterate_exec_total_ms: 0,
        md_tpcc_delivery_skipped: 0,
        md_tpcc_delivery_total_order_lines: 0,
        md_tpcc_order_status_order_lines: 0,
        md_tpcc_stock_level_recent_items: 0
    };
}

function finishTx(ctx) {
    ctx.txinfo.cc_end_time_epoch_ms = Date.now();
    ctx.txinfo.cc_duration_ms = ctx.txinfo.cc_end_time_epoch_ms - ctx.txinfo.cc_start_time_epoch_ms;

    console.log(`${(new Date()).toISOString()} | info | txinfo | ${JSON.stringify(ctx.txinfo)}`);
}

/**
 * The implementation of the TPC-C benchmark smart contract according to the specification version v5.11.0.
 */
class TPCC extends Contract {
    /**
     * Initializes the contract class.
     */
    constructor() {
        super("org.tpcc.record");
    }

    /**
     * Creates a new entry during the loading phase of the benchmark.
     * @param {Context} ctx The TX context.
     * @param {string} parameters The JSON encoded array of entries.
     * @async
     */
    async createEntries(ctx, parameters) {
        addTxInfo(ctx);

        let params = JSON.parse(parameters);

        let entryList = params.entries;
        // log(`Starting Create Entries TX for ${entryList.length} entries`, ctx, 'info');

        try {
            for (let entry of entryList) {
                switch (entry.table) {
                    case TABLES.WAREHOUSE:
                        await LedgerUtils.createWarehouse(ctx, entry.data);
                        break;
                    case TABLES.DISTRICT:
                        await LedgerUtils.createDistrict(ctx, entry.data);
                        break;
                    case TABLES.CUSTOMER:
                        await LedgerUtils.createCustomer(ctx, entry.data);
                        break;
                    case TABLES.HISTORY:
                        await LedgerUtils.createHistory(ctx, entry.data);
                        break;
                    case TABLES.NEW_ORDER:
                        await LedgerUtils.createNewOrder(ctx, entry.data);
                        break;
                    case TABLES.ORDERS:
                        await LedgerUtils.createOrder(ctx, entry.data);
                        break;
                    case TABLES.ORDER_LINE:
                        await LedgerUtils.createOrderLine(ctx, entry.data);
                        break;
                    case TABLES.ITEM:
                        await LedgerUtils.createItem(ctx, entry.data);
                        break;
                    case TABLES.STOCK:
                        await LedgerUtils.createStock(ctx, entry.data);
                        break;
                    default:
                        throw new Error(`Unknown record type: ${entry.table}`);
                }
            }

            // log('Finished Create Entries TX', ctx, 'info');
        } catch (err) {
            log(err.stack.toString() || err.toString(), ctx, 'error');
            throw err;
        } finally {
            finishTx(ctx);
        }
    }

    /**
     * Performs the Delivery read-write TX profile.
     * @param {Context} ctx The TX context.
     * @param {string} parameters The JSON encoded parameters of the TX profile.
     * @return {Promise<{object}>} The JSON encoded query results according to the specification.
     */
    async doDelivery(ctx, parameters) {
        addTxInfo(ctx);

        // TPC-C 2.7.4.2
        // log(`Starting Delivery TX with parameters: ${parameters}`, ctx, 'info');
        try {
            const params = ParseUtils.parseDeliveryParameters(parameters);

            // For a given warehouse number (W_ID), for each of the 10 districts (D_W_ID , D_ID)
            // within that warehouse, and for a given carrier number (O_CARRIER_ID):
            let deliveredOrders = [];
            let skipped = 0;
            for (let d_id = 1; d_id <= 10; d_id++) {
                // The row in the NEW-ORDER table with matching NO_W_ID (equals W_ID) and NO_D_ID
                // (equals D_ID) and with the lowest NO_O_ID value is selected. This is the oldest
                // undelivered order of that district. NO_O_ID, the order number, is retrieved.
                let newOrder = await LedgerUtils.getOldestNewOrder(ctx, params.w_id, d_id);
                if (!newOrder) {
                    // If no matching row is found, then the delivery of an order for this district
                    // is skipped. The condition in which no outstanding order is present at a given
                    // district must be handled by skipping the delivery of an order for that district
                    // only and resuming the delivery of an order from all remaining districts of the
                    // selected warehouse. If this condition occurs in more than 1%, or in more than one,
                    // whichever is greater, of the business transactions, it must be reported.
                    log(`Could not find new order for District(${params.w_id}, ${d_id}), skipping it`, ctx, 'info');
                    skipped += 1;
                    continue;
                }

                await LedgerUtils.deleteNewOrder(ctx, newOrder);

                // The row in the ORDER table with matching O_W_ID (equals W_ ID), O_D_ID (equals D_ID),
                // and O_ID (equals NO_O_ID) is selected, O_C_ID, the customer number, is retrieved,
                // and O_CARRIER_ID is updated.
                let order = await LedgerUtils.getOrder(ctx, params.w_id, d_id, newOrder.no_o_id);
                order.o_carrier_id = params.o_carrier_id;
                await LedgerUtils.updateOrder(ctx, order);

                // All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID), OL_D_ID
                // (equals O_D_ID), and OL_O_ID (equals O_ID) are selected. All OL_DELIVERY_D, the
                // delivery dates, are updated to the current system time as returned by the operating
                // system and the sum of all OL_AMOUNT is retrieved.
                let orderLineAmountTotal = 0;
                ctx.txinfo.md_tpcc_delivery_total_order_lines += order.o_ol_cnt;
                for (let i = 1; i <= order.o_ol_cnt; i++) {
                    let orderLine = await LedgerUtils.getOrderLine(ctx, params.w_id, d_id, order.o_id, i);
                    orderLineAmountTotal += orderLine.ol_amount;
                    orderLine.ol_delivery_d = params.ol_delivery_d;

                    await LedgerUtils.updateOrderLine(ctx, orderLine);
                }

                // The row in the CUSTOMER table with matching C_W_ID (equals W_ID), C_D_ID
                // (equals D_ID), and C_ID (equals O_C_ID) is selected and C_BALANCE is increased by
                // the sum of all order-line amounts (OL_AMOUNT) previously retrieved. C_DELIVERY_CNT
                // is incremented by 1.
                let customer = await LedgerUtils.getCustomer(ctx, params.w_id, d_id, order.o_c_id);
                customer.c_balance += orderLineAmountTotal;
                customer.c_delivery_cnt += 1;
                await LedgerUtils.updateCustomer(ctx, customer);

                deliveredOrders.push({
                    d_id: d_id,
                    o_id: order.o_id
                });
            }

            let output = {
                w_id: params.w_id,
                o_carrier_id: params.o_carrier_id,
                delivered: deliveredOrders,
                skipped: skipped
            };

            ctx.txinfo.md_tpcc_delivery_skipped = skipped;

            // log(`Finished Delivery TX with output: ${JSON.stringify(output)}`, ctx, 'info');
            return output;
        } catch (err) {
            log(err.stack.toString() || err.toString(), ctx, 'error');
            throw err;
        } finally {
            finishTx(ctx);
        }
    }

    /**
     * Performs the New Order read-write TX profile.
     * @param {Context} ctx The TX context.
     * @param {string} parameters The JSON encoded parameters of the TX profile.
     * @return {Promise<{object}>} The JSON encoded query results according to the specification.
     */
    async doNewOrder(ctx, parameters) {
        addTxInfo(ctx);

        // TPC-C 2.4.2.2
        // log(`Starting New Order TX with parameters: ${parameters}`, ctx, 'info');
        try {
            const params = ParseUtils.parseNewOrderParameters(parameters);

            // The row in the WAREHOUSE table with matching W_ID is selected and W_TAX,
            // the warehouse tax rate, is retrieved.
            const warehouse = await LedgerUtils.getWarehouse(ctx, params.w_id);

            // The row in the DISTRICT table with matching D_W_ID and D_ ID is selected,
            // D_TAX, the district tax rate, is retrieved, and D_NEXT_O_ID, the next
            // available order number for the district, is retrieved and incremented by one.
            let district = await LedgerUtils.getDistrict(ctx, warehouse.w_id, params.d_id);
            const nextOrderId = district.d_next_o_id;
            district.d_next_o_id += 1;
            await LedgerUtils.updateDistrict(ctx, district);

            // The row in the CUSTOMER table with matching C_W_ID, C_D_ID, and C_ID is
            // selected and C_DISCOUNT, the customer's discount rate, C_LAST, the customer's
            // last name, and C_CREDIT, the customer's credit status, are retrieved.
            const customer = await LedgerUtils.getCustomer(ctx, warehouse.w_id, district.d_id, params.c_id);

            // A new row is inserted into both the NEW-ORDER table and the ORDER table to
            // reflect the creation of the new order. O_CARRIER_ID is set to a null value.
            // If the order includes only home order-lines, then O_ALL_LOCAL is set to 1,
            // otherwise O_ALL_LOCAL is set to 0.
            const newOrder = {
                no_o_id: nextOrderId,
                no_d_id: district.d_id,
                no_w_id: warehouse.w_id
            };
            await LedgerUtils.createNewOrder(ctx, newOrder);

            let allItemsLocal = params.i_w_ids.every(i_w_id => i_w_id === warehouse.w_id);
            const order = {
                o_id: nextOrderId,
                o_d_id: district.d_id,
                o_w_id: warehouse.w_id,
                o_c_id: customer.c_id,
                o_entry_d: params.o_entry_d,
                o_carrier_id: undefined,
                o_ol_cnt: params.i_ids.length,
                o_all_local: allItemsLocal ? 1 : 0
            };

            await LedgerUtils.createOrder(ctx, order);

            let itemsData = [];
            let totalOrderLineAmount = 0.0;
            // For each O_OL_CNT item on the order
            for (let i = 0; i < params.i_ids.length; i++) {
                let i_id = params.i_ids[i];
                let i_w_id = params.i_w_ids[i];
                let i_qty = params.i_qtys[i];

                // The row in the ITEM table with matching I_ID (equals OL_I_ID) is selected
                // and I_PRICE, the price of the item, I_NAME, the name of the item, and
                // I_DATA are retrieved. If I_ID has an unused value (see Clause 2.4.1.5), a
                // "not-found" condition is signaled, resulting in a rollback of the
                // database transaction (see Clause 2.4.2.3).
                let item = await LedgerUtils.getItem(ctx, i_id);
                if (!item) {
                    // 2.4.3.4 For transactions that are rolled back as a result of an
                    // unused item number (1% of all New-Order transactions), the emulated
                    // terminal must display in the appropriate fields of the input/output
                    // screen the fields: W_ID, D_ID, C_ID, C_LAST, C_CREDIT, O_ID, and the
                    // execution status message "Item number is not valid".
                    let errorOutput = {
                        w_id: warehouse.w_id,
                        d_id: district.d_id,
                        c_id: customer.c_id,
                        c_last: customer.c_last,
                        c_credit: customer.c_credit,
                        o_id: order.o_id,
                        message: 'Item number is not valid'
                    };

                    log(`Item not found. Error output: ${JSON.stringify(errorOutput)}`, ctx, 'info');
                    throw new Error(JSON.stringify(errorOutput));
                }

                // The row in the STOCK table with matching S_I_ID (equals OL_I_ID) and
                // S_W_ID (equals OL_SUPPLY_W_ID) is selected. S_QUANTITY, the quantity in
                // stock, S_DIST_xx, where xx represents the district number, and S_DATA are
                // retrieved. If the retrieved value for S_QUANTITY exceeds OL_QUANTITY by
                // 10 or more, then S_QUANTITY is decreased by OL_QUANTITY; otherwise
                // S_QUANTITY is updated to (S_QUANTITY - OL_QUANTITY)+91. S_YTD is
                // increased by OL_QUANTITY and S_ORDER_CNT is incremented by 1. If the
                // order-line is remote, then S_REMOTE_CNT is incremented by 1.
                let stock = await LedgerUtils.getStock(ctx, i_w_id, i_id);
                if (stock.s_quantity >= i_qty + 10) {
                    stock.s_quantity -= i_qty;
                } else {
                    stock.s_quantity = (stock.s_quantity - i_qty) + 91;
                }

                stock.s_ytd += i_qty;
                stock.s_order_cnt += 1;
                if (i_w_id !== warehouse.w_id) {
                    stock.s_remote_cnt += 1;
                }

                await LedgerUtils.updateStock(ctx, stock);

                // The amount for the item in the order (OL_AMOUNT) is computed as:
                // OL_QUANTITY * I_PRICE
                let orderLineAmount = i_qty * item.i_price;
                totalOrderLineAmount += orderLineAmount;

                // The strings in I_DATA and S_DATA are examined. If they both include the
                // string "ORIGINAL", the brand-generic field for that item is set to "B",
                // otherwise, the brand-generic field is set to "G".
                let brandGeneric;
                if (item.i_data.includes('ORIGINAL') && stock.s_data.includes('ORIGINAL')) {
                    brandGeneric = 'B';
                } else {
                    brandGeneric = 'G';
                }

                // A new row is inserted into the ORDER-LINE table to reflect the item on
                // the order. OL_DELIVERY_D is set to a null value, OL_NUMBER is set to a
                // unique value within all the ORDER-LINE rows that have the same OL_O_ID
                // value, and OL_DIST_INFO is set to the content of S_DIST_xx, where xx
                // represents the district number (OL_D_ID)
                let stockDistrictId = district.d_id.toString().padStart(2, '0');
                const orderLine = {
                    ol_o_id: nextOrderId,
                    ol_d_id: district.d_id,
                    ol_w_id: warehouse.w_id,
                    ol_number: i + 1,
                    ol_i_id: i_id,
                    ol_supply_w_id: i_w_id,
                    ol_delivery_d: undefined,
                    ol_quantity: i_qty,
                    ol_amount: orderLineAmount,
                    ol_dist_info: stock[`s_dist_${stockDistrictId}`]
                };
                await LedgerUtils.createOrderLine(ctx, orderLine);

                // 2.4.3.3 The emulated terminal must display, in the appropriate fields of
                // the input/ output screen, all input data and the output data resulting
                // from the execution of the transaction. The display field s are divided in
                // two groups as follows:
                // - One repeating group of fields: OL_SUPPLY_W_ID, OL_I_ID, I_NAME,
                // OL_QUANTITY, S_QUANTITY, brand_generic, I_PRICE, and OL_AMOUNT. The group
                // is repeated O_OL_CNT times (once per item in the order), equal to the
                // computed value of ol_cnt.
                itemsData.push({
                    ol_supply_w_id: orderLine.ol_supply_w_id,
                    ol_i_id: orderLine.ol_i_id,
                    i_name: item.i_name,
                    ol_quantity: orderLine.ol_quantity,
                    s_quantity: stock.s_quantity,
                    brand_generic: brandGeneric,
                    i_price: item.i_price,
                    ol_amount: orderLine.ol_amount
                });
            }

            // The total-amount for the complete order is computed as:
            // sum(OL_AMOUNT) * (1 - C_DISCOUNT) * (1 + W_TAX + D_TAX)
            let totalAmount = totalOrderLineAmount * (1 - customer.c_discount) * (1 + warehouse.w_tax + district.d_tax);

            // 2.4.3.3 The emulated terminal must display, in the appropriate fields of
            // the input/ output screen, all input data and the output data resulting
            // from the execution of the transaction. The display field s are divided in
            // two groups as follows:
            // - One non-repeating group of fields: W_ID, D_ID, C_ID, O_ID, O_OL_CNT,
            // C_LAST, C_CREDIT, C_DISCOUNT, W_TAX, D_TAX, O_ENTRY_D, total_amount, and an
            // optional execution status message other than "Item number is not valid".
            let output = {
                w_id: warehouse.w_id,
                d_id: district.d_id,
                c_id: customer.c_id,
                c_last: customer.c_last,
                c_credit: customer.c_credit,
                c_discount: customer.c_discount,
                w_tax: warehouse.w_tax,
                d_tax: district.d_tax,
                o_ol_cnt: order.o_ol_cnt,
                o_id: order.o_id,
                o_entry_d: order.o_entry_d,
                total_amount: totalAmount,
                items: itemsData
            };

            // log(`Finished New Order TX with output: ${JSON.stringify(output)}`, ctx, 'info');
            return output;
        } catch (err) {
            log(err.stack.toString() || err.toString(), ctx, 'error');
            throw err;
        } finally {
            finishTx(ctx);
        }
    }

    /**
     * Performs the Order Status read TX profile.
     * @param {Context} ctx The TX context.
     * @param {string} parameters The JSON encoded parameters of the TX profile.
     * @return {Promise<{object}>} The JSON encoded query results according to the specification.
     */
    async doOrderStatus(ctx, parameters) {
        addTxInfo(ctx);

        // TPC-C 2.6.2.2
        // log(`Starting Order Status TX with parameters: ${parameters}`, ctx, 'info');
        try {
            const params = ParseUtils.parseOrderStatusParameters(parameters);

            let customer = await LedgerUtils.getCustomersByIdOrLastName(ctx, params.w_id, params.d_id, params.c_id, params.c_last);

            // The row in the ORDER table with matching O_W_ID (equals C_W_ID), O_D_ID (equals C_D_ID),
            // O_C_ID (equals C_ID), and with the largest existing O_ID, is selected. This is the most
            // recent order placed by that customer. O_ID, O_ENTRY_D, and O_CARRIER_ID are retrieved.
            let order = await LedgerUtils.getLastOrderOfCustomer(ctx, customer.c_w_id, customer.c_d_id, customer.c_id);

            // All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID), OL_D_ID (equals
            // O_D_ID), and OL_O_ID (equals O_ID) are selected and the corresponding sets of OL_I_ID,
            // OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, and OL_DELIVERY_D are retrieved.
            let orderLineData = [];
            ctx.txinfo.md_tpcc_order_status_order_lines = order.o_ol_cnt;
            for (let i = 1; i <= order.o_ol_cnt; i++) {
                let orderLine = await LedgerUtils.getOrderLine(ctx, order.o_w_id, order.o_d_id, order.o_id, i);
                orderLineData.push({
                    ol_supply_w_id: orderLine.ol_supply_w_id,
                    ol_i_id: orderLine.ol_i_id,
                    ol_quantity: orderLine.ol_quantity,
                    ol_amount: orderLine.ol_amount,
                    ol_delivery_d: orderLine.ol_delivery_d
                });
            }

            // 2.6.3.3 The emulated terminal must display, in the appropriate fields of the input/output
            // screen, all input data and the output data resulting from the execution of the
            // transaction. The display fields are divided in two groups as follows:
            // - One non-repeating group of fields: W_ID, D_ID, C_ID, C_FIRST, C_MIDDLE, C_LAST,
            //   C_BALANCE, O_ID, O_ENTRY_D, and O_CARRIER_ID;
            // - One repeating group of fields: OL_SUPPLY_W_ID, OL_I_ID, OL_QUANTITY, OL_AMOUNT, and
            //   OL_DELIVERY_D. The group is repeated O_OL_CNT times (once per item in the order).
            let output = {
                w_id: params.w_id,
                d_id: params.d_id,
                c_id: customer.c_id,
                c_first: customer.c_first,
                c_middle: customer.c_middle,
                c_last: customer.c_last,
                c_balance: customer.c_balance,
                o_id: order.o_id,
                o_entry_d: order.o_entry_d,
                o_carrier_id: order.o_carrier_id,
                order_lines: orderLineData
            };

            // log(`Finished Order Status TX with output: ${JSON.stringify(output)}`, ctx, 'info');
            return output;
        } catch (err) {
            log(err.stack.toString() || err.toString(), ctx, 'error');
            throw err;
        } finally {
            finishTx(ctx);
        }
    }

    /**
     * Performs the Payment read-write TX profile.
     * @param {Context} ctx The TX context.
     * @param {string} parameters The JSON encoded parameters of the TX profile.
     * @return {Promise<{object}>} The JSON encoded query results according to the specification.
     */
    async doPayment(ctx, parameters) {
        addTxInfo(ctx);

        // TPC-C 2.5.2.2
        // log(`Starting Payment TX with parameters: ${parameters}`, ctx, 'info');
        try {
            const params = ParseUtils.parsePaymentParameters(parameters);

            // The row in the WAREHOUSE table with matching W_ID is selected. W_NAME,
            // W_STREET_1, W_STREET_2, W_CITY, W_STATE, and W_ZIP are retrieved and W_YTD, the
            // warehouse's year-to-date balance, is increased by H_ AMOUNT.
            let warehouse = await LedgerUtils.getWarehouse(ctx, params.w_id);
            warehouse.w_ytd += params.h_amount;
            await LedgerUtils.updateWarehouse(ctx, warehouse);

            // The row in the DISTRICT table with matching D_W_ID and D_ID is selected.
            // D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE, and D_ZIP are retrieved and
            // D_YTD, the district's year-to-date balance, is increased by H_AMOUNT.
            let district = await LedgerUtils.getDistrict(ctx, warehouse.w_id, params.d_id);
            district.d_ytd += params.h_amount;
            await LedgerUtils.updateDistrict(ctx, district);

            let customer = await LedgerUtils.getCustomersByIdOrLastName(ctx, warehouse.w_id, district.d_id, params.c_id, params.c_last);

            // C_BALANCE is decreased by H_AMOUNT. C_YTD_PAYMENT is increased by H_AMOUNT.
            // C_PAYMENT_CNT is incremented by 1.
            customer.c_balance -= params.h_amount;
            customer.c_ytd_payment += params.h_amount;
            customer.c_payment_cnt += 1;

            // If the value of C_CREDIT is equal to "BC", then C_DATA is also retrieved from
            // the selected customer and the following history information: C_ID, C_D_ID,
            // C_W_ID, D_ID, W_ID, and H_AMOUNT, are inserted at the left of the C_DATA field
            // by shifting the existing content of C_DATA to the right by an equal number of
            // bytes and by discarding the bytes that are shifted out of the right side of the
            // C_DATA field. The content of the C_DATA field never exceeds 500 characters. The
            // selected customer is updated with the new C_DATA field.
            if (customer.c_credit === 'BC') {
                const history = [customer.c_id, customer.c_d_id, customer.c_w_id, district.d_id, warehouse.w_id, params.h_amount].join(' ');
                customer.c_data = history + '|' + customer.c_data;
                if (customer.c_data.length > 500)
                    customer.c_data = customer.c_data.slice(0, 500);
            }

            await LedgerUtils.updateCustomer(ctx, customer);

            // H_DATA is built by concatenating W_NAME and D_NAME separated by 4 spaces.
            let h_data = warehouse.w_name + ' '.repeat(4) + district.d_name;

            // A new row is inserted into the HISTORY table with H_C_ID = C_ID,
            // H_C_D_ID = C_D_ID, H_C_W_ID = C_W_ID, H_D_ID = D_ID, and H_W_ID = W_ID.
            const history = {
                h_c_id: customer.c_id,
                h_c_d_id: customer.c_d_id,
                h_c_w_id: customer.c_w_id,
                h_d_id: district.d_id,
                h_w_id: warehouse.w_id,
                h_date: params.h_date,
                h_amount: params.h_amount,
                h_data: h_data
            };
            await LedgerUtils.createHistory(ctx, history);

            // 2.5.3.3 The emulated terminal must display, in the appropriate fields of the
            // input/output screen, all input data and the output data resulting from the
            // execution of the transaction. The following fields are displayed: W_ID, D_ID,
            // C_ID, C_D_ID, C_W_ID, W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP,
            // D_STREET_1, D_STREET_2, D_CITY, D_STATE, D_ZIP, C_FIRST, C_MIDDLE, C_LAST,
            // C_STREET_1, C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT,
            // C_CREDIT_LIM, C_DISCOUNT, C_BALANCE, the first 200 characters of C_DATA (only
            // if C_CREDIT = "BC"), H_AMOUNT, and H_DATE.

            let output = {
                w_id: warehouse.w_id,
                d_id: district.d_id,
                c_id: customer.c_id,
                c_d_id: customer.c_d_id,
                c_w_id: customer.c_w_id,
                h_amount: history.h_amount,
                h_date: history.h_date,
                w_street_1: warehouse.w_street_1,
                w_street_2: warehouse.w_street_2,
                w_city: warehouse.w_city,
                w_state: warehouse.w_state,
                w_zip: warehouse.w_zip,
                d_street_1: district.d_street_1,
                d_street_2: district.d_street_2,
                d_city: district.d_city,
                d_state: district.d_state,
                d_zip: district.d_zip,
                c_first: customer.c_first,
                c_middle: customer.c_middle,
                c_last: customer.c_last,
                c_street_1: customer.c_street_1,
                c_street_2: customer.c_street_2,
                c_city: customer.c_city,
                c_state: customer.c_state,
                c_zip: customer.c_zip,
                c_phone: customer.c_phone,
                c_since: customer.c_since,
                c_credit: customer.c_credit,
                c_credit_lim: customer.c_credit_lim,
                c_discount: customer.c_discount,
                c_balance: customer.c_balance
            };

            if (customer.c_credit === 'BC') {
                output.c_data = customer.c_data.substring(0, 200);
            }

            // log(`Finished Payment TX with output: ${JSON.stringify(output)}`, ctx, 'info');
            return output;
        } catch (err) {
            log(err.stack.toString() || err.toString(), ctx, 'error');
            throw err;
        } finally {
            finishTx(ctx);
        }
    }

    /**
     * Performs the Stock Level read TX profile.
     * @param {Context} ctx The TX context.
     * @param {string} parameters The JSON encoded parameters of the TX profile.
     * @return {Promise<{object}>} The JSON encoded query results according to the specification.
     */
    async doStockLevel(ctx, parameters) {
        addTxInfo(ctx);

        // TPC-C 2.8.2.2
        // log(`Starting Stock Level TX with parameters: ${parameters}`, ctx, 'info');
        try {
            const params = ParseUtils.parseStockLevelParameters(parameters);

            // The row in the DISTRICT table with matching D_W_ID and D_ID is selected and D_NEXT_O_ID
            // is retrieved.
            const district = await LedgerUtils.getDistrict(ctx, params.w_id, params.d_id);

            // All rows in the ORDER-LINE table with matching OL_W_ID (equals W_ID), OL_D_ID (equals
            // D_ID), and OL_O_ID (lower than D_NEXT_O_ID and greater than or equal to D_NEXT_O_ID
            // minus 20) are selected. They are the items for 20 recent orders of the district.
            const o_id_min = district.d_next_o_id - 20;
            const o_id_max = district.d_next_o_id;
            let recentItemIds = await LedgerUtils.getItemIdsOfRecentOrders(ctx, params.w_id, district.d_id, o_id_min, o_id_max);
            ctx.txinfo.md_tpcc_stock_level_recent_items = recentItemIds.length;

            // All rows in the STOCK table with matching S_I_ID (equals OL_I_ID) and S_W_ID (equals W_ID)
            // from the list of distinct item numbers and with S_QUANTITY lower than threshold are counted
            // (giving low_stock).
            let lowStock = 0;
            for (let i_id of recentItemIds) {
                let stock = await LedgerUtils.getStock(ctx, params.w_id, i_id);
                if (stock.s_quantity < params.threshold) {
                    lowStock += 1;
                }
            }

            // 2.8.3.3 The emulated terminal must display, in the appropriate field of the input/
            // output screen, all input data and the output data which results from the execution of
            // the transaction. The following fields are displayed: W_ID, D_ID, threshold, and low_stock.
            let output = {
                w_id: params.w_id,
                d_id: params.d_id,
                threshold: params.threshold,
                low_stock: lowStock
            };

            // log(`Finished Stock Level TX with output: ${JSON.stringify(output)}`, ctx, 'info');
            return output;
        } catch (err) {
            log(err.stack.toString() || err.toString(), ctx, 'error');
            throw err;
        } finally {
            finishTx(ctx);
        }
    }

    /**
     * Initializes the TPC-C chaincode.
     * @param {Context} ctx The TX context.
     */
    async instantiate(ctx) {
        log('Instantiating TPC-C chaincode', ctx);
    }
}

module.exports = TPCC;
