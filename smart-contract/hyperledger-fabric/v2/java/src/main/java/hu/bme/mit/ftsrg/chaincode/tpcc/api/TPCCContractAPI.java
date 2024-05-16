/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jcabi.aspects.Loggable;
import hu.bme.mit.ftsrg.chaincode.MethodLogger;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.*;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.input.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Loggable(Loggable.DEBUG) // FIXME how to configure AspectJ with OpenJML and Gradle?
public final class TPCCContractAPI implements ContractInterface {

  private static final Logger logger = LoggerFactory.getLogger(TPCCContractAPI.class);

  private static final MethodLogger methodLogger = new MethodLogger(logger, "TPCCContractAPI");

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
    final String paramString = methodLogger.generateParamsString(ctx, parameters);
    methodLogger.logStart("delivery", paramString);
    final String json =
        JSON.serialize(
            TPCCBusinessAPI.delivery(ctx, JSON.deserialize(parameters, DeliveryInput.class)));
    methodLogger.logEnd("delivery", paramString, json);
    return json;
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
    final String paramString = methodLogger.generateParamsString(ctx, parameters);
    methodLogger.logStart("newOrder", paramString);
    final String json =
        JSON.serialize(
            TPCCBusinessAPI.newOrder(ctx, JSON.deserialize(parameters, NewOrderInput.class)));
    methodLogger.logEnd("newOrder", paramString, json);
    return json;
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
    final String paramString = methodLogger.generateParamsString(ctx, parameters);
    methodLogger.logStart("orderStatus", paramString);
    final String json =
        JSON.serialize(
            TPCCBusinessAPI.orderStatus(ctx, JSON.deserialize(parameters, OrderStatusInput.class)));
    methodLogger.logEnd("orderStatus", paramString, json);
    return json;
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
    final String paramString = methodLogger.generateParamsString(ctx, parameters);
    methodLogger.logStart("payment", paramString);
    final String json =
        JSON.serialize(
            TPCCBusinessAPI.payment(ctx, JSON.deserialize(parameters, PaymentInput.class)));
    methodLogger.logEnd("payment", paramString, json);
    return json;
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
    final String paramString = methodLogger.generateParamsString(ctx, parameters);
    methodLogger.logStart("stockLevel", paramString);
    final String json =
        JSON.serialize(
            TPCCBusinessAPI.stockLevel(ctx, JSON.deserialize(parameters, StockLevelInput.class)));
    methodLogger.logEnd("init", paramString, "<void>");
    return json;
  }

  /**
   * Creates some dummy initial entities for testing.
   *
   * @param ctx The transaction context
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void init(final TPCCContext ctx) throws EntityExistsException, SerializationException {
    final String paramString = methodLogger.generateParamsString(ctx.toString());
    methodLogger.logStart("init", paramString);
    TPCCBusinessAPI.init(ctx);
    methodLogger.logEnd("init", paramString, "<void>");
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
    final String paramString = methodLogger.generateParamsString(ctx, w_id);
    methodLogger.logStart("readWarehouse", paramString);

    final Warehouse warehouse = Warehouse.builder().id(w_id).build();
    ctx.getRegistry().read(warehouse);

    ctx.commit();
    final String json = JSON.serialize(warehouse);
    methodLogger.logEnd("readWarehouse", paramString, json);
    return json;
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
    final String paramString = methodLogger.generateParamsString(ctx, w_id, d_id, o_id);
    methodLogger.logStart("readOrder", paramString);

    final Order order = Order.builder().w_id(w_id).d_id(d_id).id(o_id).build();
    ctx.getRegistry().read(order);

    ctx.commit();
    final String json = JSON.serialize(order);
    methodLogger.logEnd("readOrder", paramString, json);
    return json;
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
    final String paramString = methodLogger.generateParamsString(ctx, i_id);
    methodLogger.logStart("readItem", paramString);

    final Item item = Item.builder().id(i_id).build();
    ctx.getRegistry().read(item);

    ctx.commit();
    final String json = JSON.serialize(item);
    methodLogger.logEnd("readItem", paramString, json);
    return json;
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
    final String paramString = methodLogger.generateParamsString(ctx, w_id, d_id, o_id);
    methodLogger.logStart("readNewOrder", paramString);

    final NewOrder newOrder = NewOrder.builder().w_id(w_id).d_id(d_id).o_id(o_id).build();
    ctx.getRegistry().read(newOrder);

    ctx.commit();
    final String json = JSON.serialize(newOrder);
    methodLogger.logEnd("readNewOrder", paramString, json);
    return json;
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
    final String paramString = methodLogger.generateParamsString(ctx.toString());
    methodLogger.logStart("ping", paramString);

    ctx.commit();
    final String pong = "pong";
    methodLogger.logEnd("ping", paramString, pong);
    return pong;
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
    final String paramString = methodLogger.generateParamsString(ctx, c_w_id, c_d_id, c_id);
    methodLogger.logStart("OJMLTEST__getCustomer", paramString);

    final Customer customer = Customer.builder().w_id(c_w_id).d_id(c_d_id).id(c_id).build();

    ctx.commit();
    final String json = JSON.serialize(customer);
    methodLogger.logEnd("OJMLTEST__getCustomer", paramString, json);
    return json;
  }
}
