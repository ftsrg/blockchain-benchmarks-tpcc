package hu.bme.mit.ftsrg.chaincode.dataaccess;

public interface EntityFactory<Type extends SerializableEntity<Type>> {
  Type create();
}
