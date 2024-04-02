/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.data.input;

import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Input parameters to {@link TPCC#doDelivery(TPCCContext, String)}. */
@EqualsAndHashCode
@DataType
public final class DeliveryInput {

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  private int w_id;

  /** The carrier ID for the order. */
  @Property private int o_carrier_id;

  /** The delivery date of the order. */
  @Property private String ol_delivery_d;

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

  public String getOl_delivery_d() {
    return ol_delivery_d;
  }

  public void setOl_delivery_d(final String ol_delivery_d) {
    this.ol_delivery_d = ol_delivery_d;
  }
}
