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

class WaitTimeSpecification {
    constructor(menuResponseTime, keyingTimeNewOrder, keyingTimePayment, keyingTimeOrderStatus, keyingTimeDelivery,
                keyingTimeStockLevel, meanThinkingTimeNewOrder, meanThinkingTimePayment, meanThinkingTimeOrderStatus,
                meanThinkingTimeDelivery, meanThinkingTimeStockLevel, responseTimeLimitNewOrder,
                responseTimeLimitPayment, responseTimeLimitOrderStatus, responseTimeLimitDelivery, responseTimeLimitStockLevel) {
        this._menuResponseTime = menuResponseTime;

        this._keyingTime = {};
        this._keyingTime[C.TransactionProfiles.NEW_ORDER] = keyingTimeNewOrder;
        this._keyingTime[C.TransactionProfiles.PAYMENT] = keyingTimePayment;
        this._keyingTime[C.TransactionProfiles.ORDER_STATUS] = keyingTimeOrderStatus;
        this._keyingTime[C.TransactionProfiles.DELIVERY] = keyingTimeDelivery;
        this._keyingTime[C.TransactionProfiles.STOCK_LEVEL] = keyingTimeStockLevel;

        this._meanThinkingTime = {};
        this._meanThinkingTime[C.TransactionProfiles.NEW_ORDER] = meanThinkingTimeNewOrder;
        this._meanThinkingTime[C.TransactionProfiles.PAYMENT] = meanThinkingTimePayment;
        this._meanThinkingTime[C.TransactionProfiles.ORDER_STATUS] = meanThinkingTimeOrderStatus;
        this._meanThinkingTime[C.TransactionProfiles.DELIVERY] = meanThinkingTimeDelivery;
        this._meanThinkingTime[C.TransactionProfiles.STOCK_LEVEL] = meanThinkingTimeStockLevel;

        this._responseTimeLimit = {};
        this._responseTimeLimit[C.TransactionProfiles.NEW_ORDER] = responseTimeLimitNewOrder;
        this._responseTimeLimit[C.TransactionProfiles.PAYMENT] = responseTimeLimitPayment;
        this._responseTimeLimit[C.TransactionProfiles.ORDER_STATUS] = responseTimeLimitOrderStatus;
        this._responseTimeLimit[C.TransactionProfiles.DELIVERY] = responseTimeLimitDelivery;
        this._responseTimeLimit[C.TransactionProfiles.STOCK_LEVEL] = responseTimeLimitStockLevel;
    }

    /**
     *
     * @return {WaitTimeSpecification}
     */
    static makeDefault() {
        let keying = C.WaitTimes.KeyingTime;
        let thinking = C.WaitTimes.MeanThinkingTime;
        let response = C.WaitTimes.ResponseTimeLimit;

        return new WaitTimeSpecification(
            C.WaitTimes.MENU_RESPONSE_TIME,
            keying.NEW_ORDER, keying.PAYMENT, keying.ORDER_STATUS, keying.DELIVERY, keying.STOCK_LEVEL,
            thinking.NEW_ORDER, thinking.PAYMENT, thinking.ORDER_STATUS, thinking.DELIVERY, thinking.STOCK_LEVEL,
            response.NEW_ORDER, response.PAYMENT, response.ORDER_STATUS, response.DELIVERY, response.STOCK_LEVEL
        );
    }

    getMenuResponseTime() {
        return this._menuResponseTime;
    }

    getKeyingTime(transactionProfile) {
        return this._keyingTime[transactionProfile];
    }

    getMeanThinkingTime(transactionProfile) {
        return this._meanThinkingTime[transactionProfile];
    }

    getResponseTimeLimit(transactionProfile) {
        return this._responseTimeLimit[transactionProfile];
    }
}

module.exports = WaitTimeSpecification;