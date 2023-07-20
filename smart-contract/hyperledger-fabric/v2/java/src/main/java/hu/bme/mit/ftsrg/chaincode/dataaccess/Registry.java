package hu.bme.mit.ftsrg.chaincode.dataaccess;

import java.util.List;
import org.hyperledger.fabric.contract.Context;

public interface Registry {

  <Type extends SerializableEntity<Type>> void create(Context ctx, Type entity);

  <Type extends SerializableEntity<Type>> void update(Context ctx, Type entity);

  <Type extends SerializableEntity<Type>> void delete(Context ctx, Type entity);

  <Type extends SerializableEntity<Type>> Type read(Context ctx, Type entity);

  <Type extends SerializableEntity<Type>> List<Type> readAll(Context ctx, Type entity);
}
