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

class UniformRandomGenerator {
    constructor(randFunction = undefined, randIntRangeFunction = undefined) {
        this.randBase = randFunction || Math.random;
        this.randRange = randIntRangeFunction || ((minimum, maximum) => Math.floor((this.randBase() * (maximum - minimum)) + minimum));
    }

    /**
     *
     * @return {number}
     */
    rand() {
        return this.randBase();
    }

    /**
     *
     * @param {number} minimum
     * @param {number} maximum
     * @return {number}
     */
    number(minimum, maximum) {
        return this.randRange(minimum, maximum);
    }

    /**
     *
     * @param {number} minimum
     * @param {number} maximum
     * @param {number} excluding
     * @return {number}
     */
    numberExcluding(minimum, maximum, excluding) {
        // Generate 1 less number than the range
        let num = this.number(minimum, maximum - 1);

        // Adjust the numbers to remove excluding
        if (num >= excluding) {
            num += 1;
        }

        return num;
    }

    /**
     *
     * @param {number} decimal_places
     * @param {number} minimum
     * @param {number} maximum
     * @return {number}
     */
    fixedPoint(decimal_places, minimum, maximum) {
        let multiplier = 1;

        for (let i = 0; i < decimal_places; i++) {
            multiplier *= 10;
        }

        let int_min = Math.floor(minimum * multiplier + 0.5);
        let int_max = Math.floor(maximum * multiplier + 0.5);

        return this.number(int_min, int_max) / multiplier;
    }

    /**
     *
     * @param {number} numUnique
     * @param {number} minimum
     * @param {number} maximum
     * @return {Set<number>}
     */
    selectUniqueIds(numUnique, minimum, maximum) {
        let rows = new Set();

        for (let i = 0; i < numUnique; i++) {
            let index = this.number(minimum, maximum);
            while (rows.has(index)) {
                index = this.number(minimum, maximum);
            }

            rows.add(index);
        }

        return rows;
    }

    /**
     *
     * @param {number} minimum_length
     * @param {number} maximum_length
     * @return {string}
     */
    astring(minimum_length, maximum_length) {
        return this.randomString(minimum_length, maximum_length, 'a', 26);
    }

    /**
     *
     * @param {number} minimum_length
     * @param {number} maximum_length
     * @return {string}
     */
    nstring(minimum_length, maximum_length) {
        return this.randomString(minimum_length, maximum_length, '0', 10);
    }

    /**
     *
     * @param {number} minimum_length
     * @param {number} maximum_length
     * @param {string} base
     * @param {number} numCharacters
     * @return {string}
     */
    randomString(minimum_length, maximum_length, base, numCharacters) {
        let length = this.number(minimum_length, maximum_length);
        let baseByte = base.charCodeAt(0);
        let string = '';

        for (let i = 0; i < length; i++) {
            string += String.fromCharCode(baseByte + this.number(0, numCharacters - 1));
        }

        return string;
    }
}

module.exports = UniformRandomGenerator;