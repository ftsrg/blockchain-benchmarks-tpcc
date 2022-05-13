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

const { CaliperUtils } = require('@hyperledger/caliper-core');

const TPCC = require('./tpcc');
const TpccProfiles = require('./tpcc').Constants.TransactionProfiles;

let blockchain, context;
let timing;

let invokeMapping = {};
invokeMapping[TpccProfiles.STOCK_LEVEL] = {
    ccFunction: 'doStockLevel',
    adapterFunction: 'querySmartContract',
    deferred: false
};

invokeMapping[TpccProfiles.ORDER_STATUS] = {
    ccFunction: 'doOrderStatus',
    adapterFunction: 'querySmartContract',
    deferred: false
};

invokeMapping[TpccProfiles.DELIVERY] = {
    ccFunction: 'doDelivery',
    adapterFunction: 'invokeSmartContract',
    deferred: true
};

invokeMapping[TpccProfiles.PAYMENT] = {
    ccFunction: 'doPayment',
    adapterFunction: 'invokeSmartContract',
    deferred: false
};

invokeMapping[TpccProfiles.NEW_ORDER] = {
    ccFunction: 'doNewOrder',
    adapterFunction: 'invokeSmartContract',
    deferred: false
};

let clientData = [];
let txQueue = [];
let nextTx;

module.exports.info  = 'Caliper workload module for TPC-C execution';

module.exports.prepareWorkerParameters = async (workerParameters) => {
    return workerParameters;
};

module.exports.init = async (bc, contx, args, workerParameters, workerIndex, totalWorkers) => {
    blockchain = bc;
    context = contx;

    if (!workerParameters.tpcc) {
        throw new Error('Missing TPC-C seed configuration for executor workload module');
    }

    timing = Boolean(args.timing || false);
    const warehouses = Number(args.warehouses || 1);
    const scaleFactor = parseFloat(args.scaleFactor || 40);
    const runParams = workerParameters.tpcc.run;
    const scale = TPCC.ScaleParameters.makeWithScaleFactor(warehouses, scaleFactor);

    const clientNum = Number(args.clients || 10);

    for (let i = 0; i < clientNum; i++) {
        const urand = new TPCC.UniformRandomGenerator();
        const nurConst = new TPCC.NonuniformRandomConstant(runParams.cLast, runParams.cId, runParams.orderLineItemId);
        const nurand = new TPCC.NonuniformRandomGenerator(nurConst, urand);
        const txGen = new TPCC.TransactionProfileGenerator(scale, urand, nurand,
            (((workerIndex * clientNum) + i) % scale.warehouses) + 1, (((workerIndex * clientNum) + i) % scale.districtsPerWarehouse) + 1);
        const timingGen = new TPCC.WaitTimeGenerator(TPCC.WaitTimeSpecification.makeDefault(), urand);

        clientData.push({
            terminalIndex: i,
            txGen: txGen,
            timingGen: timingGen
        });
    }
};

module.exports.run = async () => {
    if (!nextTx) {
        fillQueue();
        nextTx = txQueue.shift();
    }

    let currentTx = nextTx;
    nextTx = undefined;

    let results = await (blockchain[currentTx.invokeInfo.adapterFunction](context, 'tpcc', '', currentTx.txArgs, currentTx.responseTimeLimit / 1000));
    let result = results[0];

    currentTx.finished = true;
    currentTx.finishTime = result.GetTimeFinal();

    return results;
};

module.exports.end = async () => { };

function generateNextTx(client, prevTxData = undefined) {
    const terminalId = client.terminalIndex;
    let nextTx = client.txGen.next();
    let invokeInfo = invokeMapping[nextTx.profile];

    // add the timestamp for History primary key construction
    if (nextTx.profile === TpccProfiles.PAYMENT) {
        nextTx.parameters.h_date = (new Date()).toISOString();
    }

    let txData = {
        terminalIndex: terminalId,
        finished: false,
        tpcc: nextTx,
        invokeInfo: invokeInfo,
        txArgs: {
            chaincodeFunction: invokeInfo.ccFunction,
            chaincodeArguments: [JSON.stringify(nextTx.parameters)]
        },
        menuResponseTime: client.timingGen.generateMenuResponseTime(),
        keyingTime: client.timingGen.generateKeyingTime(nextTx.profile),
        responseTimeLimit: client.timingGen.generateTransactionResponseTimeLimit(nextTx.profile) || 60000,
        thinkTime: client.timingGen.generateThinkingTime(nextTx.profile, true)
    };

    if (timing) {
        if (prevTxData) {
            txData.startTime = prevTxData.finishTime + (prevTxData.invokeInfo.deferred ? 0 : prevTxData.thinkTime) + txData.menuResponseTime + txData.keyingTime;
        } else {
            txData.startTime = Date.now() + txData.menuResponseTime + txData.keyingTime;
        }
    } else {
        txData.startTime = prevTxData ? prevTxData.finishTime : Date.now();
    }

    txQueue.push(txData);
    if (invokeInfo.deferred) {
        txData.finishTime = txData.startTime;
        generateNextTx(client, txData);
    }
}

function fillQueue() {
    for (let i = 0; i < clientData.length; i++) {
        let client = clientData[i];
        let terminalTxs = txQueue.filter(tx => tx.terminalIndex === i);

        if (terminalTxs.length === 0) {
            generateNextTx(client);
        } else {
            let lastFinishedTx = terminalTxs.filter(tx => tx.finished).sort((a, b) => b.startTime - a.startTime)[0];
            if (lastFinishedTx) {
                generateNextTx(client, lastFinishedTx);
            }
        }
    }

    txQueue = txQueue.filter(tx => !tx.finished).sort((a, b) => a.startTime - b.startTime);
}

class SequentialRateController {
    async init(msg) { }

    async applyRateControl(start, idx, recentResults, resultStats) {
        fillQueue();
        nextTx = txQueue.shift();

        const diff = nextTx.startTime - Date.now();
        if (diff < 10) {
            return;
        }

        await CaliperUtils.sleep(diff);
    }

    async end() { }
}

function createRateController(opts, clientIdx, roundIdx) {
    return new SequentialRateController();
}

module.exports.createRateController = createRateController;