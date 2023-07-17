package hu.bme.mit.ftsrg.tpcc.registry;

import java.util.List;

import org.hyperledger.fabric.contract.Context;

import hu.bme.mit.ftsrg.tpcc.entities.SerializableEntityInterface;

public interface RegistryInterface {

  <Type extends SerializableEntityInterface> void _create(Context ctx, Type entity, boolean strict);

  <Type extends SerializableEntityInterface> void _update(Context ctx, Type entity, boolean strict);

  <Type extends SerializableEntityInterface> void _delete(Context ctx, Type entity, boolean strict);

  <Type extends SerializableEntityInterface> Type _readAndParse(Context ctx, Type entity);
  
  <Type extends SerializableEntityInterface> List<Type> _readAll(Context ctx, Type entity);

  <Type extends SerializableEntityInterface> void _dispose();
}
