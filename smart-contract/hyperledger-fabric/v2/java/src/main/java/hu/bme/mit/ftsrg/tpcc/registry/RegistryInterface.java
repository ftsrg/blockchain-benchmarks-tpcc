package hu.bme.mit.ftsrg.tpcc.registry;

import java.util.List;

import org.hyperledger.fabric.contract.Context;

import hu.bme.mit.ftsrg.tpcc.entities.SerializableEntityInterface;

public interface RegistryInterface {

  <Type extends SerializableEntityInterface<Type>> void create(Context ctx, Type entity, boolean strict);

  <Type extends SerializableEntityInterface<Type>> void update(Context ctx, Type entity, boolean strict);

  <Type extends SerializableEntityInterface<Type>> void delete(Context ctx, Type entity, boolean strict);

  <Type extends SerializableEntityInterface<Type>> Type read(Context ctx, Type entity);
  
  <Type extends SerializableEntityInterface<Type>> List<Type> readAll(Context ctx, Type entity);

  //<Type extends SerializableEntityInterface> void dispose();
}
