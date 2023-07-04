package hu.bme.mit.ftsrc.tpcc.outputs;

// import java.util.List;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import hu.bme.mit.ftsrc.tpcc.entries.DeliveredOrder;

@DataType()
public class DoDeliveryOutput {
  @Property() private int w_id;
  @Property() private int o_carrier_id;
  @Property() private DeliveredOrder delivered;
  // @Property()
  // public int skipped;

  public DoDeliveryOutput(int w_id, int o_carrier_id, DeliveredOrder delivered) {
    this.w_id = w_id;
    this.o_carrier_id = o_carrier_id;
    this.delivered = delivered;
    // this.skipped = skipped;
  }
}
