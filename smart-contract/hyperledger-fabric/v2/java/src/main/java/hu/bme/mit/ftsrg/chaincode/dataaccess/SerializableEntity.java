/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.dataaccess;

public interface SerializableEntity<Type extends SerializableEntity<Type>> {

  String getType();

  String[] getKeyParts();

  byte[] toBuffer();

  void fromBuffer(byte[] buffer);

  String toJson();

  void fromJson(String json);

  EntityFactory<Type> getFactory();
}
