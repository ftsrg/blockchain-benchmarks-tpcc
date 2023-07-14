package hu.bme.mit.ftsrg.tpcc.stub;

import hu.bme.mit.ftsrg.tpcc.registry.EntityRegistry;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

public class EnhancedContext extends Context {

  public EnhancedContext(ChaincodeStub stub) {
    super(stub);
    this.registry = new EntityRegistry(this);
  }

  public EntityRegistry registry;
}
