/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.data.input;

import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Input parameters to {@link TPCC#doNewOrder(TPCCContext, String)}. */
@EqualsAndHashCode
@DataType
public final class NewOrderInput {

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  private int w_id;

  /** The district ID. */
  @Property(schema = {"minimum", "0"})
  private int d_id;

  /** The customer ID. */
  @Property(schema = {"minimum", "0"})
  private int c_id;

  /** The date ISO string for the order entry. */
  @Property private String o_entry_d;

  /** The array of item IDs for the order lines. */
  @Property private int[] i_ids;

  /** The array of warehouse IDs for the order lines. */
  @Property private int[] i_w_ids;

  /** The array of quantities for the order lines. */
  @Property private int[] i_qtys;

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

  public int getC_id() {
    return c_id;
  }

  public void setC_id(final int c_id) {
    this.c_id = c_id;
  }

  public String getO_entry_d() {
    return o_entry_d;
  }

  public void setO_entry_d(final String o_entry_d) {
    this.o_entry_d = o_entry_d;
  }

  public int[] getI_ids() {
    return i_ids;
  }

  public void setI_ids(final int[] i_ids) {
    this.i_ids = i_ids;
  }

  public int[] getI_w_ids() {
    return i_w_ids;
  }

  public void setI_w_ids(final int[] i_w_ids) {
    this.i_w_ids = i_w_ids;
  }

  public int[] getI_qtys() {
    return i_qtys;
  }

  public void setI_qtys(final int[] i_qtys) {
    this.i_qtys = i_qtys;
  }
}
