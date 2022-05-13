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

const { TABLES, log, pad } = require('./common');
const ParseUtils = require('./parseUtils');

/**
 * Utility functions for accessing the state database.
 */
class LedgerUtils {
    /**
     * LOW-LEVEL API
     */

    /**
     * Adds a new record to the state database. Throws an error if it already exists.
     * @param {Context} ctx The TX context.
     * @param {string} type The type of the record.
     * @param {string[]} keyParts The parts of the record's primary key.
     * @param {string|object} entry The JSON string/object representation of the record.
     * @async
     */
    static async createEntry(ctx, type, keyParts, entry) {
        let key = ctx.stub.createCompositeKey(type, keyParts);
        entry = typeof entry === 'string' ? entry : JSON.stringify(entry);
        let buffer = Buffer.from(entry);

        const start = Date.now();
        await ctx.stub.putState(key, buffer);
        const end = Date.now();

        ctx.txinfo.stat_put_cnt += 1;
        ctx.txinfo.stat_put_size_bytes += buffer.length;
        ctx.txinfo.stat_put_exec_total_ms += end - start;

        // log(`Created entry: ${key}`, ctx);
        // log(`With value: ${entry}`, ctx);
    }

    /**
     * Updates a record in the state database.
     * @param {Context} ctx The TX context.
     * @param {string} type The type of the record.
     * @param {string[]} keyParts The parts of the record's primary key.
     * @param {string|object} entry The JSON string/object representation of the record.
     * @async
     */
    static async updateEntry(ctx, type, keyParts, entry) {
        let key = ctx.stub.createCompositeKey(type, keyParts);
        entry = typeof entry === 'string' ? entry : JSON.stringify(entry);
        let buffer = Buffer.from(entry);

        const start = Date.now();
        await ctx.stub.putState(key, buffer);
        const end = Date.now();

        ctx.txinfo.stat_put_cnt += 1;
        ctx.txinfo.stat_put_size_bytes += buffer.length;
        ctx.txinfo.stat_put_exec_total_ms += end - start;

        // log(`Updated entry: ${key}`, ctx);
        // log(`New value: ${entry}`, ctx);
    }

    /**
     * Retrieves the given record from the state database.
     * @param {Context} ctx The TX context.
     * @param {string} type The type of the record.
     * @param {string[]} keyParts The attribute values as key parts.
     * @return {Promise<string>} The retrieved data or undefined if not found.
     * @async
     */
    static async getEntry(ctx, type, keyParts) {
        let key = ctx.stub.createCompositeKey(type, keyParts);

        const start = Date.now();
        let data = await ctx.stub.getState(key);
        const end = Date.now();

        ctx.txinfo.stat_get_cnt += 1;
        ctx.txinfo.stat_get_exec_total_ms += end - start;

        if (data.length > 0) {
            ctx.txinfo.stat_get_size_bytes += data.length;
            let entry = data.toString('utf8');
            // log(`Retrieved entry: ${key}`, ctx);
            // log(`With value: ${entry}`, ctx);
            return entry;
        }

        // log(`Couldn't find entry: ${key}`, ctx, 'warn');
        return undefined;
    }

    /**
     * Deletes the given record from the state database.
     * @param {Context} ctx The TX context.
     * @param {string} type The type of the record.
     * @param {string[]} keyParts The attribute values as key parts.
     * @async
     */
    static async deleteEntry(ctx, type, keyParts) {
        let key = ctx.stub.createCompositeKey(type, keyParts);

        const start = Date.now();
        await ctx.stub.deleteState(key);
        const end = Date.now();

        ctx.txinfo.stat_del_cnt += 1;
        ctx.txinfo.stat_del_exec_total_ms += end - start;
        // log(`Deleted entry: ${key}`, ctx);
    }

