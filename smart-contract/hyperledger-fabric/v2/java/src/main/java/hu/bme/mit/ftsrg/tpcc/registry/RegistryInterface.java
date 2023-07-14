package hu.bme.mit.ftsrg.tpcc.registry;

import hu.bme.mit.ftsrg.tpcc.entities.EntityInterface;
import hu.bme.mit.ftsrg.tpcc.stub.EnhancedContext;

public interface RegistryInterface {

  void create(EnhancedContext ctx, EntityInterface entity);

  EntityInterface read(EnhancedContext ctx, EntityInterface entity);

  void update(EnhancedContext ctx, EntityInterface entity);

  void delete(EnhancedContext ctx, EntityInterface entity);
}
