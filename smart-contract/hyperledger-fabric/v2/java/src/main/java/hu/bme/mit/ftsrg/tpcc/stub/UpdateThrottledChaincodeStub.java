package hu.bme.mit.ftsrg.tpcc.stub;

import org.hyperledger.fabric.shim.ChaincodeStub;

public class UpdateThrottledChaincodeStub extends ChaincodeStubMiddlewareBase {

  UpdateThrottledChaincodeStub(ChaincodeStub nextLayer) {
    super(nextLayer);
  }

  public byte[] read(String key) {
    return this.nextLayer.getState(key);
  }

  public void write(String key, byte[] value) {

    this.nextLayer.putState(key, value);
  }

  public void delete(String key) {

    this.nextLayer.delState(key);
  }

  public void dispose() {
    // empty function
  }
}