    /**
     * Retrieves entries from the state database that matches certain criteria.
     * @param {Context} ctx The TX context.
     * @param {string} type The type of entries to search.
     * @param {string[]} keyParts The parts of the entries private key.
     * @param {function} matchFunction The function that determines matches.
     * @param {boolean} firstMatch Indicates whether only the first match should be returned.
     * @return {Promise<object|object[]>} The retrieved entries.
     * @async
     */
    static async select(ctx, type, keyParts, matchFunction, firstMatch) {
        let iterator = await ctx.stub.getStateByPartialCompositeKey(type, keyParts);
        // log(`Created partial iterator for: ${type}.${keyParts.join('.')}`, ctx);
        let matches = [];
        let retrieved = 0;

        try {
            while (true) {
                const start = Date.now();
                const res = await iterator.next();
                const end = Date.now();

                if (res.done) {
                    break;
                }

                retrieved += 1;
                let buffer = res.value.value;
                ctx.txinfo.stat_iterate_cnt += 1;
                ctx.txinfo.stat_iterate_size_bytes += buffer ? buffer.length : 0;
                ctx.txinfo.stat_iterate_exec_total_ms += end - start;

                let entry = buffer.toString('utf8');
                // log(`Enumerated entry: ${res.value.getKey()} => ${entry}`, ctx);

                let match = matchFunction(entry);
                if (match) {
                    matches.push(match);
                    if (firstMatch) {
                        break;
                    }
                }
            }
        } catch (err) {
            log(err.stack.toString() || err.toString(), ctx, 'error');
            throw err;
        } finally {
            await iterator.close();
        }

        // log(`Matched ${matches.length} entries out of ${retrieved}`, ctx);
        return firstMatch ? matches[0] : matches;
    }

    /**
     * WAREHOUSE API
     */

    /**
     * Adds a new warehouse to the state database.
     * @param {Context} ctx The TX context.
     * @param {string|Warehouse} entry The JSON string.
     * @async
     */
    static async createWarehouse(ctx, entry) {
        let warehouse = typeof entry === 'string' ? ParseUtils.parseWarehouse(entry) : entry;
        await LedgerUtils.createEntry(ctx, TABLES.WAREHOUSE, [pad(warehouse.w_id)], entry);
    }

    /**
     * Retrieves a warehouse record from the ledger.
     * @param {Context} ctx The TX context.
     * @param {number} w_id The warehouse ID.
     * @return {Promise<Warehouse>} The retrieved warehouse.
     */
    static async getWarehouse(ctx, w_id) {
        let entry = await LedgerUtils.getEntry(ctx, TABLES.WAREHOUSE, [pad(w_id)]);
        if (!entry) {
            throw new Error(`Could not retrieve Warehouse(${w_id})`);
        }

        return entry ? ParseUtils.parseWarehouse(entry) : undefined;
    }

    /**
     * Updates a warehouse in the state database.
     * @param {Context} ctx The TX context.
     * @param {Warehouse} entry The warehouse object.
     * @async
     */
    static async updateWarehouse(ctx, entry) {
        await LedgerUtils.updateEntry(ctx, TABLES.WAREHOUSE, [pad(entry.w_id)], entry);
    }

    /**
     * DISTRICT API
     */

    /**
     * Adds a new district to the state database.
     * @param {Context} ctx The TX context.
     * @param {string|District} entry The JSON string.
     * @async
     */
    static async createDistrict(ctx, entry) {
        let district = typeof entry === 'string' ? ParseUtils.parseDistrict(entry) : entry;
        await LedgerUtils.createEntry(ctx, TABLES.DISTRICT, [pad(district.d_w_id), pad(district.d_id)], entry);
    }

    /**
     * Retrieves a district record from the ledger.
     * @param {Context} ctx The TX context.
     * @param {number} d_w_id The warehouse ID.
     * @param {number} d_id The district ID.
     * @return {Promise<District>} The retrieved district.
     */
    static async getDistrict(ctx, d_w_id, d_id) {
        let entry = await LedgerUtils.getEntry(ctx, TABLES.DISTRICT, [pad(d_w_id), pad(d_id)]);
        if (!entry) {
            throw new Error(`Could not retrieve District(${d_w_id}, ${d_id})`);
        }

        return entry ? ParseUtils.parseDistrict(entry) : undefined;
    }

    /**
     * Updates a district in the state database.
     * @param {Context} ctx The TX context.
     * @param {District} entry The district object.
     * @async
     */
    static async updateDistrict(ctx, entry) {
        await LedgerUtils.updateEntry(ctx, TABLES.DISTRICT, [pad(entry.d_w_id), pad(entry.d_id)], entry);
    }

