package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.EntityInterface;
//import hu.bme.mit.ftsrg.tpcc.stub.ChaincodeStubMiddlewareBase;

import java.util.logging.Logger;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ledger.CompositeKey;

public class EntityRegistry implements RegistryInterface {
    private static final Logger LOGGER = Logger.getLogger(EntityRegistry.class.getName());  
    Context context;

  EntityRegistry(Context ctx) {
    this.context = ctx;
  }

  @Override
  public void create(EntityInterface entity) {
    LOGGER.info("Starting create Entity " + entity);
    String type = entity.getType();
    String [] keyParts = entity.getKeyParts();
    
    CompositeKey key = context.getStub().createCompositeKey(type, keyParts);
           
    byte[] buffer = entity.toBuffer();
    context.getStub().putState(key.toString(), buffer);
    }

  @Override
  public EntityInterface read(EntityInterface entity) {

    throw new UnsupportedOperationException("Unimplemented method 'getEntity'");
  }

  @Override
  public void update(EntityInterface entity) {
    
    throw new UnsupportedOperationException("Unimplemented method 'update'");
  }

  @Override
  public void delete(EntityInterface entity) {
    
    throw new UnsupportedOperationException("Unimplemented method 'delete'");
  }
}
