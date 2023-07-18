package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.EntityFactory;
import hu.bme.mit.ftsrg.tpcc.entities.SerializableEntityInterface;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;

public class EntityRegistry implements RegistryInterface {
  public EntityRegistry(){}

  private <Type extends SerializableEntityInterface<Type>> String getKey(Context ctx, Type obj) {
    CompositeKey compositeKey = ctx.getStub().createCompositeKey(obj.getType(), obj.getKeyParts());
    return compositeKey.toString();
  }

  @Override
  public <Type extends SerializableEntityInterface<Type>> void create(Context ctx, Type entity) {
    assertNotExists(ctx, entity);

    String key = getKey(ctx, entity);
    byte[] buffer = entity.toBuffer();
    ctx.getStub().putState(key, buffer);
  }

  @Override
  public <Type extends SerializableEntityInterface<Type>> void update(Context ctx, Type entity) {

    assertExists(ctx, entity);

    String key = getKey(ctx, entity);
    byte[] buffer = entity.toBuffer();
    ctx.getStub().putState(key, buffer);
  }

  @Override
  public <Type extends SerializableEntityInterface<Type>> void delete(Context ctx, Type entity) {

    assertExists(ctx, entity);

    String key = getKey(ctx, entity);
    ctx.getStub().delState(key);
  }

  @Override
  public <Type extends SerializableEntityInterface<Type>> Type read(Context ctx, Type entity) {

    String key = getKey(ctx, entity);
    byte[] data = ctx.getStub().getState(key);

    if (data == null || data.length == 0) {
      throw new Error(
          "Entity with key \"" + key + "\" does not exist on the ledger, thus cannot parse it");
    }

    entity.fromBuffer(data);
    return entity;
  }

  @Override
  public <Type extends SerializableEntityInterface<Type>> List<Type> readAll(
      Context ctx, Type entityTemplate) {
    List<Type> entities = new ArrayList<>();
    String compositeKey =
        ctx.getStub().createCompositeKey(entityTemplate.getType(), new String[] {}).toString();
    Iterator<KeyValue> iterator =
        ctx.getStub().getStateByPartialCompositeKey(compositeKey).iterator();
    while (iterator.hasNext()) {
      byte[] value = iterator.next().getValue();
      EntityFactory<Type> factory = entityTemplate.getFactory();
      Type entity = factory.create();
      entity.fromBuffer(value);
      entities.add(entity);
    }
    return entities;
  }

  private boolean keyExists(Context ctx, String key) {
    byte[] valueOnLedger = ctx.getStub().getState(key);
    return valueOnLedger != null && valueOnLedger.length > 0;
  }

  public <Type extends SerializableEntityInterface<Type>> boolean exists(Context ctx, Type obj) {
    return keyExists(ctx, getKey(ctx, obj));
  }

  public <Type extends SerializableEntityInterface<Type>> void assertNotExists(
      Context ctx, Type obj) {
    if (exists(ctx, obj)) {
      throw new Error("Entity with key \"" + getKey(ctx, obj) + "\" already exists on the ledger");
    }
  }

  public <Type extends SerializableEntityInterface<Type>> void assertExists(Context ctx, Type obj) {
    if (!exists(ctx, obj)) {
      throw new Error("Entity with key \"" + getKey(ctx, obj) + "\" does not exist on the ledger");
    }
  }
}
