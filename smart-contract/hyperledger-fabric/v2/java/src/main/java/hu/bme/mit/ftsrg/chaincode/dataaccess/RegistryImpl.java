package hu.bme.mit.ftsrg.chaincode.dataaccess;

import com.jcabi.aspects.Loggable;
import java.util.ArrayList;
import java.util.List;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;

@Loggable(Loggable.DEBUG)
public class RegistryImpl implements Registry {
  public RegistryImpl() {}

  private <Type extends SerializableEntity<Type>> String getKey(final Context ctx, final Type obj) {
    final CompositeKey compositeKey =
        ctx.getStub().createCompositeKey(obj.getType(), obj.getKeyParts());
    return compositeKey.toString();
  }

  @Override
  public <Type extends SerializableEntity<Type>> void create(final Context ctx, final Type entity) {
    assertNotExists(ctx, entity);

    final String key = getKey(ctx, entity);
    final byte[] buffer = entity.toBuffer();
    ctx.getStub().putState(key, buffer);
  }

  @Override
  public <Type extends SerializableEntity<Type>> void update(final Context ctx, final Type entity) {
    assertExists(ctx, entity);

    final String key = getKey(ctx, entity);
    final byte[] buffer = entity.toBuffer();
    ctx.getStub().putState(key, buffer);
  }

  @Override
  public <Type extends SerializableEntity<Type>> void delete(final Context ctx, final Type entity) {
    assertExists(ctx, entity);

    final String key = getKey(ctx, entity);
    ctx.getStub().delState(key);
  }

  @Override
  public <Type extends SerializableEntity<Type>> Type read(final Context ctx, final Type entity) {
    final String key = getKey(ctx, entity);
    final byte[] data = ctx.getStub().getState(key);

    if (data == null || data.length == 0)
      throw new Error(
          "Entity with key \"" + key + "\" does not exist on the ledger, thus cannot parse it");

    entity.fromBuffer(data);
    return entity;
  }

  @Override
  public <Type extends SerializableEntity<Type>> List<Type> readAll(
      final Context ctx, final Type entityTemplate) {
    final List<Type> entities = new ArrayList<>();
    final String compositeKey =
        ctx.getStub().createCompositeKey(entityTemplate.getType()).toString();
    for (KeyValue keyValue : ctx.getStub().getStateByPartialCompositeKey(compositeKey)) {
      final byte[] value = keyValue.getValue();
      final EntityFactory<Type> factory = entityTemplate.getFactory();
      final Type entity = factory.create();
      entity.fromBuffer(value);
      entities.add(entity);
    }
    return entities;
  }

  private boolean keyExists(final Context ctx, final String key) {
    final byte[] valueOnLedger = ctx.getStub().getState(key);
    return valueOnLedger != null && valueOnLedger.length > 0;
  }

  public <Type extends SerializableEntity<Type>> boolean exists(final Context ctx, final Type obj) {
    return keyExists(ctx, getKey(ctx, obj));
  }

  public <Type extends SerializableEntity<Type>> void assertNotExists(
      final Context ctx, final Type obj) {
    if (exists(ctx, obj))
      throw new Error("Entity with key \"" + getKey(ctx, obj) + "\" already exists on the ledger");
  }

  public <Type extends SerializableEntity<Type>> void assertExists(
      final Context ctx, final Type obj) {
    if (!exists(ctx, obj))
      throw new Error("Entity with key \"" + getKey(ctx, obj) + "\" does not exist on the ledger");
  }
}
