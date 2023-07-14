/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.stub;

import org.hyperledger.fabric.shim.ChaincodeStub;

public class UpdateThrottledChaincodeStub extends ChaincodeStubMiddlewareBase {

  UpdateThrottledChaincodeStub(ChaincodeStub nextLayer) {
    super(nextLayer);
  }
}
