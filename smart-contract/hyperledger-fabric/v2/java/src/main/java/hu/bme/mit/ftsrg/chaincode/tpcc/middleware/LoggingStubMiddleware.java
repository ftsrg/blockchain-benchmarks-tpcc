package hu.bme.mit.ftsrg.chaincode.tpcc.middleware;

import hu.bme.mit.ftsrg.chaincode.dataaccess.ChaincodeStubMiddlewareBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class LoggingStubMiddleware extends ChaincodeStubMiddlewareBase {

  private static final Logger logger = LoggerFactory.getLogger(LoggingStubMiddleware.class);
  private final Level logLevel;

  public LoggingStubMiddleware(final ChaincodeStub next) {
    this(next, Level.INFO);
  }

  public LoggingStubMiddleware(final ChaincodeStub next, final Level logLevel) {
    super(next);
    this.logLevel = logLevel;
  }

  @Override
  public byte[] getState(final String key) {
    logger.atLevel(this.logLevel).log("Getting state for key '%s'".formatted(key));
    final byte[] value = this.nextLayer.getState(key);
    logger.atLevel(this.logLevel).log("Got state for key '%s'; value = '%s'".formatted(key, value));
    return value;
  }

  @Override
  public void putState(final String key, final byte[] value) {
    logger
        .atLevel(this.logLevel)
        .log("Setting state for key '%s' to have value '%s'".formatted(key, value));
    this.nextLayer.putState(key, value);
    logger.atLevel(this.logLevel).log("Done setting state for key '%s'".formatted(key));
  }

  @Override
  public void delState(final String key) {
    logger.atLevel(this.logLevel).log("Deleting state for key '%s'".formatted(key));
    this.nextLayer.delState(key);
    logger.atLevel(this.logLevel).log("Done deleting state for key '%s'".formatted(key));
  }
}
