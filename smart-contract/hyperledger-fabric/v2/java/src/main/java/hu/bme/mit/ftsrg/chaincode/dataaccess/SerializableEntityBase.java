/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.dataaccess;

import hu.bme.mit.ftsrg.chaincode.tpcc.util.JSON;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;

@EqualsAndHashCode
@DataType
public abstract class SerializableEntityBase<Type extends SerializableEntity<Type>>
    implements SerializableEntity<Type> {

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

  @Override
  public String[] getKeyParts() {
    // Stream-based implementation replaced with code below to accommodate OpenJML...
    final List<String> keyParts = new ArrayList<>();
    for (final Field field : this.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(KeyPart.class))
        try {
          keyParts.add(pad(field.getInt(this)));
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
    }

    return keyParts.toArray(new String[0]);
  }

  @Override
  public EntityFactory<Type> getFactory() {
    final Class<? extends SerializableEntityBase> ourClass = this.getClass();
    // Lambda-based implementation replaced with code below to accommodate OpenJML...
    return new EntityFactory<Type>() {
      @Override
      public Type create() {
        try {
          return (Type) ourClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException
            | IllegalAccessException
            | InvocationTargetException
            | NoSuchMethodException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private static final int padLength = Integer.toString(Integer.MAX_VALUE).length();

  /**
   * Converts the number to text and pads it to a fix length.
   *
   * @param num The number to pad.
   * @return The padded number text.
   */
  private static String pad(final int num) {
    return String.format("%0" + padLength + "d", num);
  }
}