    /**
     * CUSTOMER API
     */

    /**
     * Adds a new customer to the state database.
     * @param {Context} ctx The TX context.
     * @param {string|Customer} entry The JSON string.
     * @async
     */
    static async createCustomer(ctx, entry) {
        let customer = typeof entry === 'string' ? ParseUtils.parseCustomer(entry) : entry;
        await LedgerUtils.createEntry(ctx, TABLES.CUSTOMER, [pad(customer.c_w_id), pad(customer.c_d_id), pad(customer.c_id)], entry);
        await LedgerUtils.createEntry(ctx, TABLES.CUSTOMER_LAST_NAME,
            [pad(customer.c_w_id), pad(customer.c_d_id), customer.c_last, pad(customer.c_id)], entry);
    }

    /**
     * Retrieves a customer record from the ledger.
     * @param {Context} ctx The TX context.
     * @param {number} c_w_id The warehouse ID.
     * @param {number} c_d_id The district ID.
     * @param {number} c_id The customer ID.
     * @return {Promise<Customer>} The retrieved customer.
     */
    static async getCustomer(ctx, c_w_id, c_d_id, c_id) {
        let entry = await LedgerUtils.getEntry(ctx, TABLES.CUSTOMER, [pad(c_w_id), pad(c_d_id), pad(c_id)]);
        if (!entry) {
            throw new Error(`Could not retrieve Customer(${c_w_id} ${c_d_id}, ${c_id})`);
        }

        return entry ? ParseUtils.parseCustomer(entry) : undefined;
    }

    /**
     * Retrieves the customers from the state database that match the given partial key
     * @param {Context} ctx The TX context.
     * @param {number} c_w_id The warehouse ID of the customer.
     * @param {number} c_d_id The district ID of the customer.
     * @param {string} c_last The last name of the customer.
     * @return {Promise<Customer[]>} The retrieved customers.
     * @async
     */
    static async getCustomersByLastName(ctx, c_w_id, c_d_id, c_last) {
        const matchFunction = entry => {
            let customer = ParseUtils.parseCustomer(entry);
            return customer.c_last === c_last ? customer : undefined;
        };

        // log(`Searching for Customers(${c_w_id}, ${c_d_id}, c_id) with last name "${c_last}"`, ctx);
        let entries = await LedgerUtils.select(ctx, TABLES.CUSTOMER_LAST_NAME, [pad(c_w_id), pad(c_d_id), c_last], matchFunction, false);
        if (entries.length === 0) {
            throw new Error(`Could not find Customers(${c_w_id}, ${c_d_id}, c_id) matching last name "${c_last}"`);
        }

        // log(`Found ${entries.length} Customers(${c_w_id}, ${c_d_id}, c_id) with last name "${c_last}"`, ctx);
        return entries;
    }

    /**
     * Retrieves the customers from the state database that match the given ID or last name
     * @param {Context} ctx The TX context.
     * @param {number} c_w_id The warehouse ID of the customer.
     * @param {number} c_d_id The district ID of the customer.
     * @param {number} c_id The customer ID.
     * @param {string} c_last The last name of the customer.
     * @return {Promise<Customer>} The retrieved customers.
     * @async
     */
    static async getCustomersByIdOrLastName(ctx, c_w_id, c_d_id, c_id, c_last) {
        if (c_id !== undefined) {
            // Case 1, the customer is selected based on customer number: the row in the CUSTOMER
            // table with matching C_W_ID, C_D_ID, and C_ID is selected and C_BALANCE, C_FIRST,
            // C_MIDDLE, and C_LAST are retrieved.
            return await LedgerUtils.getCustomer(ctx, c_w_id, c_d_id, c_id);
        } else if (c_last !== undefined) {
            // Case 2, the customer is selected based on customer last name: all rows in the
            // CUSTOMER table with matching C_W_ID, C_D_ID and C_LAST are selected sorted by
            // C_FIRST in ascending order. Let n be the number of rows selected. C_BALANCE,
            // C_FIRST, C_MIDDLE, and C_LAST are retrieved from the row at position n/ 2 rounded up
            // in the sorted set of selected rows from the CUSTOMER table.
            let customerList = await LedgerUtils.getCustomersByLastName(ctx, c_w_id, c_d_id, c_last);
            customerList.sort((c1, c2) => c1.c_first.localeCompare(c2.c_first));
            const position = Math.ceil(customerList.length / 2);
            return customerList[position - 1];
        } else {
            throw new Error(`Neither the customer ID nor the customer last name parameter is supplied`);
        }
    }

