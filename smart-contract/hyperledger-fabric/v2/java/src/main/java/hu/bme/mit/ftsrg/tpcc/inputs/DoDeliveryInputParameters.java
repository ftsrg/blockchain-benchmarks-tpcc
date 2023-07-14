/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.inputs;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DoDeliveryInputParameters {

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  public int w_id;

  /** The carrier ID for the order. */
  @Property public int o_carrier_id;

  /** The delivery date of the order. */
  @Property public String ol_delivery_d;

  public DoDeliveryInputParameters(
      final int w_id, final int o_carrier_id, final String ol_delivery_d) {
    this.w_id = w_id;
    this.o_carrier_id = o_carrier_id;
    this.ol_delivery_d = ol_delivery_d;
  }
}
