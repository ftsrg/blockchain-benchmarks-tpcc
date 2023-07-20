/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entities;

import hu.bme.mit.ftsrg.tpcc.utils.JSON;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;

@EqualsAndHashCode
@DataType
public abstract class SerializableEntityBase<Type extends SerializableEntityInterface<Type>>
    implements SerializableEntityInterface<Type> {

  @Override
  public String getType() {
    return this.getClass().getName().toUpperCase();
  }

  @Override
  public byte[] toBuffer() {
    return this.toJson().getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public void fromBuffer(final byte[] buffer) {
    this.fromJson(new String(buffer, StandardCharsets.UTF_8));
  }

  @Override
  public String toJson() {
    return JSON.serialize(this);
  }

  @Override
  public void fromJson(final String json) {
    final Object obj = JSON.deserialize(json, this.getClass());
    final Field[] ourFields = this.getClass().getDeclaredFields();
    /*
     * Try to get values for our known fields from the deserialized
     * object.  This process is forgiving: if one of our fields does not
     * exist inside the deserialized object, we leave it as is; if the
     * deserialized object contains fields we do not recognize, we
     * silently ignore them.
     */
    for (final Field ourField : ourFields) {
      try {
        final Field theirField = obj.getClass().getField(ourField.getName());
        try {
          if (ourField.get(this) == null) ourField.set(this, theirField.get(obj));
        } catch (IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace();
        }
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      }
    }
  }
}
