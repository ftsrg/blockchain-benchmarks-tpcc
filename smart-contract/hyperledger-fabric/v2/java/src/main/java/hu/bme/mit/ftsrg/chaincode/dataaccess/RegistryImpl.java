package hu.bme.mit.ftsrg.chaincode.dataaccess;

import com.jcabi.aspects.Loggable;
import java.util.*;
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
  public <Type extends SerializableEntity<Type>> Type read(final Context ctx, final Type target) {
    final String key = getKey(ctx, target);
    final byte[] data = ctx.getStub().getState(key);

    if (data == null || data.length == 0)
      throw new Error(
          "Entity with key \"" + key + "\" does not exist on the ledger, thus cannot parse it");

    target.fromBuffer(data);
    return target;
  }

  @Override
  public <Type extends SerializableEntity<Type>> List<Type> readAll(
      final Context ctx, final Type template) {
    final List<Type> entities = new ArrayList<>();
    final String compositeKey = ctx.getStub().createCompositeKey(template.getType()).toString();
    for (KeyValue keyValue : ctx.getStub().getStateByPartialCompositeKey(compositeKey)) {
      final byte[] value = keyValue.getValue();
      final EntityFactory<Type> factory = template.getFactory();
      final Type entity = factory.create();
      entity.fromBuffer(value);
      entities.add(entity);
    }
    return entities;
  }

  @Override
  public <Type extends SerializableEntity<Type>> SelectionBuilder<Type> select(
      final Context ctx, final Type template) {
    return new SelectionBuilderImpl<>(this.readAll(ctx, template));
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

  private final class SelectionBuilderImpl<Type extends SerializableEntity<Type>>
      implements SelectionBuilder<Type> {

    private List<Type> selection;

    SelectionBuilderImpl(final List<Type> entities) {
      this.selection = entities;
    }

    @Override
    public SelectionBuilder<Type> matching(final Matcher<Type> matcher) {
      final List<Type> newSelection = new ArrayList<>();
      for (Type entity : this.selection) if (matcher.match(entity)) newSelection.add(entity);
      this.selection = newSelection;
      return this;
    }

    @Override
    public SelectionBuilder<Type> sortedBy(final Comparator<Type> comparator) {
      this.selection.sort(comparator);
      return this;
    }

    @Override
    public SelectionBuilder<Type> descending() {
      Collections.reverse(this.selection);
      return this;
    }

    @Override
    public List<Type> get() {
      return this.selection;
    }

    @Override
    public Type getFirst() {
      if (this.selection.isEmpty()) return null;
      return this.selection.get(0);
    }
  }
}
