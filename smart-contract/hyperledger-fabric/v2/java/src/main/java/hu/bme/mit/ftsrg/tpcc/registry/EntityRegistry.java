package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.EntityInterface;
import hu.bme.mit.ftsrg.tpcc.stub.EnhancedContext;
import java.util.logging.Logger;
import org.hyperledger.fabric.shim.ledger.CompositeKey;

public class EntityRegistry implements RegistryInterface {
  private static final Logger LOGGER = Logger.getLogger(EntityRegistry.class.getName());

  public EntityRegistry(EnhancedContext ctx) {}

  @Override
  public void create(EnhancedContext ctx, EntityInterface entity) {
    LOGGER.info("Starting create Entity " + entity);
    String type = entity.getType();
    String[] keyParts = entity.getKeyParts();

    CompositeKey key = ctx.getStub().createCompositeKey(type, keyParts);

    byte[] buffer = entity.toBuffer();
    ctx.getStub().putState(key.toString(), buffer);
  }

  @Override
  public EntityInterface read(EnhancedContext ctx, EntityInterface entity) {

    throw new UnsupportedOperationException("Unimplemented method 'getEntity'");
  }

  @Override
  public void update(EnhancedContext ctx, EntityInterface entity) {

    throw new UnsupportedOperationException("Unimplemented method 'update'");
  }

  @Override
  public void delete(EnhancedContext ctx, EntityInterface entity) {

    throw new UnsupportedOperationException("Unimplemented method 'delete'");
  }
}
