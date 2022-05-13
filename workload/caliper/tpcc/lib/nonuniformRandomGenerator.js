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

const SYLLABLES = [ 'BAR', 'OUGHT', 'ABLE', 'PRI', 'PRES', 'ESE', 'ANTI', 'CALLY', 'ATION', 'EING'];

class NonuniformRandomGenerator {
    constructor(nonuniformRandomConstant, uniformRandomGenerator) {
        this.nonuniformRandomConstant = nonuniformRandomConstant;
        this.uniformRandomGenerator = uniformRandomGenerator;
    }

    /**
     *
     * @param {number} a
     * @param {number} x
     * @param {number} y
     * @return {number}
     */
    NURand(a, x, y) {
        let c;
        if (a === 255) {
            c = this.nonuniformRandomConstant.cLast;
        } else if (a === 1023) {
            c = this.nonuniformRandomConstant.cId;
        } else if (a === 8191) {
            c = this.nonuniformRandomConstant.orderLineItemId;
        } else {
            throw new Error(`Unsupported "a" value ${a}`);
        }

        let urg = this.uniformRandomGenerator;

        return (((urg.number(0, a) | urg.number(x, y)) + c) % (y - x + 1)) + x;
    }

    /**
     *
     * @param {number} number
     * @return {string}
     */
    makeLastName(number) {
        let indices = [Math.floor(number / 100), Math.floor(number / 100) % 10, number % 10];
        return indices.map(i => SYLLABLES[i]).join('');
    }

    /**
     *
     * @param {number} maxCID
     * @return {string}
     */
    makeRandomLastName(maxCID) {
        let min_cid = 999;
        if ((maxCID - 1) < min_cid) {
            min_cid = maxCID - 1;
        }

        return this.makeLastName(this.NURand(255, 0, min_cid));
    }
}

module.exports = NonuniformRandomGenerator;