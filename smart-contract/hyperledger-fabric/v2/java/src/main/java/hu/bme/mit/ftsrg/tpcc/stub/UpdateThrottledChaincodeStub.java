package hu.bme.mit.ftsrg.tpcc.stub;

import org.hyperledger.fabric.shim.ChaincodeStub;

public class UpdateThrottledChaincodeStub extends ChaincodeStubMiddleware {

  UpdateThrottledChaincodeStub(ChaincodeStub nextLayer) {
    super(nextLayer);
  }
}
