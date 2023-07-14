/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entries;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class OrderLineData {

  @Property(schema = {"minimum", "0"})
  public int ol_supply_w_id;

  @Property(schema = {"minimum", "0"})
  public int ol_i_id;

  @Property public int ol_quantity;

  @Property public Double ol_amount;

  @Property public String ol_delivery_d;
}
