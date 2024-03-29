package hu.bme.mit.ftsrg.chaincode.dataaccess;

/**
 * Minimal interface to allow entities to return factories that produce them.
 *
 * @param <Type> the entity's type
 */
public interface EntityFactory<Type extends SerializableEntity<Type>> {
  /**
   * Simply gives a new, empty instance of the entity of type <code>Type</code>.
   *
   * @return a new, empty entity instance
   */
  Type create();
}
