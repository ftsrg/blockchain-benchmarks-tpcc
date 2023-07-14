/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.inputs;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DoNewOrderInputParameters {

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  public int w_id;

  /** The district ID. */
  @Property(schema = {"minimum", "0"})
  public int d_id;

  /** The customer ID. */
  @Property(schema = {"minimum", "0"})
  public int c_id;

  /** The date ISO string for the order entry. */
  @Property public String o_entry_d;

  /** The array of item IDs for the order lines. */
  @Property public int[] i_ids;

  /** The array of warehouse IDs for the order lines. */
  @Property public int[] i_w_ids;

  /** The array of quantities for the order lines. */
  @Property public int[] i_qtys;
}
