/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.inputs;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DoStockLevelInputParameters {

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  public int w_id;

  /** The district ID. */
  @Property(schema = {"minimum", "0"})
  public int d_id;

  /** The threshold of minimum quantity in stock to report. */
  @Property public int threshold;
}
