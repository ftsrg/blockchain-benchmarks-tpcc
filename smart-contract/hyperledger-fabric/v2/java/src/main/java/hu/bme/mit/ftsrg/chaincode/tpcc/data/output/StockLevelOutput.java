/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.data.output;

import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@EqualsAndHashCode
@DataType
public final class StockLevelOutput {

  @Property(schema = {"minimum", "0"})
  private int w_id;

  @Property(schema = {"minimum", "0"})
  private int d_id;

  @Property private int threshold;
  @Property private int low_stock;

  public StockLevelOutput(
      final int w_id, final int d_id, final int threshold, final int low_stock) {
    this.w_id = w_id;
    this.d_id = d_id;
    this.threshold = threshold;
    this.low_stock = low_stock;
  }

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

  public int getLow_stock() {
    return low_stock;
  }

  public void setLow_stock(final int low_stock) {
    this.low_stock = low_stock;
  }

  public static DoStockLevelOutputBuilder builder() {
    return new DoStockLevelOutputBuilder();
  }

  public static final class DoStockLevelOutputBuilder {
    private int w_id;
    private int d_id;
    private int threshold;
    private int low_stock;

    DoStockLevelOutputBuilder() {}

    public DoStockLevelOutputBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public DoStockLevelOutputBuilder d_id(final int d_id) {
      this.d_id = d_id;
      return this;
    }

    public DoStockLevelOutputBuilder threshold(final int threshold) {
      this.threshold = threshold;
      return this;
    }

    public DoStockLevelOutputBuilder low_stock(final int low_stock) {
      this.low_stock = low_stock;
      return this;
    }

    public StockLevelOutput build() {
      return new StockLevelOutput(this.w_id, this.d_id, this.threshold, this.low_stock);
    }
  }
}
