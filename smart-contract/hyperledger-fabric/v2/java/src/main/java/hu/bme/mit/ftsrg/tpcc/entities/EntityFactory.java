package hu.bme.mit.ftsrg.tpcc.entities;

public interface EntityFactory<Type extends SerializableEntityInterface> {
  Type create();
}
