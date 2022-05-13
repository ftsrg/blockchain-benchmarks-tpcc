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

const logger = require('fabric-shim').newLogger('tpcc');

/**
 * Enumerates the tables of the TPC-C benchmark.
 * @type {{WAREHOUSE: string, DISTRICT: string, CUSTOMER: string, HISTORY: string, NEW_ORDER: string, ORDER: string, ORDER_LINE: string, ITEM: string, STOCK: string}}
 */
const TABLES = {
    WAREHOUSE: 'WAREHOUSE',
    DISTRICT: 'DISTRICT',
    CUSTOMER: 'CUSTOMER',
    CUSTOMER_LAST_NAME: 'CUSTOMER_LAST_NAME',
    HISTORY: 'HISTORY',
    NEW_ORDER: 'NEW_ORDER',
    ORDERS: 'ORDERS',
    ORDER_LINE: 'ORDER_LINE',
    ITEM: 'ITEM',
    STOCK: 'STOCK'
};

/**
 * Logs the given debug message by appending the TX ID stub before it.
 * @param {string} msg The message to log.
 * @param {Context} ctx The TX context.
 * @param {string} level The log level.
 */
function log(msg, ctx = undefined, level = 'debug') {
    // let txId = ctx ? `<${ctx.stub.getTxID().substring(0, 8)}> ` : '';
    // logger[level](`${txId}${msg}`);
}

const padLength = Number.MAX_SAFE_INTEGER.toString().length;

/**
 * Converts the number to text and pads it to a fix length.
 * @param {number} num The number to pad.
 * @return {string} The padded number text.
 */
function pad(num) {
    return num.toString().padStart(padLength, '0');
}

module.exports.TABLES = TABLES;
module.exports.log = log;
module.exports.pad = pad;

/**
 * The definition of the New Order TX parameters.
 * @typedef {object} NewOrderParameters
 * @property {number} w_id The warehouse ID.
 * @property {number} d_id The district ID.
 * @property {number} c_id The customer ID.
 * @property {string} o_entry_d The date ISO string for the order entry.
 * @property {number[]} i_ids The array of item IDs for the order lines.
 * @property {number[]} i_w_ids The array of warehouse IDs for the order lines.
 * @property {number[]} i_qtys The array of quantities for the order lines.
 */

/**
 * The definition of the Payment TX parameters.
 * @typedef {object} PaymentParameters
 * @property {number} w_id The warehouse ID.
 * @property {number} d_id The district ID.
 * @property {number} h_amount The payment amount
 * @property {number} c_w_id The warehouse ID to which the customer belongs to.
 * @property {number} c_d_id The district ID to which the customer belongs to.
 * @property {number} c_id The customer ID.
 * @property {string} c_last The last name of the customer.
 * @property {string} h_date The payment date.
 */

/**
 * The definition of the Delivery TX parameters.
 * @typedef {object} DeliveryParameters
 * @property {number} w_id The warehouse ID.
 * @property {number} o_carrier_id The carrier ID for the order.
 * @property {string} ol_delivery_d The delivery date of the order.
 */

/**
 * The definition of the Order Status TX parameters.
 * @typedef {object} OrderStatusParameters
 * @property {number} w_id The warehouse ID.
 * @property {number} d_id The district ID.
 * @property {number} c_id The customer ID if provided.
 * @property {string} c_last The last name of the customer if provided.
 */

/**
 * The definition of the Stock Level TX parameters.
 * @typedef {object} StockLevelParameters
 * @property {number} w_id The warehouse ID.
 * @property {number} d_id The district ID.
 * @property {number} threshold The threshold of minimum quantity in stock to report.
 */

/**
 * Class for the Warehouse entries.
 * @typedef {object} Warehouse
 * @property {number} w_id The warehouse ID. Primary key.
 * @property {string} w_name The name of the warehouse.
 * @property {string} w_street_1 The first street name of the warehouse.
 * @property {string} w_street_2 The second street name of the warehouse.
 * @property {string} w_city The city of the warehouse.
 * @property {string} w_state The state of the warehouse.
 * @property {string} w_zip The ZIP code of the warehouse.
 * @property {number} w_tax The sales tax of the warehouse.
 * @property {number} w_ytd The year to date balance of the warehouse.
 */

/**
 * Class for the District entries.
 * @typedef {object} District
 * @property {number} d_id The district ID. Primary key.
 * @property {number} d_w_id The warehouse ID associated with the district. Primary key.
 * @property {string} d_name The name of the district.
 * @property {string} d_street_1 The first street name of the district.
 * @property {string} d_street_2 The second street name of the district.
 * @property {string} d_city The city of the district.
 * @property {string} d_state The state of the district.
 * @property {string} d_zip The ZIP code of the district.
 * @property {number} d_tax The sales tax of the district.
 * @property {number} d_ytd The year to date balance of the district.
 * @property {number} d_next_o_id The next available order ID.
 */

