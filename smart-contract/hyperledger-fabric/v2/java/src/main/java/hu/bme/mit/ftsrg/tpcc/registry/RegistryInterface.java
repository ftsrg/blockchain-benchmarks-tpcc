package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.EntityInterface;

public interface RegistryInterface {

  void _create(EntityInterface entity, boolean strict);

  EntityInterface _readAndParse(EntityInterface entity);

  void _update(EntityInterface entity, boolean strict);

  void _delete(EntityInterface entity, boolean strict);

  void _dispose();
}
