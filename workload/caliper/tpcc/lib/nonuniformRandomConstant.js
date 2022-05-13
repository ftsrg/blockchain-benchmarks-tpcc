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

/**
 * Returns true if the cRun value is valid for running. See TPC-C 2.1.6.1.
 * @param {number} cRun The cRun value.
 * @param {number} cLoad The cLoad value.
 * @return {boolean} Indicates whether the cRun value is valid for running.
 */
function validCRun(cRun, cLoad) {
    const cDelta = Math.abs(cRun - cLoad);
    return (65 <= cDelta)
        && (cDelta <= 119)
        && (cDelta !== 96)
        && (cDelta !== 112);
}

class NonuniformRandomConstant {
    constructor(cLast, cId, orderLineItemId) {
        this._cLast = cLast;
        this._cId = cId;
        this._orderLineItemId = orderLineItemId;
    }

    /**
     * Create random NURand constants, appropriate for loading the database.
     * @property {UniformRandomGenerator} uniformRandomGenerator
     * @return {NonuniformRandomConstant} The NURand variable.
     */
    static makeForLoad(uniformRandomGenerator) {
        const cLast = uniformRandomGenerator.number(0, 255);
        const cId = uniformRandomGenerator.number(0, 1023);
        const orderLineItemId = uniformRandomGenerator.number(0, 8191);

        return new NonuniformRandomConstant(cLast, cId, orderLineItemId);
    }

    /**
     * Create random NURand constants for running TPC-C. TPC-C 2.1.6.1. specifies the valid range for these constants.
     * @param {NonuniformRandomConstant} loadC The constant used for populating the database.
     * @param {UniformRandomGenerator} uniformRandomGenerator
     * @return {NonuniformRandomConstant} The NURand variable.
     */
    static makeForRun(loadC, uniformRandomGenerator) {
        let cRun = uniformRandomGenerator.number(0, 255);
        while (!validCRun(cRun, loadC.cLast)) {
            cRun = uniformRandomGenerator.number(0, 255);
        }

        let cId = uniformRandomGenerator.number(0, 1023);
        let orderLineItemId = uniformRandomGenerator.number(0, 8191);
        return new NonuniformRandomConstant(cRun, cId, orderLineItemId);
    }

    /**
     *
     * @return {number}
     */
    get orderLineItemId() {
        return this._orderLineItemId;
    }

    /**
     *
     * @return {number}
     */
    get cId() {
        return this._cId;
    }

    /**
     *
     * @return {number}
     */
    get cLast() {
        return this._cLast;
    }
}


module.exports = NonuniformRandomConstant;