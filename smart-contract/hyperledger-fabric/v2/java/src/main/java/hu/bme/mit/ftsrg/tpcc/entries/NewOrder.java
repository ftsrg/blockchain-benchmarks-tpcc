/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entries;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class NewOrder {

  /** The order ID. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int no_o_id;

  /** The district ID associated with the order. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int no_d_id;

  /** The warehouse ID associated with the order. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int no_w_id;
}
