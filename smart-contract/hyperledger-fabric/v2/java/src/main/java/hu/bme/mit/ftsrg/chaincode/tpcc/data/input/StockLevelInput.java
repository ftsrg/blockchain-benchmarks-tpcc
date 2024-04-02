/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.data.input;

import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Input parameters to {@link TPCC#doStockLevel(TPCCContext, String)}. */
@EqualsAndHashCode
@DataType
public final class StockLevelInput {

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  private int w_id;

  /** The district ID. */
  @Property(schema = {"minimum", "0"})
  private int d_id;

  /** The threshold of minimum quantity in stock to report. */
  @Property private int threshold;

  public int getW_id() {
    return w_id;
  }

  public void setW_id(final int w_id) {
    this.w_id = w_id;
  }

  public int getD_id() {
    return d_id;
  }

  public void setD_id(final int d_id) {
    this.d_id = d_id;
  }

  public int getThreshold() {
    return threshold;
  }

  public void setThreshold(final int threshold) {
    this.threshold = threshold;
  }
}
