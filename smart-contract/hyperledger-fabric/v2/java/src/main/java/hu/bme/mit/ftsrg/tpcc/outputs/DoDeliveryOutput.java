/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.outputs;

import java.util.List;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@EqualsAndHashCode
@DataType
public final class DoDeliveryOutput {

  @Property(schema = {"minimum", "0"})
  private int w_id;

  @Property private int o_carrier_id;
  @Property private List<DeliveredOrder> delivered;

  @Property(schema = {"minimum", "0"})
  private int skipped;

  public DoDeliveryOutput(
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

  public DoDeliveryOutputBuilder builder() {
    return new DoDeliveryOutputBuilder();
  }

  public static final class DoDeliveryOutputBuilder {
    private int w_id;
    private int o_carrier_id;
    private List<DeliveredOrder> delivered;
    private int skipped;

    DoDeliveryOutputBuilder() {}

    public DoDeliveryOutputBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public DoDeliveryOutputBuilder o_carrier_id(final int o_carrier_id) {
      this.o_carrier_id = o_carrier_id;
      return this;
    }

    public DoDeliveryOutputBuilder delivered(final List<DeliveredOrder> delivered) {
      this.delivered = delivered;
      return this;
    }

    public DoDeliveryOutputBuilder skipped(final int skipped) {
      this.skipped = skipped;
      return this;
    }

    public DoDeliveryOutput build() {
      return new DoDeliveryOutput(this.w_id, this.o_carrier_id, this.delivered, this.skipped);
    }
  }
}
