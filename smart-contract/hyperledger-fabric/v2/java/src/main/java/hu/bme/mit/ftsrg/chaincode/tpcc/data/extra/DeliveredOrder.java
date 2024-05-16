/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.data.extra;

import hu.bme.mit.ftsrg.chaincode.tpcc.data.output.DeliveryOutput;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Class to encapsulate the <code>delivered</code> field's data of a {@link DeliveryOutput}. */
@EqualsAndHashCode
@DataType
public final class DeliveredOrder {

  @Property(schema = {"minimum", "0"})
  private int d_id;

  @Property(schema = {"minimum", "0"})
  private int o_id;

  public DeliveredOrder(final int d_id, final int o_id) {
    this.d_id = d_id;
    this.o_id = o_id;
  }

  public int getD_id() {
    return d_id;
  }

  public void setD_id(final int d_id) {
    this.d_id = d_id;
  }

  public int getO_id() {
    return o_id;
  }

  public void setO_id(final int o_id) {
    this.o_id = o_id;
  }

  public static DeliveredOrderBuilder builder() {
    return new DeliveredOrderBuilder();
  }

  public static final class DeliveredOrderBuilder {
    private int d_id;
    private int o_id;

    DeliveredOrderBuilder() {}

    public DeliveredOrderBuilder d_id(final int d_id) {
      this.d_id = d_id;
      return this;
    }

    public DeliveredOrderBuilder o_id(final int o_id) {
      this.o_id = o_id;
      return this;
    }

    public DeliveredOrder build() {
      return new DeliveredOrder(this.d_id, this.o_id);
    }
  }
}