    /**
     * Updates a customer in the state database.
     * @param {Context} ctx The TX context.
     * @param {Customer} entry The customer object.
     * @async
     */
    static async updateCustomer(ctx, entry) {
        await LedgerUtils.updateEntry(ctx, TABLES.CUSTOMER, [pad(entry.c_w_id), pad(entry.c_d_id), pad(entry.c_id)], entry);
        await LedgerUtils.updateEntry(ctx, TABLES.CUSTOMER_LAST_NAME,
            [pad(entry.c_w_id), pad(entry.c_d_id), entry.c_last, pad(entry.c_id)], entry);
    }

    /**
     * HISTORY API
     */

    /**
     * Adds a new history to the state database.
     * @param {Context} ctx The TX context.
     * @param {string|History} entry The JSON string.
     * @async
     */
    static async createHistory(ctx, entry) {
        let history = typeof entry === 'string' ? ParseUtils.parseHistory(entry) : entry;
        await LedgerUtils.createEntry(ctx, TABLES.HISTORY, [pad(history.h_c_w_id), pad(history.h_c_d_id), pad(history.h_c_id), history.h_date], entry);
    }

    /**
     * NEW ORDER API
     */

    /**
     * Adds a new new order to the state database.
     * @param {Context} ctx The TX context.
     * @param {string|NewOrder} entry The JSON string.
     * @async
     */
    static async createNewOrder(ctx, entry) {
        let newOrder = typeof entry === 'string' ? ParseUtils.parseNewOrder(entry) : entry;
        await LedgerUtils.createEntry(ctx, TABLES.NEW_ORDER, [pad(newOrder.no_w_id), pad(newOrder.no_d_id), pad(newOrder.no_o_id)], entry);
    }

    /**
     * Retrieves the oldest new order from the state database that matches the given partial key.
     * @param {Context} ctx The TX context.
     * @param {number} no_w_id The new order's warehouse ID.
     * @param {number} no_d_id The new order's district ID.
     * @return {NewOrder} The oldest new order.
     * @async
     */
    static async getOldestNewOrder(ctx, no_w_id, no_d_id) {
        // log(`Searching for oldest New Order(${no_w_id}, ${no_d_id}, no_o_id)`, ctx);
        let oldest = await LedgerUtils.select(ctx, TABLES.NEW_ORDER, [pad(no_w_id), pad(no_d_id)], ParseUtils.parseNewOrder, true);

        // if (oldest) {
        //     log(`Retrieved oldest oldest New Order(${no_w_id}, ${no_d_id}, ${oldest.no_o_id})`, ctx);
        // }

        return oldest;
    }

    /**
     * Deletes a new order from the state database.
     * @param {Context} ctx The TX context.
     * @param {NewOrder} entry The new order object.
     * @async
     */
    static async deleteNewOrder(ctx, entry) {
        await LedgerUtils.deleteEntry(ctx, TABLES.NEW_ORDER, [pad(entry.no_w_id), pad(entry.no_d_id), pad(entry.no_o_id)]);
    }

    /**
     * ORDER API
     */

    /**
     * Adds a new order to the state database.
     * @param {Context} ctx The TX context.
     * @param {string|Order} entry The JSON string.
     * @async
     */
    static async createOrder(ctx, entry) {
        let order = typeof entry === 'string' ? ParseUtils.parseOrder(entry) : entry;
        await LedgerUtils.createEntry(ctx, TABLES.ORDERS, [pad(order.o_w_id), pad(order.o_d_id), pad(Number.MAX_SAFE_INTEGER - order.o_id)], entry);
    }

