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

const TPCC = require('./tpcc');

let blockchain, context;

let entryGen;
let batchSize;
let invokeTimeout;

module.exports.info  = 'Caliper workload module for TPC-C data loading';

module.exports.prepareWorkerParameters = async (workerParameters) => {
    if (!workerParameters.tpcc) {
        const urand = new TPCC.UniformRandomGenerator();
        const loadNRUConst = TPCC.NonuniformRandomConstant.makeForLoad(urand);
        const runNRUConst = TPCC.NonuniformRandomConstant.makeForRun(loadNRUConst, urand);

        workerParameters.tpcc = {
            load: {
                cLast: loadNRUConst.cLast,
                cId: loadNRUConst.cId,
                orderLineItemId: loadNRUConst.orderLineItemId
            },
            run: {
                cLast: runNRUConst.cLast,
                cId: runNRUConst.cId,
                orderLineItemId: runNRUConst.orderLineItemId
            }
        };
    }

    return workerParameters;
};

module.exports.init = async (bc, contx, args, workerParameters, workerIndex, totalWorkers) => {
    blockchain = bc;
    context = contx;

    if (!workerParameters.tpcc) {
        throw new Error('Missing TPC-C seed configuration for loader workload module');
    }

    batchSize = Number(args.batchSize || 50);
    invokeTimeout = Number(args.invokeTimeout || 60);
    const warehouses = Number(args.warehouses || 1);
    const scaleFactor = parseFloat(args.scaleFactor || 40);
    const loadParams = workerParameters.tpcc.load;

    const scale = TPCC.ScaleParameters.makeWithScaleFactor(warehouses, scaleFactor);
    const urand = new TPCC.UniformRandomGenerator();
    const nurConst = new TPCC.NonuniformRandomConstant(loadParams.cLast, loadParams.cId, loadParams.orderLineItemId);
    const nurand = new TPCC.NonuniformRandomGenerator(nurConst, urand);

    let warehouseIds = [];
    for (let i = 1; i <= scale.warehouses; i++) {
        warehouseIds.push(i);
    }

    entryGen = new TPCC.TableEntryGenerator(scale, warehouseIds, true, urand, nurand);
};

module.exports.run = async (cancellationToken) => {
    let entries = [];
    while (entryGen.hasNext() && entries.length < batchSize) {
        let entry = entryGen.next();
        entries.push(entry);
    }

    if (!entryGen.hasNext()) {
        cancellationToken.cancel('No more TPC-C table entries to generate.');
    }

    const txArgs = {
        chaincodeFunction: 'createEntries',
        chaincodeArguments: [JSON.stringify({ entries })]
    };

    return await blockchain.invokeSmartContract(context, 'tpcc', '', txArgs, invokeTimeout);
};

module.exports.end = async () => {};