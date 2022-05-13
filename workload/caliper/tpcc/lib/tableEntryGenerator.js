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
'use strict';

const C = require('./constants');

const States = {
    LOAD_ITEMS: 1,
    LOAD_WAREHOUSES: 2,
    LOAD_DISTRICTS_FOR_WAREHOUSE: 3,
    LOAD_CUSTOMERS_FOR_DISTRICT: 4,
    LOAD_HISTORY_FOR_CUSTOMER: 5,
    LOAD_ORDERS_FOR_DISTRICT: 6,
    LOAD_ORDER_LINES_FOR_ORDER: 7,
    LOAD_NEW_ORDER_FOR_ORDER: 8,
    LOAD_STOCKS_FOR_WAREHOUSE: 9,
    FINISHED: 10,

    toString: state => {
        switch (state) {
            case States.LOAD_ITEMS:
                return 'LOAD_ITEMS';
            case States.LOAD_WAREHOUSES:
                return 'LOAD_WAREHOUSES';
            case States.LOAD_DISTRICTS_FOR_WAREHOUSE:
                return 'LOAD_DISTRICTS_FOR_WAREHOUSE';
            case States.LOAD_CUSTOMERS_FOR_DISTRICT:
                return 'LOAD_CUSTOMERS_FOR_DISTRICT';
            case States.LOAD_HISTORY_FOR_CUSTOMER:
                return 'LOAD_HISTORY_FOR_CUSTOMER';
            case States.LOAD_ORDERS_FOR_DISTRICT:
                return 'LOAD_ORDERS_FOR_DISTRICT';
            case States.LOAD_ORDER_LINES_FOR_ORDER:
                return 'LOAD_ORDER_LINES_FOR_ORDER';
            case States.LOAD_NEW_ORDER_FOR_ORDER:
                return 'LOAD_NEW_ORDER_FOR_ORDER';
            case States.LOAD_STOCKS_FOR_WAREHOUSE:
                return 'LOAD_STOCKS_FOR_WAREHOUSE';
            case States.FINISHED:
                return 'FINISHED';
            default:
                throw new Error(`Unknown state code: ${state}`);
        }
    }
};

class TableEntryGenerator {
    /**
     *
     * @param {ScaleParameters} scaleParameters The scaling configuration.
     * @param {number[]} warehouseIds The array of warehouse IDs to create.
     * @param {boolean} loadItems Indicates whether to fill the ITEM table.
     * @param {UniformRandomGenerator} uniformRandomGenerator
     * @param {NonuniformRandomGenerator} nonuniformRandomGenerator
     * @param {function} logger
     */
    constructor(scaleParameters, warehouseIds, loadItems, uniformRandomGenerator, nonuniformRandomGenerator, logger = undefined) {
        this._logger = logger || ((msg) => {});
        this._urand = uniformRandomGenerator;
        this._nurand = nonuniformRandomGenerator;
        this._scaleParameters = scaleParameters;
        this._warehouseIds = warehouseIds;
        this._totalEntriesGenerated = 0;

        this._logger(`Items to generate: ${loadItems ? this._scaleParameters.items : 0}`);
        this._logger(`Total items: ${this._scaleParameters.items}`);
        this._logger(`Warehouses to generate: ${warehouseIds.length}`);
        this._logger(`Total warehouses: ${this._scaleParameters.warehouses}`);
        this._logger(`Districts per warehouses: ${this._scaleParameters.districtsPerWarehouse}`);
        this._logger(`Customers/histories/orders per districts: ${this._scaleParameters.customersPerDistrict}`);
        this._logger(`New orders per districts: ${this._scaleParameters.newOrdersPerDistrict}`);
        this._logger(`Stocks per warehouses: ${this._scaleParameters.items}`);

        // initial state depends on whether we need to load items or not
        if (loadItems) {
            this._state = States.LOAD_ITEMS;
            // select 10% of the rows to be marked "original"
            this._itemOriginalRows = this._urand.selectUniqueIds(Math.floor(this._scaleParameters.items / 10),
                1, this._scaleParameters.items);
        } else {
            this._state = this._warehouseIds.length > 0 ? States.LOAD_WAREHOUSES : States.FINISHED;
        }

        this._logger(`Initial state: ${States.toString(this._state)}`);

        /////////////////////
        // State variables //
        /////////////////////

        // item-related
        this._itemsGenerated = 0;

        // warehouse-related
        this._warehousesGenerated = 0;

        // district-related
        this._districtsGenerated = 0;

        // customer-related
        this._customerBadCreditRows = new Set();
        this._customersGenerated = 0;
        this._customerIdPermutation = [];

        // order-related
        this._ordersGenerated = 0;
        this._orderLineCount = 0;
        this._orderIsNew = false;

        // order line-related
        this._orderLinesGenerated = 0;

        // stock-related
        this._stocksGenerated = 0;
        this._stockOriginalRows = new Set();
    }