    /**
     * Retrieves an order record from the ledger.
     * @param {Context} ctx The TX context.
     * @param {number} o_w_id The order's warehouse ID.
     * @param {number} o_d_id The order's district ID.
     * @param {number} o_id The order ID.
     * @return {Promise<Order>} The retrieved order.
     */
    static async getOrder(ctx, o_w_id, o_d_id, o_id) {
        let entry = await LedgerUtils.getEntry(ctx, TABLES.ORDERS, [pad(o_w_id), pad(o_d_id), pad(Number.MAX_SAFE_INTEGER - o_id)]);
        if (!entry) {
            throw new Error(`Could not retrieve Order(${o_w_id}, ${o_d_id}, ${o_id})`);
        }

        return entry ? ParseUtils.parseOrder(entry) : undefined;
    }

    /**
     * Retrieves the last of a customer from the state database.
     * @param {Context} ctx The TX context.
     * @param {number} o_w_id The warehouse ID of the order.
     * @param {number} o_d_id The district ID of the order.
     * @param {number} o_c_id The customer ID for the order.
     * @return {Promise<Order>} The retrieved order.
     * @async
     */
    static async getLastOrderOfCustomer(ctx, o_w_id, o_d_id, o_c_id) {
        const matchFunction = entry => {
            let order = ParseUtils.parseOrder(entry);
            if (order.o_c_id === o_c_id) {
                return order;
            }

            return undefined;
        };

        // log(`Searching for last Order(${o_w_id}, ${o_d_id}, o_id) of Customer(${o_w_id}, ${o_d_id}, ${o_c_id})`, ctx);
        let lastOrder = await LedgerUtils.select(ctx, TABLES.ORDERS, [pad(o_w_id), pad(o_d_id)], matchFunction, true);
        if (!lastOrder) {
            throw new Error(`Could not find last Order(${o_w_id}, ${o_d_id}, o_id) of Customer(${o_w_id}, ${o_d_id}, ${o_c_id})`);
        }

        // log(`Retrieved last Order(${o_w_id}, ${o_d_id}, ${lastOrder.o_id}) of Customer(${o_w_id}, ${o_d_id}, ${o_c_id})`, ctx);
        return lastOrder;
    }

    /**
     * Updates an order in the state database.
     * @param {Context} ctx The TX context.
     * @param {Order} entry The order object.
     * @async
     */
    static async updateOrder(ctx, entry) {
        await LedgerUtils.updateEntry(ctx, TABLES.ORDERS, [pad(entry.o_w_id), pad(entry.o_d_id), pad(Number.MAX_SAFE_INTEGER - entry.o_id)], entry);
    }

    /**
     * ORDER LINE API
     */

    /**
     * Adds a new order line to the state database.
     * @param {Context} ctx The TX context.
     * @param {string|OrderLine} entry The JSON string.
     * @async
     */
    static async createOrderLine(ctx, entry) {
        let orderLine = typeof entry === 'string' ? ParseUtils.parseOrderLine(entry) : entry;
        await LedgerUtils.createEntry(ctx, TABLES.ORDER_LINE, [pad(orderLine.ol_w_id), pad(orderLine.ol_d_id),
            pad(orderLine.ol_o_id), pad(orderLine.ol_number)], entry);
    }

    /**
     * Retrieves an order line record from the ledger.
     * @param {Context} ctx The TX context.
     * @param {number} ol_w_id The order's warehouse ID.
     * @param {number} ol_d_id The order's district ID.
     * @param {number} ol_o_id The order ID.
     * @param {number} ol_number The number of the order line.
     * @return {Promise<OrderLine>} The retrieved order line.
     */
    static async getOrderLine(ctx, ol_w_id, ol_d_id, ol_o_id, ol_number) {
        let entry = await LedgerUtils.getEntry(ctx, TABLES.ORDER_LINE, [pad(ol_w_id), pad(ol_d_id), pad(ol_o_id), pad(ol_number)]);
        if (!entry) {
            throw new Error(`Could not retrieve Order Line(${ol_w_id}, ${ol_d_id}, ${ol_o_id}, ${ol_number})`);
        }

        return entry ? ParseUtils.parseOrderLine(entry) : undefined;
    }

    /**
     * Updates an order line in the state database.
     * @param {Context} ctx The TX context.
     * @param {OrderLine} entry The order line object.
     * @async
     */
    static async updateOrderLine(ctx, entry) {
        await LedgerUtils.updateEntry(ctx, TABLES.ORDER_LINE, [pad(entry.ol_w_id), pad(entry.ol_d_id), pad(entry.ol_o_id), pad(entry.ol_number)], entry);
    }

