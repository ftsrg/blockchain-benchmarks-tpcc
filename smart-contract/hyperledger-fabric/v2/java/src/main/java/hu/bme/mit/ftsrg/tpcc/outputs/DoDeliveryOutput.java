/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.outputs;

import java.util.List;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DoDeliveryOutput {

  @Property(schema = {"minimum", "0"})
  public int w_id;

  @Property public int o_carrier_id;
  @Property public List<DeliveredOrder> delivered;

  @Property(schema, {"minimum", "0"})
  public int skipped;

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
}
