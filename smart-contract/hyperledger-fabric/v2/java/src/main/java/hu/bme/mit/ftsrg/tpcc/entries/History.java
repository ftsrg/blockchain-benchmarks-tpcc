/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entries;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class History {

  /** The customer ID. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int h_c_id;

  /** The district ID associated with the customer. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int h_c_d_id;

  /** The warehouse ID associated with the customer. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int h_c_w_id;

  /** The district ID. */
  @Property(schema = {"minimum", "0"})
  public int h_d_id;

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  public int h_w_id;

  /** The date for the history. Primary key. */
  @Property public String h_date;

  /** The amount of payment. */
  @Property public Double h_amount;

  /** Arbitrary information. */
  @Property(schema = {"maxLength", "24"})
  public String h_data;
}
