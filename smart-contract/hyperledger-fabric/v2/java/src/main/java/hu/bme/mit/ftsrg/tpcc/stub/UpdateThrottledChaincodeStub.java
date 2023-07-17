package hu.bme.mit.ftsrg.tpcc.stub;

import org.hyperledger.fabric.shim.ChaincodeStub;

public class UpdateThrottledChaincodeStub extends ChaincodeStubMiddlewareBase {
  // private Context context;

  UpdateThrottledChaincodeStub(ChaincodeStub nextLayer) {
    super(nextLayer);

    // this.context = ctx;
  }

  public byte[] read(String key) {
    return this.nextLayer.getState(key);
    // return this.context.getStub().getState(key);
  }

  public void write(String key, byte[] value) {
    // this.context.getStub().putState(key, value);
    this.nextLayer.putState(key, value);
  }

  public void delete(String key) {
    // this.context.getStub().deleteState(key);
    this.nextLayer.delState(key);
  }

  public void dispose() {
    // empty function
  }
}
