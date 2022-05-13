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

/**
 * Utility functions for parsing JSON strings to "typed" objects.
 */
class ParseUtils {
    /**
     * Parses the parameters of a New Order TX. (To enable IDE code completion)
     * @param {string} params The JSON string of the parameters.
     * @return {NewOrderParameters} The parsed parameters.
     */
    static parseNewOrderParameters(params) {
        return JSON.parse(params);
    }

    /**
     * Parses the parameters of a Payment TX. (To enable IDE code completion)
     * @param {string} params The JSON string of the parameters.
     * @return {PaymentParameters} The parsed parameters.
     */
    static parsePaymentParameters(params) {
        return JSON.parse(params);
    }

    /**
     * Parses the parameters of a Delivery TX. (To enable IDE code completion)
     * @param {string} params The JSON string of the parameters.
     * @return {DeliveryParameters} The parsed parameters.
     */
    static parseDeliveryParameters(params) {
        return JSON.parse(params);
    }

    /**
     * Parses the parameters of an Order Status TX. (To enable IDE code completion)
     * @param {string} params The JSON string of the parameters.
     * @return {OrderStatusParameters} The parsed parameters.
     */
    static parseOrderStatusParameters(params) {
        return JSON.parse(params);
    }

    /**
     * Parses the parameters of an Order Status TX. (To enable IDE code completion)
     * @param {string} params The JSON string of the parameters.
     * @return {StockLevelParameters} The parsed parameters.
     */
    static parseStockLevelParameters(params) {
        return JSON.parse(params);
    }

    /**
     * Parses the Warehouse entry JSON string. (To enable IDE code completion)
     * @param {string} jsonString The JSON string of the warehouse.
     * @return {Warehouse} The warehouse object.
     */
    static parseWarehouse(jsonString) {
        return JSON.parse(jsonString);
    }

    /**
     * Parses the District entry JSON string. (To enable IDE code completion)
     * @param {string} jsonString The JSON string of the district.
     * @return {District} The district object.
     */
    static parseDistrict(jsonString) {
        return JSON.parse(jsonString);
    }

    /**
     * Parses the Customer entry JSON string. (To enable IDE code completion)
     * @param {string} jsonString The JSON string of the customer.
     * @return {Customer} The customer object.
     */
    static parseCustomer(jsonString) {
        return JSON.parse(jsonString);
    }

    /**
     * Parses the History entry JSON string. (To enable IDE code completion)
     * @param {string} jsonString The JSON string of the history.
     * @return {History} The history object.
     */
    static parseHistory(jsonString) {
        return JSON.parse(jsonString);
    }

    /**
     * Parses the New Order entry JSON string. (To enable IDE code completion)
     * @param {string} jsonString The JSON string of the new order.
     * @return {NewOrder} The new order object.
     */
    static parseNewOrder(jsonString) {
        return JSON.parse(jsonString);
    }

    /**
     * Parses the Order entry JSON string. (To enable IDE code completion)
     * @param {string} jsonString The JSON string of the order.
     * @return {Order} The order object.
     */
    static parseOrder(jsonString) {
        return JSON.parse(jsonString);
    }

    /**
     * Parses the Order Line entry JSON string. (To enable IDE code completion)
     * @param {string} jsonString The JSON string of the order line.
     * @return {OrderLine} The order line object.
     */
    static parseOrderLine(jsonString) {
        return JSON.parse(jsonString);
    }

    /**
     * Parses the Item entry JSON string. (To enable IDE code completion)
     * @param {string} jsonString The JSON string of the item.
     * @return {Item} The item object.
     */
    static parseItem(jsonString) {
        return JSON.parse(jsonString);
    }

    /**
     * Parses the Stock entry JSON string. (To enable IDE code completion)
     * @param {string} jsonString The JSON string of the stock.
     * @return {Stock} The stock object.
     */
    static parseStock(jsonString) {
        return JSON.parse(jsonString);
    }
}

module.exports = ParseUtils;