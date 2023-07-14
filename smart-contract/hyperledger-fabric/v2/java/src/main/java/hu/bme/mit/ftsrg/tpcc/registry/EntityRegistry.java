package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.EntityInterface;
import hu.bme.mit.ftsrg.tpcc.stub.EnhancedContext;
import hu.bme.mit.ftsrg.tpcc.utils.ParseUtils;

//import java.util.logging.Logger;
import org.hyperledger.fabric.shim.ledger.CompositeKey;

public class EntityRegistry implements RegistryInterface {
  //private static final Logger LOGGER = Logger.getLogger(EntityRegistry.class.getName());

  public EntityRegistry(EnhancedContext ctx) {}

  @Override
  public void create(EnhancedContext ctx, EntityInterface entity) {
    //LOGGER.info("Starting create Entity " + entity);

    // ???? do we need perseUtils to parse entity here????
    String type = entity.getType();
    String[] keyParts = entity.getKeyParts();

    CompositeKey key = ctx.getStub().createCompositeKey(type, keyParts);

    byte[] buffer = entity.toBuffer();
    ctx.getStub().putState(key.toString(), buffer);
  }

  @Override
  public EntityInterface read(EnhancedContext ctx, EntityInterface entity) {
    //LOGGER.info("retrieve entity");
    String type = entity.getType();
    String[] keyParts = entity.getKeyParts();
    CompositeKey key = ctx.getStub().createCompositeKey(type, keyParts);
    byte[] data = ctx.getStub().getState(key.toString());
    String entry = entity.fromBuffer(data);
    
    if (entry == null) {
      try {
        throw new Exception("Could not retrieve Entity");
      } catch (Exception e) {
        e.printStackTrace();
      }
    } 

    return entry;
  }

  @Override
  public void update(EnhancedContext ctx, EntityInterface entity) {
    //LOGGER.info("Begin update entity ");
    String type = entity.getType();
    String[] keyParts = entity.getKeyParts();
    CompositeKey key = ctx.getStub().createCompositeKey(type, keyParts);
    byte[] buffer = entity.toBuffer();
    ctx.getStub().putState(key.toString(), buffer);    
  }

  @Override
  public void delete(EnhancedContext ctx, EntityInterface entity) {
    //LOGGER.info("Begin delete entity");
    String type = entity.getType();
    String[] keyParts = entity.getKeyParts();    
    CompositeKey key = ctx.getStub().createCompositeKey(type, keyParts);
    ctx.getStub().delState(key.toString());
  }
}
