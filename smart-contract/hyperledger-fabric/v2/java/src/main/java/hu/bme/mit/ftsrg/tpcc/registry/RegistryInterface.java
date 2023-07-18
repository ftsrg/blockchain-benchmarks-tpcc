package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.SerializableEntityInterface;
import java.util.List;
import org.hyperledger.fabric.contract.Context;

public interface RegistryInterface {

  <Type extends SerializableEntityInterface<Type>> void create(Context ctx, Type entity);

  <Type extends SerializableEntityInterface<Type>> void update(Context ctx, Type entity);

  <Type extends SerializableEntityInterface<Type>> void delete(Context ctx, Type entity);

  <Type extends SerializableEntityInterface<Type>> Type read(Context ctx, Type entity);

  <Type extends SerializableEntityInterface<Type>> List<Type> readAll(Context ctx, Type entity);

  // <Type extends SerializableEntityInterface> void dispose();
}
