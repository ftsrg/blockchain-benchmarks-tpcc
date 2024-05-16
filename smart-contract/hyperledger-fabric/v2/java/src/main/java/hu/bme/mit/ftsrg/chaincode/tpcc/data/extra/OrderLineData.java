/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.data.extra;

import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.OrderLine;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.output.OrderStatusOutput;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/**
 * Class to encapsulate the <code>order_lines</code> field's data of a {@link OrderStatusOutput}.
 */
@EqualsAndHashCode
@DataType
public final class OrderLineData {

  @Property(schema = {"minimum", "0"})
  private int ol_supply_w_id;

  @Property(schema = {"minimum", "0"})
  private int ol_i_id;

  @Property private int ol_quantity;

  @Property private double ol_amount;

  @Property private String ol_delivery_d;

  public OrderLineData(
      final int ol_supply_w_id,
      final int ol_i_id,
      final int ol_quantity,
      final double ol_amount,
      final String ol_delivery_d) {
    this.ol_supply_w_id = ol_supply_w_id;
    this.ol_i_id = ol_i_id;
    this.ol_quantity = ol_quantity;
    this.ol_amount = ol_amount;
    this.ol_delivery_d = ol_delivery_d;
  }

  public int getOl_supply_w_id() {
    return ol_supply_w_id;
  }

  public void setOl_supply_w_id(final int ol_supply_w_id) {
    this.ol_supply_w_id = ol_supply_w_id;
  }

  public int getOl_i_id() {
    return ol_i_id;
  }

  public void setOl_i_id(final int ol_i_id) {
    this.ol_i_id = ol_i_id;
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

  public String getOl_delivery_d() {
    return ol_delivery_d;
  }

  public void setOl_delivery_d(final String ol_delivery_d) {
    this.ol_delivery_d = ol_delivery_d;
  }

  public static OrderLineDataBuilder builder() {
    return new OrderLineDataBuilder();
  }

  public static final class OrderLineDataBuilder {
    private int ol_supply_w_id;
    private int ol_i_id;
    private int ol_quantity;
    private double ol_amount;
    private String ol_delivery_d;

    OrderLineDataBuilder() {}

    public OrderLineDataBuilder ol_supply_w_id(final int ol_supply_w_id) {
      this.ol_supply_w_id = ol_supply_w_id;
      return this;
    }

    public OrderLineDataBuilder ol_i_id(final int ol_i_id) {
      this.ol_i_id = ol_i_id;
      return this;
    }

    public OrderLineDataBuilder ol_quantity(final int ol_quantity) {
      this.ol_quantity = ol_quantity;
      return this;
    }

    public OrderLineDataBuilder ol_amount(final double ol_amount) {
      this.ol_amount = ol_amount;
      return this;
    }

    public OrderLineDataBuilder ol_delivery_d(final String ol_delivery_d) {
      this.ol_delivery_d = ol_delivery_d;
      return this;
    }

    public OrderLineDataBuilder fromOrderLine(final OrderLine orderLine) {
      this.ol_supply_w_id = orderLine.getOl_supply_w_id();
      this.ol_i_id = orderLine.getOl_i_id();
      this.ol_quantity = orderLine.getOl_quantity();
      this.ol_amount = orderLine.getOl_amount();
      this.ol_delivery_d = orderLine.getOl_delivery_d();
      return this;
    }

    public OrderLineData build() {
      return new OrderLineData(
          this.ol_supply_w_id, this.ol_i_id, this.ol_quantity, this.ol_amount, this.ol_delivery_d);
    }
  }
}
