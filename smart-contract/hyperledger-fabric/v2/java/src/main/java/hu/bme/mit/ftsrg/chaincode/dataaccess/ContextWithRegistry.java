package hu.bme.mit.ftsrg.chaincode.dataaccess;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

public class ContextWithRegistry extends Context {

  public final Registry registry = new RegistryImpl();

  public ContextWithRegistry(final ChaincodeStub stub) {
    super(stub);
  }
}
