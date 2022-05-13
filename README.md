# Blockchain Benchmarks: TPC-C

This repository is the collection of artifacts (smart contract and workload implementations) relating to the benchmarking of blockchain solutions using the [TPC-C](https://www.tpc.org/tpcc/) performance benchmark.

## Users' Guide

Please refer to the documentation of the available smart contracts and workloads on how to use them.

## Developers' Guide

The preferred way of contribution is: 
1. Fork the repository;
2. Apply your changes;
3. Submit your changes for review and merging in the form of a pull request.

## Smart Contracts

The repository contains the following smart contract implementations of TPC-C:
* [Hyperledger Fabric v1.x - JavaScript](smart-contract/hyperledger-fabric/v1/javascript/README.md)

## Workloads

The repository contains the following workload implementations for TPC-C:
* [Hyperledger Caliper](workload/caliper/README.md)

## Reference Format

Please use the following information when you use or reference this project (or the related [research paper](https://dl.acm.org/doi/10.1145/3477314.3507006)) in your own work:
* ACM Reference Format:
    > Attila Klenik and Imre Kocsis. 2022. Porting a benchmark with a classic workload to blockchain: TPC-C on Hyperledger Fabric. In _The 37th ACM/SIGAPP Symposium on Applied Computing (SAC ’22), April 25–29, 2022, Virtual Event._ ACM, New York, NY, USA, 9 pages. https://doi.org/10.1145/3477314.3507006
* BibTeX file format:
    ```
    @inproceedings{KlenikKocsisTpcc2022,
        author = {Klenik, Attila and Kocsis, Imre},
        booktitle = {{The 37th ACM/SIGAPP Symposium on Applied Computing (SAC '22), April 25–29, 2022, Virtual Event.}},
        doi = {10.1145/3477314.3507006},
        pages = {290--299},
        title = {{Porting a benchmark with a classic workload to blockchain: TPC-C on Hyperledger Fabric}},
        year = {2022}
    }
    ```

## Acknowledgement

This research was partially funded by the EC and NKFIH through the Arrowhead Tools project (EU grant No. 826452, NKFIH grant 2019-2.1.3-NEMZ ECSEL-2019-00003).

## License

The project uses the Apache License Version 2.0. For more information see [NOTICES.md](NOTICES.md), [CONTRIBUTORS.md](CONTRIBUTORS.md), and [LICENSE](LICENSE).