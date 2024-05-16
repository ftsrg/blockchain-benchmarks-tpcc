/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.data.output;

import hu.bme.mit.ftsrg.chaincode.tpcc.data.extra.DeliveredOrder;
import java.util.List;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@EqualsAndHashCode
@DataType
public final class DeliveryOutput {

  @Property(schema = {"minimum", "0"})
  private int w_id;

  @Property private int o_carrier_id;
  @Property private List<DeliveredOrder> delivered;

  @Property(schema = {"minimum", "0"})
  private int skipped;

  public DeliveryOutput(
      final int w_id,
      final int o_carrier_id,
      final List<DeliveredOrder> deliveredOrders,
      final int skipped) {
    this.w_id = w_id;
    this.o_carrier_id = o_carrier_id;
    this.delivered = deliveredOrders;
    this.skipped = skipped;
  }

  public int getW_id() {
    return w_id;
  }

  public void setW_id(final int w_id) {
    this.w_id = w_id;
  }

  public int getO_carrier_id() {
    return o_carrier_id;
  }

  public void setO_carrier_id(final int o_carrier_id) {
    this.o_carrier_id = o_carrier_id;
  }

  public List<DeliveredOrder> getDelivered() {
    return delivered;
  }

  public void setDelivered(final List<DeliveredOrder> delivered) {
    this.delivered = delivered;
  }

  public int getSkipped() {
    return skipped;
  }

  public void setSkipped(final int skipped) {
    this.skipped = skipped;
  }

  public static DeliveryOutputBuilder builder() {
    return new DeliveryOutputBuilder();
  }

  public static final class DeliveryOutputBuilder {
    private int w_id;
    private int o_carrier_id;
    private List<DeliveredOrder> delivered;
    private int skipped;

    DeliveryOutputBuilder() {}

    public DeliveryOutputBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public DeliveryOutputBuilder o_carrier_id(final int o_carrier_id) {
      this.o_carrier_id = o_carrier_id;
      return this;
    }

    public DeliveryOutputBuilder delivered(final List<DeliveredOrder> delivered) {
      this.delivered = delivered;
      return this;
    }

    public DeliveryOutputBuilder skipped(final int skipped) {
      this.skipped = skipped;
      return this;
    }

    public DeliveryOutput build() {
      return new DeliveryOutput(this.w_id, this.o_carrier_id, this.delivered, this.skipped);
    }
  }
}
