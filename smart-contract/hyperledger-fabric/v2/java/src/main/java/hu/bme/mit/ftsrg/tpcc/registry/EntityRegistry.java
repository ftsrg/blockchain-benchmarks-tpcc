package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.EntityInterface;
import org.hyperledger.fabric.contract.Context;

public class EntityRegistry<AssetType> implements RegistryInterface<AssetType> {
  Context context;

  EntityRegistry(Context ctx) {
    this.context = ctx;
  }

  @Override
  public void createEntity(EntityInterface entity) {

    throw new UnsupportedOperationException("Unimplemented method 'createEntity'");
  }

  @Override
  public AssetType getEntity(String keyParts) {

    throw new UnsupportedOperationException("Unimplemented method 'getEntity'");
  }

  @Override
  public void updateEntity(EntityInterface entity) {

    throw new UnsupportedOperationException("Unimplemented method 'updateEntity'");
  }

  @Override
  public void deleteEntity(String type, String[] keyParts) {

    throw new UnsupportedOperationException("Unimplemented method 'deleteEntity'");
  }
}
