package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.EntityInterface;

public interface RegistryInterface {

  void create(EntityInterface entity);

  EntityInterface read(EntityInterface entity);

  void update(EntityInterface entity);

  void delete(EntityInterface entity);
}
