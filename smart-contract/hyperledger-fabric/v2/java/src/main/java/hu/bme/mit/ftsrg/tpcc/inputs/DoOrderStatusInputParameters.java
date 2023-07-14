/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.inputs;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DoOrderStatusInputParameters {

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  public int w_id;

  /** The district ID. */
  @Property(schema = {"minimum", "0"})
  public int d_id;

  /** The customer ID if provided. */
  @Property(schema = {"minimum", "0"})
  public int c_id;

  /** The last name of the customer if provided. */
  @Property(schema = {"maxLength", "16"})
  public String c_last;
}
