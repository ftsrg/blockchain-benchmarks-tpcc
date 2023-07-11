package hu.bme.mit.ftsrg.tpcc.entities;

//public interface EntityInterface<AssetType> {
public interface EntityInterface {
    String getType();
    String[] getKeyParts();
    byte[] toBuffer();
    void fromBuffer(byte[] buffer);
    String toJson();
    void fromJson(String json);
    
}