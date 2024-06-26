/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jcabi.aspects.Loggable;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.*;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.input.*;
import hu.bme.mit.ftsrg.chaincode.tpcc.middleware.TPCCContext;
import hu.bme.mit.ftsrg.chaincode.tpcc.util.JSON;
import hu.bme.mit.ftsrg.hypernate.entity.EntityExistsException;
import hu.bme.mit.ftsrg.hypernate.entity.EntityNotFoundException;
import hu.bme.mit.ftsrg.hypernate.entity.SerializationException;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

@Contract(
    name = "TPCC",
    info =
        @Info(
            title = "tpcc contract",
            description = "My Smart Contract",
            version = "0.0.1",
            license = @License(name = "Apache-2.0"),
            contact =
                @Contact(email = "tnnopcc@example.com", name = "tpcc", url = "http://tpcc.me")))

/**
 * Fabric chaincode interface to the TPC-C benchmark implementation. Only contains delegations to
 * the business logic implemented in {@link TPCCBusinessAPI}.
 */
@Default
@Loggable(Loggable.DEBUG)
public final class TPCCContractAPI implements ContractInterface {

  private final TPCCBusinessAPI api = new TPCCBusinessAPI();

  @Override
  public Context createContext(final ChaincodeStub stub) {
    return new TPCCContext(stub);
  }

  /**
   * Performs the Delivery read-write TX profile [TPC-C 2.7].
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   * @throws EntityNotFoundException if a required entity is not found
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String delivery(final TPCCContext ctx, final String parameters)
      throws EntityNotFoundException, SerializationException, JsonProcessingException {
    return JSON.serialize(api.delivery(ctx, JSON.deserialize(parameters, DeliveryInput.class)));
  }

  /**
   * Performs the New-Order read-write TX profile [TPC-C 2.4].
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   * @throws EntityNotFoundException if a required entity is not found
   * @throws EntityExistsException if an entity that should be created already exists
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String newOrder(final TPCCContext ctx, final String parameters)
      throws EntityNotFoundException, EntityExistsException, SerializationException, JsonProcessingException {
    return JSON.serialize(api.newOrder(ctx, JSON.deserialize(parameters, NewOrderInput.class)));
  }

  /**
   * Performs the Order-Status read TX profile [TPC-C 2.6].
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String orderStatus(final TPCCContext ctx, final String parameters)
      throws NotFoundException, EntityNotFoundException, SerializationException, JsonProcessingException {
    return JSON.serialize(api.orderStatus(ctx, JSON.deserialize(parameters, OrderStatusInput.class)));
  }

  /**
   * Performs the Payment read-write TX profile [TPC-C 2.5].
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   * @throws EntityNotFoundException if a required entity is not found
   * @throws EntityExistsException if an entity that should be created already exists
   * @throws NotFoundException if some entities are not found in the business logic
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String payment(final TPCCContext ctx, final String parameters)
      throws EntityNotFoundException, EntityExistsException, NotFoundException, SerializationException, JsonProcessingException {
    return JSON.serialize(api.payment(ctx, JSON.deserialize(parameters, PaymentInput.class)));
  }

  /**
   * Performs the Stock-Level read TX profile [TPC-C 2.8].
   *
   * @param ctx The TX context.
   * @param parameters The JSON encoded parameters of the TX profile.
   * @return The JSON encoded query results according to the specification.
   * @throws EntityNotFoundException if a required entity is not found
   * @throws NotFoundException if some entities are not found in the business logic
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String stockLevel(final TPCCContext ctx, final String parameters)
      throws EntityNotFoundException, NotFoundException, SerializationException, JsonProcessingException {
    return JSON.serialize(api.stockLevel(ctx, JSON.deserialize(parameters, StockLevelInput.class)));
  }

  /**
   * Creates some dummy initial entities for testing.
   *
   * @param ctx The transaction context
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void init(final TPCCContext ctx) throws EntityExistsException, SerializationException {
    api.init(ctx);
  }

  /**
   * Returns a warehouse entity (for debugging).
   *
   * @param ctx The transaction context
   * @param w_id The W_ID of the warehouse
   * @return The warehouse with matching W_ID
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String readWarehouse(final TPCCContext ctx, final int w_id)
      throws EntityNotFoundException, SerializationException, JsonProcessingException {
    final Warehouse warehouse = Warehouse.builder().id(w_id).build();
    ctx.getRegistry().read(warehouse);
    ctx.commit();
    return JSON.serialize(warehouse);
  }

  /**
   * Returns an order entity (for debugging).
   *
   * @param ctx The transaction context
   * @param w_id The W_ID of the order
   * @param d_id The D_ID of the order
   * @param o_id The O_ID of the order
   * @return The order with matching (W_ID, D_ID, O_ID)
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String readOrder(final TPCCContext ctx, final int w_id, final int d_id, final int o_id)
      throws EntityNotFoundException, SerializationException, JsonProcessingException {
    final Order order = Order.builder().w_id(w_id).d_id(d_id).id(o_id).build();
    ctx.getRegistry().read(order);
    ctx.commit();
    return JSON.serialize(order);
  }

  /**
   * Returns an item entity (for debugging).
   *
   * @param ctx The transaction context
   * @param i_id The I_ID of the item
   * @return The item with matching I_ID
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String readItem(final TPCCContext ctx, final int i_id) throws EntityNotFoundException, SerializationException, JsonProcessingException {
    final Item item = Item.builder().id(i_id).build();
    ctx.getRegistry().read(item);
    ctx.commit();
    return JSON.serialize(item);
  }

  /**
   * Returns a new-order entity (for debugging).
   *
   * @param ctx The transaction context
   * @param w_id The W_ID of the new-order
   * @param d_id The D_ID of the new-order
   * @param o_id The O_ID of the new-order
   * @return The new-order with matching (W_ID, D_ID, O_ID)
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String readNewOrder(final TPCCContext ctx, final int w_id, final int d_id, final int o_id)
      throws EntityNotFoundException, SerializationException, JsonProcessingException {
    final NewOrder newOrder = NewOrder.builder().w_id(w_id).d_id(d_id).o_id(o_id).build();
    ctx.getRegistry().read(newOrder);
    ctx.commit();
    return JSON.serialize(newOrder);
  }

  /**
   * Should always return 'pong' (for diagnostics).
   *
   * @param ctx The transaction context (unused)
   * @return 'pong'
   */
  @SuppressWarnings("SameReturnValue")
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String ping(final TPCCContext ctx) {
    ctx.commit();
    return "pong";
  }

  /**
   * Dummy OpenJML test.
   *
   * <p>Should only allow getting the details of customer #1, but not customer #2
   *
   * <p><b>WARNING:</b> may only be called only after {@link TPCCContractAPI#init}
   *
   * @param ctx The transaction context
   * @param c_w_id The C_W_ID of the customer
   * @param c_d_id The C_D_ID of the customer
   * @param c_id The C_ID of the customer
   * @return The customer with matching (C_W_ID, C_D_ID, C_ID), unless C_ID >= 2, in which case an
   *     exception should be thrown by OpenJML
   */
  // spotless:off
  //@ requires c_id < 2;
  // spotless:on
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String OJMLTEST__getCustomer(
      final TPCCContext ctx, final int c_w_id, final int c_d_id, final int c_id) throws JsonProcessingException {
    final Customer customer = Customer.builder().w_id(c_w_id).d_id(c_d_id).id(c_id).build();
    ctx.commit();
    return JSON.serialize(customer);
  }
}
