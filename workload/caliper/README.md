# Hyperledger Caliper TPC-C Workload

## Overview

The current Node.JS project contains a TPC-C workload implementation for a custom [Hyperledger Caliper v0.3.2](https://hyperledger.github.io/caliper/v0.3.2/getting-started/). The project consists of the following main artifacts:

* [`benchconfig.yaml`](./benchconfig.yaml): the [benchmark configuration file](https://hyperledger.github.io/caliper/v0.3.2/architecture/#benchmark-configuration-file) that describes the rounds and their parameters that constitute the benchmark run.
* [`load.js`](./load.js): the [workload module](https://hyperledger.github.io/caliper/v0.3.2/architecture/#workload-modules) that implements the data ingestion phase of the TPC-C benchmark, i.e., when entries are inserted into the database.
* [`execute.js`](./execute.js): the [workload module](https://hyperledger.github.io/caliper/v0.3.2/architecture/#workload-modules) that implements the actual execution of the TPC-C benchmark, i.e., when transactions of different types are generated and submitted by terminals.
* [`tpcc/`](./tpcc/): the Caliper-independent implementation of TPC-C entry and transaction data generators.

The execution of the workload requires the following preparation steps:
1. Get the supported Caliper version.
2. Configure the workload.
3. Set up a Fabric network and Execute the workload with Caliper.

## Supported Caliper version

Currently, only the custom [klenik/caliper-fabric-1.4.17:experimental](https://hub.docker.com/layers/145313491/klenik/caliper-fabric-1.4.17/experimental/images/sha256-0f62c351669e2735f7ef4efda1436d20756fcc131155870d59699a99d2c70065?context=repo) Caliper Docker image can execute the implemented workload. The image pre-bound Caliper to the v1.4 Fabric SDK version, however, Fabric v2 networks might also be targeted (not tested) by [rebinding](https://hyperledger.github.io/caliper/v0.3.2/installing-caliper/#the-bind-command) during the benchmark execution.

## Workload configuration

The available configurations of the workload can be changed in the [benchconfig.yaml](./benchconfig.yaml) benchmark configuration file.

### Common attributes

Some common attributes are extracted into a single place in the configuration using [YAML anchors and aliases](https://yaml.org/spec/1.2.2/#71-alias-nodes):
* `tpccArgs.warehouses`: The number of warehouses (and corresponding entries) to insert into the Fabric state database during the data loading phase.
* `tpccArgs.scaleFactor`: The integer scaling factor to apply on the number of entries. A scale factor of `1` corresponds to the TPC-C standard specification. A scale factor of `10` scales **down** the number of entries to the tenth of the numbers specified by the standard. Used mainly for testing purposes, change it with care!

> **Notes** (details in the related [research paper](https://dl.acm.org/doi/10.1145/3477314.3507006)):
> * The standard specifies that 10 terminals must correspond to each warehouse. Increasing the terminal/warehouse ratio will result in increased data access conflicts.

### Data loading attributes (Round 1)

The first round of the benchmark is the data loading phase (`tests.rounds[0]` in the benchmark configuration). The following aspects can be configured for this round:
* `rateControl`: can be set to a supported [Caliper rate controller](https://hyperledger.github.io/caliper/v0.3.2/rate-controllers/) specification.
* `arguments.batchSize`: TPC-C entries are loaded in batches, i.e., a single Fabric transaction inserts multiple entries into the state database. This attribute specifies the number of entries in such a batch.
* `arguments.invokeTimeout`: Larger batches might take more time to be committed to the ledger, thus this attribute specifies the load-specific timeout for Fabric transactions.
* `arguments.warehouses`: See [above](#general-attributes).
* `arguments.scaleFactor`: See [above](#general-attributes).

> **Notes**:
> * The `requiredWorkers` attribute should be set to `1`, as the current implementation does not support distributed data loading. I.e., a single worker will load every entry into the Fabric state database. Our experience is that a single worker is enough to saturate Fabric during this write-heavy phase.
> * The optimal combination of the `rateControl` and `arguments.batchSize` attributes depends on the Fabric setup. Since data loading usually takes relatively long, first it is recommended to empirically determine the optimal combination using a single warehouse setup.

### Transaction execution attributes (Round 2)

The second round of the benchmark is the TPC-C execution phase (`tests.rounds[1]` in the benchmark configuration). The following aspects can be configured for this round:
* `requiredWorkers`: The number of Caliper workers to use for the execution phase of the benchmark. Defaults to the `test.workers.number` attribute value, i.e., all available Caliper workers will be used to generate the workload in the second round.
* `txDuration`: Specifies the duration of the measurement in milliseconds.
* `arguments.warehouses`: See [above](#general-attributes).
* `arguments.scaleFactor`: See [above](#general-attributes).
* `timing`: Boolean value that indicates whether the implementation should emulate the timing constraints of the standard. Disable (and violate the standard) for higher workload rate.
* `clients`: The number of TPC-C terminals to emulate within a single Caliper worker.

> **Notes** (details in the related [research paper](https://dl.acm.org/doi/10.1145/3477314.3507006)):
> * A single worker can emulate multiple terminals. However, too many terminals within a single worker can affect the precision of transaction scheduling. 

## Fabric setup and Caliper execution

Perform the following prerequisite steps to initialize the Fabric network and prepare for the benchmark execution:
1. Create a Fabric network;
2. Create a Fabric channel;
3. Deploy the TPC-C chaincode;
4. Assemble a Caliper network configuration;
5. Finally, run the benchmark.

### Create a Fabric network

The official [Fabric samples](https://github.com/hyperledger/fabric-samples/tree/release-1.4/first-network) or [MiniFabric](https://github.com/hyperledger-labs/minifabric) are convenient ways to easily deploy a local Fabric network for testing. [Hyperledger Bevel](https://github.com/hyperledger/bevel) and similar tools can help with deploying truly distributed, production-like networks.

### Create a Fabric channel

While Caliper v0.3.2 is capable of creating channels on a Fabric network, this feature is deprecated, and the official Fabric binaries (or samples) should be used to create the required channel.

### Deploy the TPC-C chaincode

While Caliper v0.3.2 is capable of deploying chaincodes to a Fabric network, this feature is deprecated, and the official Fabric binaries (or samples) should be used to deploy the TPC-C chaincode.

The workload generator is agnostic to the language and implementation details of the chaincode, but its exposed API (i.e., chaincode functions and their parameters) must conform to the TPC-C chaincode [described here](./../../smart-contract/hyperledger-fabric/v1/javascript).

### Assemble the Caliper network configuration

Caliper needs a [Fabric network configuration file](https://hyperledger.github.io/caliper/v0.3.2/fabric-config/#network-configuration-file-reference) to communicate with the backend Fabric network. A [sample network configuration](./networkconfig-sample.yaml) is provided that can be modified to suit the deployed Fabric network, channel, identities, and chaincode. 

The channel and chaincode IDs can be arbitrary, but the `contractID` must be set to `tpcc` in the configuration because the workload currently depends on that name to target the chaincode (see the remarks in the configuration file).

### Running the benchmark

At this point the `workload/caliper` directory contains every artifact required to run a Caliper benchmark: 
* benchmark configuration file;
* network configuration file;
* workload modules (for both benchmark phases);
* and a running, initialized Fabric network.

Check out the corresponding documentation page on [how to run Caliper in a Docker container](https://hyperledger.github.io/caliper/v0.3.2/installing-caliper/#using-the-docker-image).
> **Note:** don't forget to use the custom Caliper Docker image to execute the benchmark: [klenik/caliper-fabric-1.4.17:experimental](https://hub.docker.com/layers/145313491/klenik/caliper-fabric-1.4.17/experimental/images/sha256-0f62c351669e2735f7ef4efda1436d20756fcc131155870d59699a99d2c70065?context=repo)

## License

The project uses the Apache License Version 2.0. For more information see [NOTICES.md](./../../NOTICES.md), [CONTRIBUTORS.md](./../../CONTRIBUTORS.md), and [LICENSE](./../../LICENSE).