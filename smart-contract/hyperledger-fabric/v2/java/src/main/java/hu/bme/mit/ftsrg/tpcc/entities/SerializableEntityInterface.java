package hu.bme.mit.ftsrg.tpcc.entities;

public interface SerializableEntityInterface<Type extends SerializableEntityInterface>{
  String getType();

  String[] getKeyParts();

  byte[] toBuffer();

  void fromBuffer(byte[] buffer);

  String toJson();

  void fromJson(String json);

  EntityFactory<Type> getFactory();
}
