/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entities;

public interface SerializableEntityInterface<Type extends SerializableEntityInterface<Type>> {

  String getType();

  String[] getKeyParts();

  byte[] toBuffer();

  void fromBuffer(byte[] buffer);

  String toJson();

  void fromJson(String json);

  EntityFactory<Type> getFactory();
}
