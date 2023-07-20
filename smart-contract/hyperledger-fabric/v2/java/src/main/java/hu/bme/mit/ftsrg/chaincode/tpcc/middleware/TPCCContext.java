package hu.bme.mit.ftsrg.chaincode.tpcc.middleware;

import hu.bme.mit.ftsrg.chaincode.dataaccess.ContextWithRegistry;
import java.util.ArrayDeque;
import java.util.Deque;
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
    this.stubMiddlewares.push(
        new WriteBackCachedChaincodeStubMiddleware(this.stubMiddlewares.peek()));
    this.stubMiddlewares.push(new LoggingStubMiddleware(this.stubMiddlewares.peek()));
  }

  @Override
  public ChaincodeStub getStub() {
    return this.stubMiddlewares.peek();
  }
}
