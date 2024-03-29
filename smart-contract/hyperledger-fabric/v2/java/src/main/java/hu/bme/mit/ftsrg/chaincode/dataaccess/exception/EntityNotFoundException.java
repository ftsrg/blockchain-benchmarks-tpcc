package hu.bme.mit.ftsrg.chaincode.dataaccess.exception;

public class EntityNotFoundException extends DataAccessException {

  public EntityNotFoundException(final String key) {
    super("Entity with key '%s' could not be found".formatted(key));
  }
}
