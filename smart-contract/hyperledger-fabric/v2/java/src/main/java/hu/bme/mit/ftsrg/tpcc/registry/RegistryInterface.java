package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.EntityInterface;

public interface RegistryInterface<AssetType> {

  void createEntity(EntityInterface entity);

  AssetType getEntity(String keyParts);

  void updateEntity(EntityInterface entity);

  void deleteEntity(String type, String[] keyParts);
}
