package hu.bme.mit.ftsrg.chaincode.tpcc.middleware;

import hu.bme.mit.ftsrg.chaincode.dataaccess.ContextWithRegistry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;
import org.hyperledger.fabric.shim.ChaincodeStub;

public class TPCCContext extends ContextWithRegistry {

  private final Deque<ChaincodeStub> stubMiddlewares = new ArrayDeque<>();

  public TPCCContext(final ChaincodeStub fabricStub) {
    super(fabricStub);

    /*
     * Stub chain:
     *   --> LOGGER --> WRITE BACK CACHE --> FABRIC STUB    ( --> ledger )
     */
    this.stubMiddlewares.push(fabricStub);
    this.prependMiddleware(WriteBackCachedChaincodeStubMiddleware::new);
    this.prependMiddleware(LoggingStubMiddleware::new);
  }

  private void prependMiddleware(final Function<ChaincodeStub, ChaincodeStub> constructor) {
    this.stubMiddlewares.push(constructor.apply(this.stubMiddlewares.peek()));
  }

  @Override
  public ChaincodeStub getStub() {
    return this.stubMiddlewares.peek();
  }
}