    /**
     *
     * @return {boolean}
     */
    hasNext() {
        return this._state !== States.FINISHED;
    }

    /**
     * @return {{table: string, data: object}}
     */
    next() {
        let oldState = this._state;
        switch (this._state) {
            case States.LOAD_ITEMS:
                let nextItem = this._generateNextItem();

                // do we have more items to generate?
                if (this._itemsGenerated < this._scaleParameters.items) {
                    // if yes, move to the next item (no state change)
                    this._state = States.LOAD_ITEMS;
                    this._logger(`${this._scaleParameters.items - this._itemsGenerated} more items to generate`);
                } else {
                    // otherwise, move to generating the warehouses, or finish if warehouses are not generated
                    this._state = this._warehouseIds.length > 0 ? States.LOAD_WAREHOUSES : States.FINISHED;
                    this._logger('No more items to generate');
                }

                this._logStateTransition(oldState);
                return nextItem;
            case States.LOAD_WAREHOUSES:
                let nextWarehouse = this._generateNextWarehouse();

                // move to generating the related districts
                this._state = States.LOAD_DISTRICTS_FOR_WAREHOUSE;

                // reset district counter for latest warehouse
                this._districtsGenerated = 0;

                this._logStateTransition(oldState);
                return nextWarehouse;
            case States.LOAD_DISTRICTS_FOR_WAREHOUSE:
                let nextDistrict = this._generateNextDistrict();

                // move to generating the related customers
                this._state = States.LOAD_CUSTOMERS_FOR_DISTRICT;

                // reset customer states for latest district
                this._customersGenerated = 0;
                this._customerIdPermutation = [];
                // Select 10% of the customers to have bad credit for the customer generation
                this._customerBadCreditRows = this._urand.selectUniqueIds(Math.floor(this._scaleParameters.customersPerDistrict / 10),
                    1, this._scaleParameters.customersPerDistrict);

                this._logStateTransition(oldState);
                return nextDistrict;
            case States.LOAD_CUSTOMERS_FOR_DISTRICT:
                let nextCustomer = this._generateNextCustomer();

                // generate the corresponding history next
                this._state = States.LOAD_HISTORY_FOR_CUSTOMER;

                this._logStateTransition(oldState);
                return nextCustomer;
            case States.LOAD_HISTORY_FOR_CUSTOMER:
                let nextHistory = this._generateNextHistory();

                // do we have additional customers to generate?
                if (this._customersGenerated < this._scaleParameters.customersPerDistrict) {
                    // if yes, then move to the next customer
                    this._state = States.LOAD_CUSTOMERS_FOR_DISTRICT;
                    this._logger(`${this._scaleParameters.customersPerDistrict - this._customersGenerated} more customers/history to generate for district ${this._districtsGenerated} of warehouse ${this._warehousesGenerated}`);
                } else {
                    // if no, then proceed to the district orders
                    this._state = States.LOAD_ORDERS_FOR_DISTRICT;
                    this._logger(`No more customers/history to generate for district ${this._districtsGenerated} of warehouse ${this._warehousesGenerated}`);

                    // TPC-C 4.3.3.1. says that o_c_id should be a permutation of [1, 3000]. But since it
                    // is a c_id field, it seems to make sense to have it be a permutation of the
                    // customers. For the "real" thing this will be equivalent
                    this._shuffle(this._customerIdPermutation);

                    // reset the order-related variables
                    this._ordersGenerated = 0;
                    this._orderLineCount = 0;
                    this._orderIsNew = false;
                }

                this._logStateTransition(oldState);
                return nextHistory;
            case States.LOAD_ORDERS_FOR_DISTRICT:
                let nextOrder = this._generateNextOrder();

                // proceed to the order lines of the order
                this._state = States.LOAD_ORDER_LINES_FOR_ORDER;

                // reset the order line-related variables
                this._orderLinesGenerated = 0;

                this._logStateTransition(oldState);
                return nextOrder;
            case States.LOAD_ORDER_LINES_FOR_ORDER:
                let nextOrderLine = this._generateNextOrderLine();

                // do we have additional order lines to generate?
                if (this._orderLinesGenerated < this._orderLineCount) {
                    // if yes, move to the next order line (no state change)
                    this._state = States.LOAD_ORDER_LINES_FOR_ORDER;
                    this._logger(`${this._orderLineCount - this._orderLinesGenerated} more order lines to generate for order ${this._ordersGenerated} in district ${this._districtsGenerated} of warehouse ${this._warehousesGenerated}`);
                } else {
                    this._logger(`No more order lines to generate for order ${this._ordersGenerated} in district ${this._districtsGenerated} of warehouse ${this._warehousesGenerated}`);
                    // if no, was this a new order?
                    if (this._orderIsNew) {
                        // if yes, move to the new order entry
                        this._state = States.LOAD_NEW_ORDER_FOR_ORDER;
                    } else {
                        // otherwise, do we have additional orders to generate?
                        if (this._ordersGenerated < this._scaleParameters.customersPerDistrict) {
                            // if yes, move to the next order
                            this._state = States.LOAD_ORDERS_FOR_DISTRICT;
                            this._logger(`${this._scaleParameters.customersPerDistrict - this._ordersGenerated} more orders to generate for district ${this._districtsGenerated} of warehouse ${this._warehousesGenerated}`);
                        } else {
                            this._logger(`No more orders to generate in district ${this._districtsGenerated} of warehouse ${this._warehousesGenerated}`);
                            // if no, do we have more districts to generate
                            if (this._districtsGenerated < this._scaleParameters.districtsPerWarehouse) {
                                // if yes, move to the next district
                                this._state = States.LOAD_DISTRICTS_FOR_WAREHOUSE;
                                this._logger(`${this._scaleParameters.districtsPerWarehouse - this._districtsGenerated} more districts to generate for warehouse ${this._warehousesGenerated}`);
                            } else {
                                this._logger(`No more districts to generate for warehouse ${this._warehousesGenerated}`);
                                // if no, move to generating the stocks
                                this._state = States.LOAD_STOCKS_FOR_WAREHOUSE;

                                // reset stock states for latest warehouse
                                this._stocksGenerated = 0;
                                // Select 10% of the stock to be marked "original"
                                this._stockOriginalRows = this._urand.selectUniqueIds(Math.floor(this._scaleParameters.items / 10),
                                    1, this._scaleParameters.items);
                            }
                        }
                    }
                }

                this._logStateTransition(oldState);
                return nextOrderLine;
            case States.LOAD_NEW_ORDER_FOR_ORDER:
                let nextNewOrder = this._generateNextNewOrder();

                // otherwise, do we have additional orders to generate?
                if (this._ordersGenerated < this._scaleParameters.customersPerDistrict) {
                    // if yes, move to the next order
                    this._state = States.LOAD_ORDERS_FOR_DISTRICT;
                    this._logger(`${this._scaleParameters.customersPerDistrict - this._ordersGenerated} more orders to generate for district ${this._districtsGenerated} of warehouse ${this._warehousesGenerated}`);
                } else {
                    this._logger(`No more orders to generate in district ${this._districtsGenerated} of warehouse ${this._warehousesGenerated}`);
                    // if no, do we have more districts to generate
                    if (this._districtsGenerated < this._scaleParameters.districtsPerWarehouse) {
                        // if yes, move to the next district
                        this._state = States.LOAD_DISTRICTS_FOR_WAREHOUSE;
                        this._logger(`${this._scaleParameters.districtsPerWarehouse - this._districtsGenerated} more districts to generate for warehouse ${this._warehousesGenerated}`);
                    } else {
                        this._logger(`No more districts to generate for warehouse ${this._warehousesGenerated}`);
                        // if no, move to generating the stocks
                        this._state = States.LOAD_STOCKS_FOR_WAREHOUSE;

                        // reset stock states for latest warehouse
                        this._stocksGenerated = 0;
                        // Select 10% of the stock to be marked "original"
                        this._stockOriginalRows = this._urand.selectUniqueIds(Math.floor(this._scaleParameters.items / 10),
                            1, this._scaleParameters.items);
                    }
                }

                this._logStateTransition(oldState);
                return nextNewOrder;
            case States.LOAD_STOCKS_FOR_WAREHOUSE:
                let nextStock = this._generateNextStock();

                // do we have more stocks to generate?
                if (this._stocksGenerated < this._scaleParameters.items) {
                    // if yes, move to the next stock (no state change)
                    this._state = States.LOAD_STOCKS_FOR_WAREHOUSE;
                    this._logger(`${this._scaleParameters.items - this._stocksGenerated} more stocks to generate for warehouse ${this._warehousesGenerated}`);
                } else {
                    this._logger(`No more stocks to generate for warehouse ${this._warehousesGenerated}`);
                    // if no, do we have more warehouses to generate?
                    if (this._warehousesGenerated < this._warehouseIds.length) {
                        // if yes, move to the next warehouse
                        this._state = States.LOAD_WAREHOUSES;
                        this._logger(`${this._warehouseIds.length - this._warehousesGenerated} more warehouses to generate`);
                        // no need for resets, warehouse data is top-level
                    } else {
                        // if no, we are done with the entries :)
                        this._logger('No more warehouses to generate. Finished entry generation');
                        this._state = States.FINISHED;
                    }
                }

                this._logStateTransition(oldState);
                return nextStock;
            case States.FINISHED:
                throw new Error('No more TPC-C entries to generate');
            default:
                throw new Error(`Unknown state code: ${this._state}`);
        }
    }

