/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.data.input;

import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@EqualsAndHashCode
@DataType
public final class OrderStatusInput {

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  private int w_id;

  /** The district ID. */
  @Property(schema = {"minimum", "0"})
  private int d_id;

  /** The customer ID if provided. */
  @Property(schema = {"minimum", "0"})
  private int c_id;

  /** The last name of the customer if provided. */
  @Property(schema = {"maxLength", "16"})
  private String c_last;

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

  public String getC_last() {
    return c_last;
  }

  public void setC_last(final String c_last) {
    this.c_last = c_last;
  }
}
