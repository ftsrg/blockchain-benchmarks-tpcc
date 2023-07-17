package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.EntityFactory;
import hu.bme.mit.ftsrg.tpcc.entities.SerializableEntityInterface;
import hu.bme.mit.ftsrg.tpcc.stub.EnhancedContext;
import hu.bme.mit.ftsrg.tpcc.stub.WriteBackCachedChaincodeStub;

import java.util.ArrayList;
import java.util.Iterator;

import org.hyperledger.fabric.shim.ledger.KeyValue;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ledger.CompositeKey;

public class EntityRegistry implements RegistryInterface {
  // private static final Logger LOGGER = Logger.getLogger(EntityRegistry.class.getName());
  private WriteBackCachedChaincodeStub cache;
  Context ctx;

  public EntityRegistry(Context ctx) {}

  private <Type extends SerializableEntityInterface> String _getKey(Context ctx, Type obj) {
    CompositeKey compositeKey =
        this.ctx.getStub().createCompositeKey(obj.getType(), obj.getKeyParts());
    return compositeKey.toString();
  }

  
  @Override
  public <Type extends SerializableEntityInterface> void _create(Context ctx, Type entity, boolean strict) {
    if (strict) {
      assertNotExists(entity);
    }
    String key = _getKey(ctx, entity);
    byte[] buffer = entity.toBuffer();
    this.cache.write(key, buffer);
  }

  @Override
  public <Type extends SerializableEntityInterface> void _update(Context ctx, Type entity, boolean strict) {
      // LOGGER.info("Begin update entity ");
      if (strict) {
        assertExists(entity);
      }
      String key = _getKey(ctx, entity);
      byte[] buffer = entity.toBuffer();
      this.cache.write(key, buffer);
  }

  @Override
  public <Type extends SerializableEntityInterface> void _delete(Context ctx, Type entity, boolean strict) {
      // LOGGER.info("Begin delete entity");
      if (strict) {
        assertExists(entity);
      }
      String key = _getKey(ctx, entity);
      this.cache.delete(key);
  }

  @Override
  public <Type extends SerializableEntityInterface> Type _readAndParse(Context ctx, Type entity) {
      // LOGGER.info("retrieve entity");
      String key = _getKey(ctx, entity);
      byte[] data = this.cache.read(key);
  
      if (data == null || data.length == 0) {
        throw new Error(
            "Entity with key \"" + key + "\" does not exist on the ledger, thus cannot parse it");
      }
  
      entity.fromBuffer(data);
      return entity;
  }

  @Override
  public <Type extends SerializableEntityInterface> List<Type> _readAll(Context ctx, Type entityTemplate) {
    List<Type> entities = new ArrayList<>();
    String compositeKey =
        this.ctx.getStub().createCompositeKey(entityTemplate.getType(), new String[]{}).toString();
    Iterator<KeyValue> iterator = ctx.getStub().getStateByPartialCompositeKey(compositeKey).iterator();
    while (iterator.hasNext()) {
      KeyValue result = iterator.next();
      String key = result.getKey();
      byte[] value = result.getValue();
      EntityFactory<Type> factory = entityTemplate.getFactory();
      Type entity = factory.create();
      entities.add(entity.fromBuffer(value));
    }
    return entities;
  }

  @Override
  public void _dispose() {
    this.cache.dispose();
  }

  private boolean _keyExists(String key) {
    byte[] valueOnLedger = this.cache.read(key);
    return valueOnLedger != null && valueOnLedger.length > 0;
  }

  public <Type extends SerializableEntityInterface> boolean exists(Type obj) {
    return _keyExists(_getKey(ctx, obj));
  }

  public <Type extends SerializableEntityInterface> void assertNotExists(Type obj) {
    if (exists(obj)) {
      throw new Error("Entity with key \"" + _getKey(ctx, obj) + "\" already exists on the ledger");
    }
  }

  public <Type extends SerializableEntityInterface> void assertExists(Type obj) {
    if (!exists(obj)) {
      throw new Error("Entity with key \"" + _getKey(ctx, obj) + "\" does not exist on the ledger");
    }
  }
  
}
