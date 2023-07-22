package hu.bme.mit.ftsrg.chaincode.dataaccess;

import com.jcabi.aspects.Loggable;
import hu.bme.mit.ftsrg.chaincode.MethodLogger;
import hu.bme.mit.ftsrg.chaincode.dataaccess.exception.EntityExistsException;
import hu.bme.mit.ftsrg.chaincode.dataaccess.exception.EntityNotFoundException;
import java.util.*;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Loggable(Loggable.DEBUG) // FIXME how to configure AspectJ with OpenJML and Gradle?
public class RegistryImpl implements Registry {

  private static final Logger logger = LoggerFactory.getLogger(RegistryImpl.class);

  private static final MethodLogger methodLogger = new MethodLogger(logger, "RegistryImpl");

  public RegistryImpl() {}

  private <Type extends SerializableEntity<Type>> String getKey(final Context ctx, final Type obj) {
    final String paramsString = methodLogger.generateParamsString(ctx, obj);
    methodLogger.logStart("getKey", paramsString);

    final CompositeKey compositeKey =
        ctx.getStub().createCompositeKey(obj.getType(), obj.getKeyParts());

    methodLogger.logEnd("getKey", paramsString, compositeKey.toString());
    return compositeKey.toString();
  }

  @Override
  public <Type extends SerializableEntity<Type>> void create(final Context ctx, final Type entity)
      throws EntityExistsException {
    final String paramsString = methodLogger.generateParamsString(ctx, entity);
    methodLogger.logStart("create", paramsString);

    assertNotExists(ctx, entity);

    final String key = getKey(ctx, entity);
    final byte[] buffer = entity.toBuffer();
    ctx.getStub().putState(key, buffer);

    methodLogger.logEnd("create", paramsString, "<void>");
  }

  @Override
  public <Type extends SerializableEntity<Type>> void update(final Context ctx, final Type entity)
      throws EntityNotFoundException {
    final String paramsString = methodLogger.generateParamsString(ctx, entity);
    methodLogger.logStart("update", paramsString);

    assertExists(ctx, entity);

    final String key = getKey(ctx, entity);
    final byte[] buffer = entity.toBuffer();
    ctx.getStub().putState(key, buffer);

    methodLogger.logEnd("update", paramsString, "<void>");
  }

  @Override
  public <Type extends SerializableEntity<Type>> void delete(final Context ctx, final Type entity)
      throws EntityNotFoundException {
    final String paramsString = methodLogger.generateParamsString(ctx, entity);
    methodLogger.logStart("delete", paramsString);

    assertExists(ctx, entity);

    final String key = getKey(ctx, entity);
    ctx.getStub().delState(key);

    methodLogger.logEnd("delete", paramsString, "<void>");
  }

  @Override
  public <Type extends SerializableEntity<Type>> Type read(final Context ctx, final Type target)
      throws EntityNotFoundException {
    final String paramsString = methodLogger.generateParamsString(ctx, target);
    methodLogger.logStart("read", paramsString);

    final String key = getKey(ctx, target);
    final byte[] data = ctx.getStub().getState(key);

    if (data == null || data.length == 0) throw new EntityNotFoundException(key);

    target.fromBuffer(data);

    methodLogger.logEnd("read", paramsString, target.toJson());
    return target;
  }

  @Override
  public <Type extends SerializableEntity<Type>> List<Type> readAll(
      final Context ctx, final Type template) {
    final String paramsString = methodLogger.generateParamsString(ctx, template);
    methodLogger.logStart("readAll", paramsString);

    final List<Type> entities = new ArrayList<>();
    final String compositeKey = ctx.getStub().createCompositeKey(template.getType()).toString();
    for (KeyValue keyValue : ctx.getStub().getStateByPartialCompositeKey(compositeKey)) {
      final byte[] value = keyValue.getValue();
      final EntityFactory<Type> factory = template.getFactory();
      final Type entity = factory.create();
      entity.fromBuffer(value);
      entities.add(entity);
    }

    methodLogger.logEnd("readAll", paramsString, entities.toString());
    return entities;
  }

  @Override
  public <Type extends SerializableEntity<Type>> SelectionBuilder<Type> select(
      final Context ctx, final Type template) {
    final String paramsString = methodLogger.generateParamsString(ctx, template);
    methodLogger.logStart("select", paramsString);

    final SelectionBuilder<Type> builder = new SelectionBuilderImpl<>(this.readAll(ctx, template));
    methodLogger.logEnd("select", paramsString, builder.toString());
    return builder;
  }

  private boolean keyExists(final Context ctx, final String key) {
    final byte[] valueOnLedger = ctx.getStub().getState(key);
    return valueOnLedger != null && valueOnLedger.length > 0;
  }

  public <Type extends SerializableEntity<Type>> boolean exists(final Context ctx, final Type obj) {
    return keyExists(ctx, getKey(ctx, obj));
  }

  public <Type extends SerializableEntity<Type>> void assertNotExists(
      final Context ctx, final Type obj) throws EntityExistsException {
    if (exists(ctx, obj)) throw new EntityExistsException(getKey(ctx, obj));
  }

  public <Type extends SerializableEntity<Type>> void assertExists(
      final Context ctx, final Type obj) throws EntityNotFoundException {
    if (!exists(ctx, obj)) throw new EntityNotFoundException(getKey(ctx, obj));
  }

  private static final class SelectionBuilderImpl<Type extends SerializableEntity<Type>>
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
