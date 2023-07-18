package hu.bme.mit.ftsrg.tpcc.stub;

import hu.bme.mit.ftsrg.tpcc.registry.EntityRegistry;
import hu.bme.mit.ftsrg.tpcc.registry.RegistryInterface;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

public class EnhancedContext extends Context {
  public RegistryInterface registry;

  public EnhancedContext(ChaincodeStub stub) {
    super(stub);
    this.registry = new EntityRegistry();
  }
}
