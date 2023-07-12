package hu.bme.mit.ftsrg.tpcc.outputs;

import java.util.List;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public class DoDeliveryOutput {
  @Property() private int w_id;
  @Property() private int o_carrier_id;
  @Property() private List<DeliveredOrder> delivered;
  @Property() private int skipped;

  // @Property()
  // public int skipped;

  public DoDeliveryOutput(
      int w_id, int o_carrier_id, List<DeliveredOrder> deliveredOrders, int skipped) {
    this.w_id = w_id;
    this.o_carrier_id = o_carrier_id;
    this.delivered = deliveredOrders;
    this.skipped = skipped;
  }
}
