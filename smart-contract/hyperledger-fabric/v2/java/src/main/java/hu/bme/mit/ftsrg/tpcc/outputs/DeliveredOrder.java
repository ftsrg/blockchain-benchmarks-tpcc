/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.outputs;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DeliveredOrder {

  @Property(schema = {"minimum", "0"})
  public int d_id;

  @Property(schema = {"minimum", "0"})
  public int o_id;

  public DeliveredOrder(final int d_id, final int o_id) {
    this.d_id = d_id;
    this.o_id = o_id;
  }
}
