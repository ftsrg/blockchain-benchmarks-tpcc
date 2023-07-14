/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.inputs;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DoPaymentInputParameters {

  @Property(schema = {"minimum", "0"})
  /** The warehouse ID. */
  public int w_id;

  @Property(schema = {"minimum", "0"})
  /** The district ID */
  public int d_id;

  @Property
  /** The The payment amount. */
  public Double h_amount;

  @Property(schema = {"minimum", "0"})
  /** The warehouse ID to which the customer belongs to. */
  public int c_w_id;

  @Property(schema = {"minimum", "0"})
  /** The district ID to which the customer belongs to. */
  public int c_d_id;

  @Property(schema = {"minimum", "0"})
  /** The customer ID. */
  public int c_id;

  @Property(schema = {"maxLength", "16"})
  /** The last name of the customer. */
  public String c_last;

  @Property
  /** The payment date. */
  public String h_date;
}