    _logStateTransition(oldState) {
        this._logger(`State transition after entry #${this._totalEntriesGenerated}: ${States.toString(oldState)} => ${States.toString(this._state)}`);
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateNextStock() {
        let latestWarehouseIndex = this._warehouseIds[this._warehousesGenerated - 1];
        let currentItemIndex = this._stocksGenerated + 1;
        let isOriginal = this._stockOriginalRows.has(currentItemIndex);

        let nextStock = this._generateStock(latestWarehouseIndex, currentItemIndex, isOriginal);

        // housekeeping
        this._stocksGenerated += 1;

        this._totalEntriesGenerated += 1;
        this._logger(`Generated entry: ${JSON.stringify(nextStock)}`);
        return nextStock;
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateStock(warehouseIndex, itemIndex, isOriginal) {
        let districts = {};
        for (let i = 0; i < C.DISTRICTS_PER_WAREHOUSE; i++) {
            let districtIndex = i + 1;
            districts[`s_dist_${districtIndex.toString().padStart(2, '0')}`] = this._urand.astring(C.DIST, C.DIST);
        }

        let nextStock = {
            table: C.TABLENAME_STOCK,
            data: {
                s_i_id: itemIndex,
                s_w_id: warehouseIndex,
                s_quantity: this._urand.number(C.MIN_QUANTITY, C.MAX_QUANTITY),
                ...districts,
                s_ytd: 0,
                s_order_cnt: 0,
                s_remote_cnt: 0,
                s_data: this._urand.astring(C.MIN_I_DATA, C.MAX_I_DATA)
            }
        };

        if (isOriginal) {
            nextStock.data.s_data = this._fillOriginal(nextStock.data.s_data);
        }

        return nextStock;
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateNextNewOrder() {
        let latestWarehouseIndex = this._warehouseIds[this._warehousesGenerated - 1];
        let latestDistrictIndex = this._districtsGenerated;
        let latestOrderIndex = this._ordersGenerated;

        let nextNewOrder = this._generateNewOrder(latestWarehouseIndex, latestDistrictIndex, latestOrderIndex);

        this._totalEntriesGenerated += 1;
        this._logger(`Generated entry: ${JSON.stringify(nextNewOrder)}`);
        return nextNewOrder;
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateNewOrder(warehouseIndex, districtIndex, orderIndex) {
        return {
            table: C.TABLENAME_NEW_ORDER,
            data: {
                no_o_id: orderIndex,
                no_d_id: districtIndex,
                no_w_id: warehouseIndex
            }
        };
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateNextOrderLine() {
        let latestWarehouseIndex = this._warehouseIds[this._warehousesGenerated - 1];
        let latestDistrictIndex = this._districtsGenerated;
        let latestOrderIndex = this._ordersGenerated;
        let currentOrderLineIndex = this._orderLinesGenerated + 1;

        let nextOrderLine = this._generateOrderLine(latestWarehouseIndex, latestDistrictIndex, latestOrderIndex,
            currentOrderLineIndex, this._orderIsNew);

        // housekeeping
        this._orderLinesGenerated += 1;

        this._totalEntriesGenerated += 1;
        this._logger(`Generated entry: ${JSON.stringify(nextOrderLine)}`);
        return nextOrderLine;
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateOrderLine(warehouseIndex, districtIndex, orderIndex, orderLineIndex, isNewOrder) {
        // determine supplying warehouse index
        let ol_supply_w_id = warehouseIndex;
        // 1% of items are from a remote warehouse
        let remote = this._urand.number(1, 100) === 1;

        if (this._scaleParameters.warehouses > 1 && remote) {
            ol_supply_w_id = this._urand.numberExcluding(this._scaleParameters.startingWarehouse,
                this._scaleParameters.endingWarehouse, warehouseIndex);
        }

        return {
            table: C.TABLENAME_ORDER_LINE,
            data: {
                ol_o_id: orderIndex,
                ol_d_id: districtIndex,
                ol_w_id: warehouseIndex,
                ol_number: orderLineIndex,
                ol_i_id: this._urand.number(1, this._scaleParameters.items),
                ol_supply_w_id: ol_supply_w_id,
                ol_delivery_d: isNewOrder ? undefined : new Date(),
                ol_quantity: C.INITIAL_QUANTITY,
                ol_amount: isNewOrder ? this._urand.fixedPoint(C.MONEY_DECIMALS, C.MIN_AMOUNT, C.MAX_PRICE * C.MAX_OL_QUANTITY) : 0.00,
                ol_dist_info: this._urand.astring(C.DIST, C.DIST)
            }
        };
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateNextOrder() {
        let latestWarehouseIndex = this._warehouseIds[this._warehousesGenerated - 1];
        let latestDistrictIndex = this._districtsGenerated;
        let currentOrderIndex = this._ordersGenerated + 1;
        let customerIndex = this._customerIdPermutation[currentOrderIndex - 1];

        this._orderLineCount = this._urand.number(C.MIN_OL_CNT, C.MAX_OL_CNT);
        this._orderIsNew = (this._scaleParameters.customersPerDistrict - this._scaleParameters.newOrdersPerDistrict) < currentOrderIndex;

        let nextOrder = this._generateOrder(latestWarehouseIndex, latestDistrictIndex, currentOrderIndex,
            customerIndex, this._orderLineCount, this._orderIsNew);

        // housekeeping
        this._ordersGenerated += 1;

        this._totalEntriesGenerated += 1;
        this._logger(`Generated entry: ${JSON.stringify(nextOrder)}`);
        this._logger(`Order will have ${this._orderLineCount} lines`);
        return nextOrder;
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateOrder(warehouseIndex, districtIndex, orderIndex, customerIndex, orderLineCount, isNewOrder) {
        return {
            table: C.TABLENAME_ORDERS,
            data: {
                o_id: orderIndex,
                o_d_id: districtIndex,
                o_w_id: warehouseIndex,
                o_c_id: customerIndex,
                o_entry_d: new Date(),
                o_carrier_id: isNewOrder ? C.NULL_CARRIER_ID : this._urand.number(C.MIN_CARRIER_ID, C.MAX_CARRIER_ID),
                o_ol_cnt: orderLineCount,
                o_all_local: C.INITIAL_ALL_LOCAL
            }
        };
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateNextHistory() {
        let latestWarehouseIndex = this._warehouseIds[this._warehousesGenerated - 1];
        let latestDistrictIndex = this._districtsGenerated;
        let latestCustomerIndex = this._customersGenerated;

        let nextHistory = this._generateHistory(latestWarehouseIndex, latestDistrictIndex, latestCustomerIndex);

        this._totalEntriesGenerated += 1;
        this._logger(`Generated entry: ${JSON.stringify(nextHistory)}`);
        return nextHistory;
    }

    /**
     *
     * @param {number} warehouseIndex
     * @param {number} districtIndex
     * @param {number} customerIndex
     * @return {{table: string, data: object}}
     * @private
     */
    _generateHistory(warehouseIndex, districtIndex, customerIndex) {
        return {
            table: C.TABLENAME_HISTORY,
            data: {
                h_c_id: customerIndex,
                h_c_d_id: districtIndex,
                h_c_w_id: warehouseIndex,
                h_d_id: districtIndex,
                h_w_id: warehouseIndex,
                h_date: new Date(),
                h_amount: C.INITIAL_AMOUNT,
                h_data: this._urand.astring(C.MIN_DATA, C.MAX_DATA)
            }
        };
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateNextCustomer() {
        let latestWarehouseIndex = this._warehouseIds[this._warehousesGenerated - 1];
        let latestDistrictIndex = this._districtsGenerated;
        let currentCustomerIndex = this._customersGenerated + 1;
        let hasBadCredit = this._customerBadCreditRows.has(currentCustomerIndex);

        let nextCustomer = this._generateCustomer(latestWarehouseIndex, latestDistrictIndex, currentCustomerIndex, hasBadCredit);

        //housekeeping
        this._customerIdPermutation.push(currentCustomerIndex);
        this._customersGenerated += 1;

        this._totalEntriesGenerated += 1;
        this._logger(`Generated entry: ${JSON.stringify(nextCustomer)}`);
        return nextCustomer;
    }

    /**
     *
     * @param {number} warehouseIndex
     * @param {number} districtIndex
     * @param {number} customerIndex
     * @param {boolean} hasBadCredit
     * @return {{table: string, data: object}}
     * @private
     */
    _generateCustomer(warehouseIndex, districtIndex, customerIndex, hasBadCredit) {
        return {
            table: C.TABLENAME_CUSTOMER,
            data: {
                c_id: customerIndex,
                c_d_id: districtIndex,
                c_w_id: warehouseIndex,
                c_first: this._urand.astring(C.MIN_FIRST, C.MAX_FIRST),
                c_middle: C.MIDDLE,
                c_last: customerIndex <= 1000 ? this._nurand.makeLastName(customerIndex - 1) : this._nurand.makeRandomLastName(C.CUSTOMERS_PER_DISTRICT),
                ...this._generateStreetAddress('c'),
                c_phone: this._urand.nstring(C.PHONE, C.PHONE),
                c_since: new Date(),
                c_credit: hasBadCredit ? C.BAD_CREDIT : C.GOOD_CREDIT,
                c_credit_lim: C.INITIAL_CREDIT_LIM,
                c_discount: this._urand.fixedPoint(C.DISCOUNT_DECIMALS, C.MIN_DISCOUNT, C.MAX_DISCOUNT),
                c_balance: C.INITIAL_BALANCE,
                c_ytd_payment: C.INITIAL_YTD_PAYMENT,
                c_payment_cnt: C.INITIAL_PAYMENT_CNT,
                c_delivery_cnt: C.INITIAL_DELIVERY_CNT,
                c_data: this._urand.astring(C.MIN_C_DATA, C.MAX_C_DATA)
            }
        };
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateNextDistrict() {
        let lastWarehouseIndex = this._warehouseIds[this._warehousesGenerated - 1];
        let currentDistrictIndex = this._districtsGenerated + 1;
        let nextOrderNumber = this._scaleParameters.customersPerDistrict + 1;

        let nextDistrict = this._generateDistrict(lastWarehouseIndex, currentDistrictIndex, nextOrderNumber);

        // housekeeping
        this._districtsGenerated += 1;

        this._totalEntriesGenerated += 1;
        this._logger(`Generated entry: ${JSON.stringify(nextDistrict)}`);
        return nextDistrict;
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateDistrict(warehouseIndex, districtIndex, nextOrderNumber) {
        return {
            table: C.TABLENAME_DISTRICT,
            data: {
                d_id: districtIndex,
                d_w_id: warehouseIndex,
                ...this._generateAddress('d'),
                d_tax: this._generateTax(),
                d_ytd: C.INITIAL_D_YTD,
                d_next_o_id: nextOrderNumber
            }
        };
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateNextWarehouse() {
        let currentIndex = this._warehouseIds[this._warehousesGenerated];
        let nextWarehouse = this._generateWarehouse(currentIndex);

        // housekeeping
        this._warehousesGenerated += 1;

        this._totalEntriesGenerated += 1;
        this._logger(`Generated entry: ${JSON.stringify(nextWarehouse)}`);
        return nextWarehouse;
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateWarehouse(warehouseIndex) {
        return {
            table: C.TABLENAME_WAREHOUSE,
            data: {
                w_id: warehouseIndex,
                ...this._generateAddress('w'),
                w_tax: this._generateTax(),
                w_ytd: C.INITIAL_W_YTD
            }
        };
    }

    /**
     *
     * @param {string} prefix
     * @return {object}
     * @private
     */
    _generateAddress(prefix) {
        let address = {};

        address[`${prefix}_name`] = this._urand.astring(C.MIN_NAME, C.MAX_NAME);
        Object.assign(address, this._generateStreetAddress(prefix));

        return address;
    }

    /**
     *
     * @param {string} prefix
     * @return {object}
     * @private
     */
    _generateStreetAddress(prefix) {
        let streetAddress = {};

        streetAddress[`${prefix}_street_1`] = this._urand.astring(C.MIN_STREET, C.MAX_STREET);
        streetAddress[`${prefix}_street_2`] = this._urand.astring(C.MIN_STREET, C.MAX_STREET);
        streetAddress[`${prefix}_city`] = this._urand.astring(C.MIN_CITY, C.MAX_CITY);
        streetAddress[`${prefix}_state`] = this._urand.astring(C.STATE, C.STATE);
        streetAddress[`${prefix}_zip`] = this._generateZip();

        return streetAddress;
    }

    /**
     *
     * @return {string}
     * @private
     */
    _generateZip() {
        let length = C.ZIP_LENGTH - C.ZIP_SUFFIX.length;
        return this._urand.nstring(length, length) + C.ZIP_SUFFIX;
    }

    /**
     *
     * @return {number}
     * @private
     */
    _generateTax() {
        return this._urand.fixedPoint(C.TAX_DECIMALS, C.MIN_TAX, C.MAX_TAX);
    }

    /**
     * @return {{table: string, data: object}}
     */
    _generateNextItem() {
        let currentIndex = this._itemsGenerated + 1;
        let isOriginal = this._itemOriginalRows.has(currentIndex);

        let nextItem = this._generateItem(currentIndex, isOriginal);

        // housekeeping
        this._itemsGenerated += 1;

        this._totalEntriesGenerated += 1;
        this._logger(`Generated entry: ${JSON.stringify(nextItem)}`);
        return nextItem;
    }

    /**
     *
     * @param {number} itemIndex
     * @param {boolean} isOriginal
     * @return {{table: string, data: object}}
     * @private
     */
    _generateItem(itemIndex, isOriginal) {
        let nextItem = {
            table: C.TABLENAME_ITEM,
            data: {
                i_id: itemIndex,
                i_im_id: this._urand.number(C.MIN_IM, C.MAX_IM),
                i_name: this._urand.astring(C.MIN_I_NAME, C.MAX_I_NAME),
                i_price: this._urand.fixedPoint(C.MONEY_DECIMALS, C.MIN_PRICE, C.MAX_PRICE),
                i_data: this._urand.astring(C.MIN_I_DATA, C.MAX_I_DATA)
            }
        };

        if (isOriginal) {
            nextItem.data.i_data = this._fillOriginal(nextItem.data.i_data);
        }

        return nextItem;
    }

    /**
     *
     * @param {string} data
     * @return {string}
     * @private
     */
    _fillOriginal(data) {
        let originalLength = C.ORIGINAL_STRING.length;
        let position = this._urand.number(0, data.length - originalLength);
        return data.slice(0, position) + C.ORIGINAL_STRING + data.slice(position + originalLength);
    }

    /**
     *
     * @param {object[]} a
     * @private
     */
    _shuffle(a) {
        let j, x, i;
        for (i = a.length - 1; i > 0; i--) {
            j = Math.floor(Math.random() * (i + 1));
            x = a[i];
            a[i] = a[j];
            a[j] = x;
        }
    }
}

module.exports = TableEntryGenerator;