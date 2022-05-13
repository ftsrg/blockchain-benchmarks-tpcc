# Hyperledger Fabric v1 TPC-C chaincode (JavaScript)

## Overview

This project contains the TPC-C transaction profile implementations in the form of a Hyperledger Fabric v1 chaincode, written in JavaScript.

## Chaincode API

The chaincode exposes the following functions:
* `createEntries`
* `doDelivery`
* `doNewOrder`
* `doOrderStatus`
* `doPayment`
* `doStockLevel`

All functions receive a Fabric transaction context as their first parameter, as required by the [high-level Contract programming model](https://hyperledger.github.io/fabric-chaincode-node/release-1.4/api/tutorial-deep-dive-contract-interface.html).

Every function has only one additional parameter (`parameters`) that is a string (as required by the [low-level Fabric chaincode API](https://hyperledger.github.io/fabric-chaincode-node/release-1.4/api/fabric-shim.ChaincodeStub.html#getArgs__anchor)). The parameter value must be a JSON string encoding of a JavaScript object that contains the inputs of the given function as attributes, detailed in the following sections.

To summarize, all functions have the following signature: `functionName(ctx, parameters)`, where `ctx` is the transaction context provided by the Fabric chaincode SDK, and `parameters` is the JSON string encoding of the function-specific inputs (i.e., the JSON stringified form of the object `{ "param1": "value1", ..., "paramN": "valueN" }`).

### createEntries

The function creates the received batch of entries on the ledger.

Attributes of `parameters`:
* `entries` (`object[]`): The list of entries to create on the ledger. The items of the list have to following high-level schema:
    * `table` (`strting`): The table name that the entry(type) corresponds to.
    * `data` (`object`): The attributes of the entry. The table-specific attributes of entries are [specified by the standard](https://www.tpc.org/tpc_documents_current_versions/pdf/tpc-c_v5.11.0.pdf#page=11). The chaincode follows the same naming convention (but with all lowercase letters).

### doDeliveries

The function performs the [Delivery](https://www.tpc.org/tpc_documents_current_versions/pdf/tpc-c_v5.11.0.pdf#page=40) read-write TX profile.

Attributes of `parameters`:
* `w_id` (`number`): The warehouse ID.
* `o_carrier_id` (`number`): The carrier ID for the order.
* `ol_delivery_d` (`string`): The delivery date string (in ISO format) for the order.

### doNewOrder

The function performs the [New Order](https://www.tpc.org/tpc_documents_current_versions/pdf/tpc-c_v5.11.0.pdf#page=28) read-write TX profile.

Attributes of `parameters`:
* `w_id` (`number`): The warehouse ID.
* `d_id` (`number`): The district ID.
* `c_id` (`number`): The customer ID.
* `o_entry_d` (`string`): The date string (in ISO format) for the order entry.
* `i_ids` (`number[]`): The array of item IDs for the order lines.
* `i_w_ids` (`number[]`): The array of warehouse IDs for the order lines.
* `i_qtys` (`number[]`): The array of quantities for the order lines.

### doOrderStatus

The function performs the [Order Status](https://www.tpc.org/tpc_documents_current_versions/pdf/tpc-c_v5.11.0.pdf#page=37) read-only TX profile.

Attributes of `parameters`:
* `w_id` (`number`): The warehouse ID.
* `d_id` (`number`): The district ID.
* `c_id` (`number`): The customer ID, if provided.
* `c_last` (`string`): The last name of the customer, if provided.

### doPayment

The function performs the [Payment](https://www.tpc.org/tpc_documents_current_versions/pdf/tpc-c_v5.11.0.pdf#page=33) read-write TX profile.

Attributes of `parameters`:
* `w_id` (`number`): The warehouse ID.
* `d_id` (`number`): The district ID.
* `h_amount` (`number`): The payment amount.
* `c_w_id` (`number`): The warehouse ID to which the customer belongs to.
* `c_d_id` (`number`): The district ID to which the customer belongs to.
* `c_id` (`number`): The customer ID.
* `c_last` (`string`): The last name of the customer.
* `h_date` (`string`): The payment date string (in ISO format).

### doStockLevel

The function performs the [Stock Level](https://www.tpc.org/tpc_documents_current_versions/pdf/tpc-c_v5.11.0.pdf#page=44) read-only TX profile.

Attributes of `parameters`:
* `w_id` (`number`): The warehouse ID.
* `d_id` (`number`): The district ID.
* `threshold` (`number`): The threshold of minimum quantity in stock to report.

## License

The project uses the Apache License Version 2.0. For more information see [NOTICES.md](./../../NOTICES.md), [CONTRIBUTORS.md](./../../CONTRIBUTORS.md), and [LICENSE](./../../LICENSE).