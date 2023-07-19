package hu.bme.mit.ftsrg.tpcc.stub;

import hu.bme.mit.ftsrg.tpcc.registry.EntityRegistry;
import hu.bme.mit.ftsrg.tpcc.registry.RegistryInterface;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

public final class EnhancedContext extends Context {

  public final RegistryInterface registry = new EntityRegistry();

  public EnhancedContext(final ChaincodeStub stub) {
    super(stub);
  }
}