/**
 * Class for the Customer entries.
 * @typedef {object} Customer
 * @property {number} c_id The customer ID. Primary key.
 * @property {number} c_d_id The district ID associated with the customer. Primary key.
 * @property {number} c_w_id The warehouse ID associated with the customer. Primary key.
 * @property {string} c_first The first name of the customer.
 * @property {string} c_middle The middle name of the customer.
 * @property {string} c_last The last name of the customer.
 * @property {string} c_street_1 The first street name of the customer.
 * @property {string} c_street_2 The second street name of the customer.
 * @property {string} c_city The city of the customer.
 * @property {string} c_state The state of the customer.
 * @property {string} c_zip The ZIP code of the customer.
 * @property {string} c_phone The phone number of the customer
 * @property {string} c_since The date when the customer was registered.
 * @property {string} c_credit The credit classification of the customer (GC or BC).
 * @property {number} c_credit_lim The credit limit of the customer.
 * @property {number} c_discount The discount for the customer.
 * @property {number} c_balance The balance of the customer.
 * @property {number} c_ytd_payment The year to date payment of the customer.
 * @property {number} c_payment_cnt The number of times the customer paid.
 * @property {number} c_delivery_cnt The number of times a delivery was made for the customer.
 * @property {string} c_data Arbitrary information.
 */

/**
 * Class for the History entries.
 * @typedef {object} History
 * @property {number} h_c_id The customer ID. Primary key.
 * @property {number} h_c_d_id The district ID associated with the customer. Primary key.
 * @property {number} h_c_w_id The warehouse ID associated with the customer. Primary key.
 * @property {number} h_d_id The district ID.
 * @property {number} h_w_id The warehouse ID.
 * @property {string} h_date The date for the history. Primary key.
 * @property {number} h_amount The amount of payment.
 * @property {string} h_data Arbitrary information.
 */

/**
 * Class for the NewOrder entries.
 * @typedef {object} NewOrder
 * @property {number} no_o_id The order ID. Primary key.
 * @property {number} no_d_id The district ID associated with the order. Primary key.
 * @property {number} no_w_id The warehouse ID associated with the order. Primary key.
 */

/**
 * Class for the Order entries.
 * @typedef {object} Order
 * @property {number} o_id The order ID. Primary key.
 * @property {number} o_d_id The district ID associated with the order. Primary key.
 * @property {number} o_w_id The warehouse ID associated with the order. Primary key.
 * @property {number} o_c_id The customer ID associated with the order.
 * @property {string} o_entry_d The date when the order was submitted.
 * @property {number} o_carrier_id The carrier ID associated with the order.
 * @property {number} o_ol_cnt The number of lines in the order.
 * @property {number} o_all_local 1 if every order lines is local, otherwise 0.
 */

/**
 * Class for the OrderLine entries.
 * @typedef {object} OrderLine
 * @property {number} ol_o_id The order ID associated with the order line. Primary key.
 * @property {number} ol_d_id The district ID associated with the order line. Primary key.
 * @property {number} ol_w_id The warehouse ID associated with the order line. Primary key.
 * @property {number} ol_number The number/position/index of the order line. Primary key.
 * @property {number} ol_i_id The item ID associated with the order line.
 * @property {number} ol_supply_w_id The ID of the supplying warehouse.
 * @property {Date} ol_delivery_d The date of delivery.
 * @property {number} ol_quantity The quantity of items in the order line.
 * @property {number} ol_amount The amount to pay.
 * @property {string} ol_dist_info Information about the district.
 */

/**
 * Class for the Item entries.
 * @typedef {object} Item
 * @property {number} i_id The ID of the item. Primary key.
 * @property {number} i_im_id The image ID associated with the item.
 * @property {string} i_name The name of the item.
 * @property {number} i_price The price of the item.
 * @property {string} i_date Brand information.
 */

/**
 * Class for the Stock entries.
 * @typedef {object} Stock
 * @property {number} s_i_id The ID of the item associated with the stock. Primary key.
 * @property {number} s_w_id The ID of the warehouse associated with the stock. Primary key.
 * @property {number} s_quantity The quantity of the related item.
 * @property {string} s_dist_01 Information about district 1.
 * @property {string} s_dist_02 Information about district 2.
 * @property {string} s_dist_03 Information about district 3.
 * @property {string} s_dist_04 Information about district 4.
 * @property {string} s_dist_05 Information about district 5.
 * @property {string} s_dist_06 Information about district 6.
 * @property {string} s_dist_07 Information about district 7.
 * @property {string} s_dist_08 Information about district 8.
 * @property {string} s_dist_09 Information about district 9.
 * @property {string} s_dist_10 Information about district 10.
 * @property {number} s_ytd The year to date balance of the stock.
 * @property {number} s_order_cnt The number of orders for the stock.
 * @property {number} s_remote_cnt The number of remote orders for the stock.
 * @property {string} s_data Stock information.
 */