    /**
     * ITEM API
     */

    /**
     * Adds a new item to the state database.
     * @param {Context} ctx The TX context.
     * @param {string|Item} entry The JSON string.
     * @async
     */
    static async createItem(ctx, entry) {
        let item = typeof entry === 'string' ? ParseUtils.parseItem(entry) : entry;
        await LedgerUtils.createEntry(ctx, TABLES.ITEM, [pad(item.i_id)], entry);
    }

    /**
     * Retrieves an item record from the ledger.
     * @param {Context} ctx The TX context.
     * @param {number} i_id The item ID.
     * @return {Promise<Item>} The retrieved item.
     */
    static async getItem(ctx, i_id) {
        let entry = await LedgerUtils.getEntry(ctx, TABLES.ITEM, [pad(i_id)]);
        return entry ? ParseUtils.parseItem(entry) : undefined;
    }

    /**
     * Counts the number of items whose stock is below a given threshold.
     * @param {Context} ctx The TX context.
     * @param {number} w_id The warehouse ID.
     * @param {number} d_id The district ID.
     * @param {number} o_id_min The oldest/minimum order ID to consider (inclusive).
     * @param {number} o_id_max The newest/maximum order ID to consider (exclusive).
     * @return {number[]} The unique IDs of items from the recent orders.
     * @async
     */
    static async getItemIdsOfRecentOrders(ctx, w_id, d_id, o_id_min, o_id_max) {
        let itemIds = new Set();

        // log(`Retrieving item IDs for Orders(${w_id}, ${d_id}, [${o_id_min}, ${o_id_max}))`, ctx);
        for (let current_o_id = o_id_min; current_o_id < o_id_max; current_o_id++) {
            let order = await LedgerUtils.getOrder(ctx, w_id, d_id, current_o_id);

            for (let ol_number = 1; ol_number <= order.o_ol_cnt; ol_number++) {
                let orderLine = await LedgerUtils.getOrderLine(ctx, w_id, d_id, current_o_id, ol_number);
                itemIds.add(orderLine.ol_i_id);
            }
        }

        if (itemIds.size === 0) {
            throw new Error(`Could not find item IDs of recent Orders(${w_id}, ${d_id}, [${o_id_min}, ${o_id_max}))`);
        }

        // log(`Retrieved ${itemIds.size} item IDs for Orders(${w_id}, ${d_id}, [${o_id_min}, ${o_id_max}))`, ctx);
        return Array.from(itemIds);
    }

    /**
     * STOCK API
     */

    /**
     * Adds a new stock to the state database.
     * @param {Context} ctx The TX context.
     * @param {string|Stock} entry The JSON string.
     * @async
     */
    static async createStock(ctx, entry) {
        let stock = typeof entry === 'string' ? ParseUtils.parseStock(entry) : entry;
        await LedgerUtils.createEntry(ctx, TABLES.STOCK, [pad(stock.s_w_id), pad(stock.s_i_id)], entry);
    }

    /**
     * Retrieves a stock record from the ledger.
     * @param {Context} ctx The TX context.
     * @param {number} s_w_id The warehouse ID.
     * @param {number} s_i_id The item ID.
     * @return {Promise<Stock>} The retrieved stock.
     */
    static async getStock(ctx, s_w_id, s_i_id) {
        let entry = await LedgerUtils.getEntry(ctx, TABLES.STOCK, [pad(s_w_id), pad(s_i_id)]);
        if (!entry) {
            throw new Error(`Could not retrieve Stock(${s_w_id}, ${s_i_id})`);
        }

        return entry ? ParseUtils.parseStock(entry) : undefined;
    }

    /**
     * Updates a stock in the state database.
     * @param {Context} ctx The TX context.
     * @param {Stock} entry The stock object.
     * @async
     */
    static async updateStock(ctx, entry) {
        await LedgerUtils.updateEntry(ctx, TABLES.STOCK, [pad(entry.s_w_id), pad(entry.s_i_id)], entry);
    }
}

module.exports = LedgerUtils;