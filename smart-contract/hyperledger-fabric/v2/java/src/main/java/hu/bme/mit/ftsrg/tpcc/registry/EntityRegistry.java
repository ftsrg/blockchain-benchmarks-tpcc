package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.EntityInterface;
import hu.bme.mit.ftsrg.tpcc.stub.EnhancedContext;
import hu.bme.mit.ftsrg.tpcc.stub.WriteBackCachedChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;

public class EntityRegistry implements RegistryInterface {
  // private static final Logger LOGGER = Logger.getLogger(EntityRegistry.class.getName());
  private WriteBackCachedChaincodeStub cache;
  EnhancedContext ctx;

  public EntityRegistry(EnhancedContext ctx) {}

  private String _getKey(EntityInterface obj) {
    CompositeKey compositeKey =
        this.ctx.getStub().createCompositeKey(obj.getType(), obj.getKeyParts());
    return compositeKey.toString();
  }

  @Override
  public void _create(EntityInterface entity, boolean strict) {
    if (strict) {
      assertNotExists(entity);
    }
    String key = _getKey(entity);
    byte[] buffer = entity.toBuffer();
    this.cache.write(key, buffer);
  }

  @Override
  public EntityInterface _readAndParse(EntityInterface entity) {
    // LOGGER.info("retrieve entity");
    String key = _getKey(entity);
    byte[] data = this.cache.read(key);
    ;
    if (data == null || data.length == 0) {
      throw new Error(
          "Entity with key \"" + key + "\" does not exist on the ledger, thus cannot parse it");
    }

    entity.fromBuffer(data);
    return entity;
  }

  @Override
  public void _update(EntityInterface entity, boolean strict) {
    // LOGGER.info("Begin update entity ");
    if (strict) {
      assertExists(entity);
    }
    String key = _getKey(entity);
    byte[] buffer = entity.toBuffer();
    this.cache.write(key, buffer);
  }

  @Override
  public void _delete(EntityInterface entity, boolean strict) {
    // LOGGER.info("Begin delete entity");
    if (strict) {
      assertExists(entity);
    }
    String key = _getKey(entity);
    this.cache.delete(key);
  }

  @Override
  public void _dispose() {
    this.cache.dispose();
  }

  private boolean _keyExists(String key) {
    byte[] valueOnLedger = this.cache.read(key);
    return valueOnLedger != null && valueOnLedger.length > 0;
  }

  public boolean exists(EntityInterface obj) {
    return _keyExists(_getKey(obj));
  }

  public void assertNotExists(EntityInterface obj) {
    if (exists(obj)) {
      throw new Error("Entity with key \"" + _getKey(obj) + "\" already exists on the ledger");
    }
  }

  public void assertExists(EntityInterface obj) {
    if (!exists(obj)) {
      throw new Error("Entity with key \"" + _getKey(obj) + "\" does not exist on the ledger");
    }
  }
}
