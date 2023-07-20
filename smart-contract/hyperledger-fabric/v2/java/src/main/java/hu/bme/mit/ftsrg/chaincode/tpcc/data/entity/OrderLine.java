/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.data.entity;

import hu.bme.mit.ftsrg.chaincode.dataaccess.KeyPart;
import hu.bme.mit.ftsrg.chaincode.dataaccess.SerializableEntityBase;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Essentially, the ORDER-LINE table. */
@EqualsAndHashCode
@DataType
public final class OrderLine extends SerializableEntityBase<OrderLine> {

  /** The order ID associated with the order line. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int ol_o_id;

  /** The district ID associated with the order line. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int ol_d_id;

  /** The warehouse ID associated with the order line. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int ol_w_id;

  /** The number/position/index of the order line. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int ol_number;

  /** The item ID associated with the order line. */
  @Property(schema = {"minimum", "0"})
  private int ol_i_id;

  /** The ID of the supplying warehouse. */
  @Property(schema = {"minimum", "0"})
  private int ol_supply_w_id;

  /** The date of delivery. */
  @Property private String ol_delivery_d;

  /** The quantity of items in the order line. */
  @Property(schema = {"minimum", "0"})
  private int ol_quantity;

  /** The amount to pay. */
  @Property private double ol_amount;

  /** Information about the district. */
  @Property(schema = {"maxLength", "24"})
  private String ol_dist_info;

  public OrderLine() {
    this.ol_o_id = -1;
    this.ol_d_id = -1;
    this.ol_w_id = -1;
    this.ol_number = -1;
  }

  public OrderLine(
      final int o_id,
      final int d_id,
      final int w_id,
      final int number,
      final int i_id,
      final int supply_w_id,
      String delivery_d,
      final int quantity,
      final double amount,
      final String dist_info) {
    this.ol_o_id = o_id;
    this.ol_d_id = d_id;
    this.ol_w_id = w_id;
    this.ol_number = number;
    this.ol_i_id = i_id;
    this.ol_supply_w_id = supply_w_id;
    this.ol_delivery_d = delivery_d;
    this.ol_quantity = quantity;
    this.ol_amount = amount;
    this.ol_dist_info = dist_info;
  }

  public int getOl_o_id() {
    return ol_o_id;
  }

  public int getOl_d_id() {
    return ol_d_id;
  }

  public int getOl_w_id() {
    return ol_w_id;
  }

  public int getOl_number() {
    return ol_number;
  }

  public int getOl_i_id() {
    return ol_i_id;
  }

  public void setOl_i_id(final int ol_i_id) {
    this.ol_i_id = ol_i_id;
  }

  public int getOl_supply_w_id() {
    return ol_supply_w_id;
  }

  public void setOl_supply_w_id(final int ol_supply_w_id) {
    this.ol_supply_w_id = ol_supply_w_id;
  }

  public String getOl_delivery_d() {
    return ol_delivery_d;
  }

  public void setOl_delivery_d(final String ol_delivery_d) {
    this.ol_delivery_d = ol_delivery_d;
  }

  public int getOl_quantity() {
    return ol_quantity;
  }

  public void setOl_quantity(final int ol_quantity) {
    this.ol_quantity = ol_quantity;
  }

  public double getOl_amount() {
    return ol_amount;
  }

  public void setOl_amount(final double ol_amount) {
    this.ol_amount = ol_amount;
  }

  public String getOl_dist_info() {
    return ol_dist_info;
  }

  public void setOl_dist_info(final String ol_dist_info) {
    this.ol_dist_info = ol_dist_info;
  }

  public static OrderLineBuilder builder() {
    return new OrderLineBuilder();
  }

  public static final class OrderLineBuilder {

    private int o_id;
    private int d_id;
    private int w_id;
    private int number;
    private int i_id;
    private int supply_w_id;
    private String delivery_d;
    private int quantity;
    private double amount;
    private String dist_info;

    OrderLineBuilder() {}

    public OrderLineBuilder o_id(final int o_id) {
      this.o_id = o_id;
      return this;
    }

    public OrderLineBuilder d_id(final int d_id) {
      this.d_id = d_id;
      return this;
    }

    public OrderLineBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public OrderLineBuilder number(final int number) {
      this.number = number;
      return this;
    }

    public OrderLineBuilder i_id(final int i_id) {
      this.i_id = i_id;
      return this;
    }

    public OrderLineBuilder supply_w_id(final int supply_w_id) {
      this.supply_w_id = supply_w_id;
      return this;
    }

    public OrderLineBuilder delivery_d(final String delivery_d) {
      this.delivery_d = delivery_d;
      return this;
    }

    public OrderLineBuilder quantity(final int quantity) {
      this.quantity = quantity;
      return this;
    }

    public OrderLineBuilder amount(final double amount) {
      this.amount = amount;
      return this;
    }

    public OrderLineBuilder dist_info(final String dist_info) {
      this.dist_info = dist_info;
      return this;
    }

    public OrderLine build() {
      return new OrderLine(
          this.o_id,
          this.d_id,
          this.w_id,
          this.number,
          this.i_id,
          this.supply_w_id,
          this.delivery_d,
          this.quantity,
          this.amount,
          this.dist_info);
    }
  }
}
