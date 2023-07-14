/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entities;

import hu.bme.mit.ftsrg.tpcc.utils.JSON;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.hyperledger.fabric.contract.annotation.DataType;

@DataType
public class EntityBase implements EntityInterface {

  public EntityBase() {}

  @Override
  public String getType() {
    throw new UnsupportedOperationException("Unimplemented method 'getType'");
  }

  @Override
  public String[] getKeyParts() {

    throw new UnsupportedOperationException("Unimplemented method 'getKeyParts'");
  }

  @Override
  public byte[] toBuffer() {
    // return StandardCharsets.UTF_8.encode(JSON.serialize(this)).array();
    // return JSON.serialize(this).getBytes(StandardCharsets.UTF_8);

    String entityToJson = JSON.serialize(this);
    return entityToJson.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public void fromBuffer(byte[] buffer) {
    this.fromJson(new String(buffer, StandardCharsets.UTF_8));
  }

  @Override
  public String toJson() {
    return JSON.serialize(this);
  }

  @Override
  public void fromJson(final String json) {
    Object obj = JSON.deserialize(json, this.getClass());
    Field[] ourFields = this.getClass().getDeclaredFields();
    /*
     * Try to get values for our known fields from the deserialized
     * object.  This process is forgiving: if one of our fields does not
     * exist inside the deserialized object, we leave it as is; if the
     * deserialized object contains fields we do not recognize, we
     * silently ignore them.
     */
    for (Field ourField : ourFields) {
      try {
        Field theirField = obj.getClass().getField(ourField.getName());
        try {
          if (ourField.get(this) == null) ourField.set(this, theirField.get(obj));
        } catch (IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace();
        }
      } catch (NoSuchFieldException e) {
        /* ignore or: */
        e.printStackTrace();
      }
    }
  }
}