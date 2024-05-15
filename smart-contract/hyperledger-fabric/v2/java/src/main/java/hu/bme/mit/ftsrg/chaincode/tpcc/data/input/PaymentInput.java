/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.data.input;

import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@EqualsAndHashCode
@DataType
public final class PaymentInput {

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  private int w_id;

  /** The district ID */
  @Property(schema = {"minimum", "0"})
  private int d_id;

  /** The payment amount. */
  @Property private double h_amount;

  /** The warehouse ID to which the customer belongs to. */
  @Property(schema = {"minimum", "0"})
  private int c_w_id;

  /** The district ID to which the customer belongs to. */
  @Property(schema = {"minimum", "0"})
  private int c_d_id;

  /** The customer ID. */
  @Property(schema = {"minimum", "0"})
  private int c_id;

  /** The last name of the customer. */
  @Property(schema = {"maxLength", "16"})
  private String c_last;

  /** The payment date. */
  @Property private String h_date;

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

  public double getH_amount() {
    return h_amount;
  }

  public void setH_amount(final double h_amount) {
    this.h_amount = h_amount;
  }

  public int getC_w_id() {
    return c_w_id;
  }

  public void setC_w_id(final int c_w_id) {
    this.c_w_id = c_w_id;
  }

  public int getC_d_id() {
    return c_d_id;
  }

  public void setC_d_id(final int c_d_id) {
    this.c_d_id = c_d_id;
  }

  public int getC_id() {
    return c_id;
  }

  public void setC_id(final int c_id) {
    this.c_id = c_id;
  }

  public String getC_last() {
    return c_last;
  }

  public void setC_last(final String c_last) {
    this.c_last = c_last;
  }

  public String getH_date() {
    return h_date;
  }

  public void setH_date(final String h_date) {
    this.h_date = h_date;
  }
}
