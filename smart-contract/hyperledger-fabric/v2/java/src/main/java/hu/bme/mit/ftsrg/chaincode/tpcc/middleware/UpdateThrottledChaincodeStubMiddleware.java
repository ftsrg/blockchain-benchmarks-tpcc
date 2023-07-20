/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.middleware;

import hu.bme.mit.ftsrg.chaincode.dataaccess.ChaincodeStubMiddlewareBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

public final class UpdateThrottledChaincodeStubMiddleware extends ChaincodeStubMiddlewareBase {

  public UpdateThrottledChaincodeStubMiddleware(final ChaincodeStub nextLayer) {
    super(nextLayer);
  }

  /* TODO implement ... */
}
