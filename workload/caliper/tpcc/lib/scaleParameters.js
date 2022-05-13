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

/**
 * The class encapsulating the scale configuration of the benchmark.
 */
class ScaleParameters {
    /**
     * Initialize the scale configuration object.
     * @param {number} items The number of items.
     * @param {number} warehouses The number of warehouses.
     * @param {number} districtsPerWarehouse The number of districts per warehouse.
     * @param {number} customersPerDistrict The number of customers per district.
     * @param {number} newOrdersPerDistrict The number of new orders per district.
     */
    constructor(items, warehouses, districtsPerWarehouse, customersPerDistrict, newOrdersPerDistrict) {
        this._items = items;
        this._warehouses = warehouses;
        this._starting_warehouse = 1;
        this._districtsPerWarehouse = districtsPerWarehouse;
        this._customersPerDistrict = customersPerDistrict;
        this._newOrdersPerDistrict = newOrdersPerDistrict;
        this._ending_warehouse = this._warehouses + this._starting_warehouse - 1;
    }

    /**
     * Create a scale configuration for the given warehouses and scale factor.
     * @param {number} warehouses The number of warehouses to use.
     * @param {number} scaleFactor The scaling factor to apply.
     * @return {ScaleParameters} The scale configuration.
     */
    static makeWithScaleFactor(warehouses, scaleFactor = 1) {

        return new ScaleParameters(
            Math.max(Math.floor(C.NUM_ITEMS / scaleFactor), 1),
            warehouses,
            Math.max(Math.floor(C.DISTRICTS_PER_WAREHOUSE / scaleFactor), 1),
            Math.max(Math.floor(C.CUSTOMERS_PER_DISTRICT / scaleFactor), 1),
            Math.max(Math.floor(C.INITIAL_NEW_ORDERS_PER_DISTRICT / scaleFactor), 0)
        );
    }

    /**
     *
     * @return {number}
     */
    get endingWarehouse() {
        return this._ending_warehouse;
    }

    /**
     *
     * @return {number}
     */
    get newOrdersPerDistrict() {
        return this._newOrdersPerDistrict;
    }

    /**
     *
     * @return {number}
     */
    get customersPerDistrict() {
        return this._customersPerDistrict;
    }

    /**
     *
     * @return {number}
     */
    get districtsPerWarehouse() {
        return this._districtsPerWarehouse;
    }

    /**
     *
     * @return {number}
     */
    get startingWarehouse() {
        return this._starting_warehouse;
    }

    /**
     *
     * @return {number}
     */
    get warehouses() {
        return this._warehouses;
    }

    /**
     *
     * @return {number}
     */
    get items() {
        return this._items;
    }
}

module.exports = ScaleParameters;