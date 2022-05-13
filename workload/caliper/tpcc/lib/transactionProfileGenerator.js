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

class TransactionProfileGenerator {
    /**
     *
     * @param {ScaleParameters} scaleParameters
     * @param {UniformRandomGenerator} uniformRandomGenerator
     * @param {NonuniformRandomGenerator} nonuniformRandomGenerator
     * @param {number} homeWarehouseId The ID of the associated home warehouse
     * @param {number} stockLevelDistrictId The ID of the district associated with the Stock Level TX profile.
     */
    constructor(scaleParameters, uniformRandomGenerator, nonuniformRandomGenerator, homeWarehouseId, stockLevelDistrictId) {
        this._urand = uniformRandomGenerator;
        this._nurand = nonuniformRandomGenerator;
        this._scaleParameters = scaleParameters;
        this._homeWarehouseId = homeWarehouseId;
        this._stockLevelDistrictId = stockLevelDistrictId;
    }

    /**
     * @return {{profile: string, parameters: object}}
     */
    next() {
        // Selects and returns a TX content at random
        // The number of new order transactions executed per minute is the official "tpmC" metric. See TPC-C 5.4.2.

        // This is not strictly accurate: The requirement is for certain
        // *minimum* percentages to be maintained. This is close to the right
        // thing, but not precisely correct. See TPC-C 5.2.4.
        let r = this._urand.number(1, 100);

        if (r <= 4) { // 4%
            return this._generateStockLevelParams();
        } else if (r <= 4 + 4) { // 4%
            return this._generateDeliveryParams();
        } else if (r <= 4 + 4 + 4) { // 4%
            return this._generateOrderStatusParams();
        } else if (r <= 43 + 4 + 4 + 4) { // 43%
            return this._generatePaymentParams();
        } else { // 45%
            return this._generateNewOrderParams();
        }
    }

    /**
     * @return {{profile: string, parameters: object}}
     */
    _generateNewOrderParams() {
        let w_id = this._homeWarehouseId || this._makeWarehouseId();
        let ol_cnt = this._urand.number(C.MIN_OL_CNT, C.MAX_OL_CNT);
        let faulty = false;

        let i_ids = [];
        let i_w_ids = [];
        let i_qtys = [];

        let rollback = this._urand.number(1, 100) === 1;

        for (let i = 0; i < ol_cnt; i++) {
            // 1% chance of the last order line to reference a non-existing item
            if (rollback && ((i + 1) === ol_cnt)) {
                i_ids.push(this._scaleParameters.items + 1);
                faulty = true;
            } else {
                let i_id = this._makeItemId();
                while (i_ids.includes(i_id)) {
                    i_id = this._makeItemId();
                }

                i_ids.push(i_id);
            }

            // 1% of items are from a remote warehouse
            let remote = this._urand.number(1, 100) === 1;
            if ((this._scaleParameters.warehouses > 1) && remote) {
                i_w_ids.push(
                    this._urand.numberExcluding(this._scaleParameters.startingWarehouse, this._scaleParameters.endingWarehouse, w_id)
                );
            } else {
                i_w_ids.push(w_id);
            }

            i_qtys.push(this._urand.number(1, C.MAX_OL_QUANTITY));
        }

        return {
            profile: C.TransactionProfiles.NEW_ORDER,
            parameters: {
                w_id: w_id,
                d_id: this._makeDistrictId(),
                c_id: this._makeCustomerId(),
                o_entry_d: (new Date()).toISOString(),
                i_ids: i_ids,
                i_w_ids: i_w_ids,
                i_qtys: i_qtys
            },
            metadata: {
                md_tpcc_new_order_lines: ol_cnt,
                md_tpcc_new_order_item_fault: faulty
            }
        };
    }

    /**
     * @return {{profile: string, parameters: object}}
     */
    _generatePaymentParams() {
        let x = this._urand.number(1, 100);
        let y = this._urand.number(1, 100);

        let w_id = this._homeWarehouseId || this._makeWarehouseId();
        let d_id = this._makeDistrictId();

        // 85%: paying through own warehouse (or there is only 1 warehouse)
        // 15%: paying through another warehouse
        let ownWarehouse = (this._scaleParameters.warehouses === 1) || (x <= 85);

        // 60%: payment by last name
        // 40%: payment by id
        let byLastName = y <= 60;

        return {
            profile: C.TransactionProfiles.PAYMENT,
            parameters: {
                w_id: w_id,
                d_id: d_id,
                h_amount: this._urand.fixedPoint(2, C.MIN_PAYMENT, C.MAX_PAYMENT),
                c_w_id: ownWarehouse ? w_id : this._urand.numberExcluding(this._scaleParameters.startingWarehouse, this._scaleParameters.endingWarehouse, w_id),
                c_d_id: ownWarehouse ? d_id : this._makeDistrictId(),
                c_id: byLastName ? undefined : this._makeCustomerId(),
                c_last: byLastName ? this._nurand.makeRandomLastName(this._scaleParameters.customersPerDistrict) : undefined
            },
            metadata: {
                md_tpcc_payment_by_last_name: byLastName
            }
        };
    }

    /**
     * @return {{profile: string, parameters: object}}
     */
    _generateOrderStatusParams() {
        let params = {
            profile: C.TransactionProfiles.ORDER_STATUS,
            parameters: {
                w_id: this._makeWarehouseId(),
                d_id: this._makeDistrictId(),
                c_last: undefined,
                c_id: undefined
            },
            metadata: {
                md_tpcc_order_status_by_last_name: false
            }
        };

        // 60%: order status by last name
        if (this._urand.number(1, 100) <= 60) {
            params.parameters.c_last = this._nurand.makeRandomLastName(this._scaleParameters.customersPerDistrict);
            params.metadata.md_tpcc_order_status_by_last_name = true;
        } else {
            // 40%: order status by id
            params.parameters.c_id = this._makeCustomerId();
        }

        return params;
    }

    /**
     * @return {{profile: string, parameters: object}}
     */
    _generateDeliveryParams() {
       return {
           profile: C.TransactionProfiles.DELIVERY,
           parameters: {
               w_id: this._makeWarehouseId(),
               o_carrier_id: this._urand.number(C.MIN_CARRIER_ID, C.MAX_CARRIER_ID),
               ol_delivery_d: (new Date()).toISOString()
           },
           metadata: {}
       };
    }

    /**
     * @return {{profile: string, parameters: object}}
     */
    _generateStockLevelParams() {
        return {
            profile: C.TransactionProfiles.STOCK_LEVEL,
            parameters: {
                w_id: this._homeWarehouseId || this._makeWarehouseId(),
                d_id: this._stockLevelDistrictId || this._makeDistrictId(),
                threshold: this._urand.number(C.MIN_STOCK_LEVEL_THRESHOLD, C.MAX_STOCK_LEVEL_THRESHOLD)
            },
            metadata: {}
        };
    }

    /**
     *
     * @return {number}
     * @private
     */
    _makeWarehouseId() {
        return this._urand.number(this._scaleParameters.startingWarehouse, this._scaleParameters.endingWarehouse);
    }

    /**
     *
     * @return {number}
     * @private
     */
    _makeDistrictId() {
        return this._urand.number(1, this._scaleParameters.districtsPerWarehouse);
    }

    /**
     *
     * @return {number}
     * @private
     */
    _makeCustomerId() {
        return this._nurand.NURand(1023, 1, this._scaleParameters.customersPerDistrict);
    }

    /**
     *
     * @return {number}
     * @private
     */
    _makeItemId() {
        return this._nurand.NURand(8191, 1, this._scaleParameters.items);
    }
}

module.exports = TransactionProfileGenerator;