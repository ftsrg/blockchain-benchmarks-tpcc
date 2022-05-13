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

class WaitTimeGenerator {
    /**
     *
     * @param {WaitTimeSpecification} waitTimeSpecification
     * @param {UniformRandomGenerator} uniformRandomGenerator
     */
    constructor(waitTimeSpecification, uniformRandomGenerator) {
        this._spec = waitTimeSpecification;
        this._rand = uniformRandomGenerator;
    }

    generateMenuResponseTime() {
        // 5.2.5.1 The Menu step is transaction independent. At least 90% of all Menu selections must have a Menu RT (see Clause 5.3.3) of less than 2 seconds.
        return this._rand.number(0, Math.floor(this._spec.getMenuResponseTime() / 0.9));
    }

    generateKeyingTime(transactionProfile) {
        // 5.2.5.2 For each transaction type, the Keying Time is constant and must be a minimum of 18 seconds for New-Order, 3 seconds for Payment, and 2 seconds each for Order-Status, Delivery, and Stock-Level.
        return this._spec.getKeyingTime(transactionProfile);
    }

    generateThinkingTime(transactionProfile, truncate = false) {
        // 5.2.5.4 For each transaction type, think time is taken independently from a negative exponential distribution. Think time, Tt, is computed from the following equation:
        //     Tt = -log(r) * u
        // where:
        // log = natural log (base e)
        // Tt = think time
        // r = random number uniformly distributed between 0 and 1
        // u = mean think time
        // Each distribution may be truncated at 10 times its mean value

        // 5.2.5.7 Minimum mean of think time distribution (u in the above equation)
        // New-Order: 12 sec.
        // Payment: 12 sec.
        // Order-Status: 10 sec.
        // Delivery: 5 sec.
        // Stock-Level: 5 sec.
        let meanThinkTime = this._spec.getMeanThinkingTime(transactionProfile);
        let thinkTime = -1 * Math.log(this._rand.rand()) * meanThinkTime;

        if (truncate) {
            thinkTime = Math.min(thinkTime, meanThinkTime * 10);
        }

        return thinkTime;
    }

    generateTransactionResponseTimeLimit(transactionProfile) {
        // 5.2.5.3 At least 90% of all transactions of each type must have a Transaction RT (see Clause 5.3.4) of less than 5 seconds each for New-Order, Payment, Order-Status, and Delivery, and 20 seconds for Stock-Level.
        let inTime = this._rand.number(1, 100) <= 90;
        return inTime ? this._spec.getResponseTimeLimit(transactionProfile) : undefined;
    }
}

module.exports = WaitTimeGenerator;