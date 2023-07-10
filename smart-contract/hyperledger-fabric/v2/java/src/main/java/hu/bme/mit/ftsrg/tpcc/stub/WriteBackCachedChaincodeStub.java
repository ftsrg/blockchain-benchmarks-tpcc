package hu.bme.mit.ftsrg.tpcc.stub;

import org.hyperledger.fabric.shim.ChaincodeStub;

public class WriteBackCachedChaincodeStub extends ChaincodeStubMiddleware {

  WriteBackCachedChaincodeStub(ChaincodeStub nextLayer) {
    super(nextLayer);
  }
}
