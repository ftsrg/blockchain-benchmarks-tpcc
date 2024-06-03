package hu.bme.mit.ftsrg.chaincode.tpcc.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.Warehouse;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.input.*;
import hu.bme.mit.ftsrg.chaincode.tpcc.middleware.TPCCContext;
import hu.bme.mit.ftsrg.chaincode.tpcc.test.mock.InMemoryMockChaincodeStub;
import hu.bme.mit.ftsrg.hypernate.entity.EntityExistsException;
import hu.bme.mit.ftsrg.hypernate.entity.EntityNotFoundException;
import hu.bme.mit.ftsrg.hypernate.entity.SerializationException;
import org.jmlspecs.runtime.JmlAssertionError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class TPCCBusinessApiTest {

  private final TPCCBusinessAPI api = new TPCCBusinessAPI();

  private TPCCContext ctx;

  @BeforeEach
  void setup() {
    ctx = new TPCCContext(new InMemoryMockChaincodeStub());
  }

  @Test
  void test_ojml_sanity() {
    final Warehouse warehouse = new Warehouse();
    assertThrows(JmlAssertionError.class, new Executable() {
      @Override
      public void execute() throws Throwable {
        warehouse.setW_state("INVALID_STATE_STRING");
      }
    });
  }

  @Test
  void test_invoke_all_transaction_profiles() throws SerializationException, EntityExistsException, EntityNotFoundException, NotFoundException, JsonProcessingException {
    api.init(ctx);
    api.newOrder(ctx, new NewOrderInput(1, 1, 1, "20110101T081500Z", new int[]{1, 2}, new int[]{1, 1}, new int[]{1, 1}));
    api.payment(ctx, new PaymentInput(1, 1, 100, 1, 1, 1, "YONG", "20240525T12:00:00Z"));
    api.delivery(ctx, new DeliveryInput(1, 1, "2024-05-25"));
    api.orderStatus(ctx, new OrderStatusInput(1, 1, 1, "YONG"));
    api.stockLevel(ctx, new StockLevelInput(1, 1, 5));
  }
}
