package hu.bme.mit.ftsrg.tpcc.entities;

public interface EntityFactory<T extends SerializableEntityInterface> {
  T create();
}
