/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entities;

import hu.bme.mit.ftsrg.tpcc.utils.Common;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@EqualsAndHashCode
@DataType
public final class History extends SerializableEntityBase<History> {

  /** The customer ID. Primary key. */
  @Property(schema = {"minimum", "0"})
  private final int h_c_id;

  /** The district ID associated with the customer. Primary key. */
  @Property(schema = {"minimum", "0"})
  private final int h_c_d_id;

  /** The warehouse ID associated with the customer. Primary key. */
  @Property(schema = {"minimum", "0"})
  private final int h_c_w_id;

  /** The district ID. */
  @Property(schema = {"minimum", "0"})
  private int h_d_id;

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  private int h_w_id;

  /** The date for the history. Primary key. */
  @Property private String h_date;

  /** The amount of payment. */
  @Property private double h_amount;

  /** Arbitrary information. */
  @Property(schema = {"maxLength", "24"})
  private String h_data;

  History() {
    this.h_c_id = -1;
    this.h_c_d_id = -1;
    this.h_c_w_id = -1;
  }

  public History(
      final int c_id,
      final int c_d_id,
      final int c_w_id,
      final int d_id,
      final int w_id,
      final String date,
      final double amount,
      final String data) {
    this.h_c_id = c_id;
    this.h_c_d_id = c_d_id;
    this.h_c_w_id = c_w_id;
    this.h_d_id = d_id;
    this.h_w_id = w_id;
    this.h_date = date;
    this.h_amount = amount;
    this.h_data = data;
  }

  @Override
  public String[] getKeyParts() {
    return new String[] {Common.pad(h_c_w_id), Common.pad(h_c_d_id), Common.pad(h_c_id), h_date};
  }

  @Override
  public EntityFactory<History> getFactory() {
    return History::new;
  }

  public int getH_c_id() {
    return h_c_id;
  }

  public int getH_c_d_id() {
    return h_c_d_id;
  }

  public int getH_c_w_id() {
    return h_c_w_id;
  }

  public int getH_d_id() {
    return h_d_id;
  }

  public void setH_d_id(final int h_d_id) {
    this.h_d_id = h_d_id;
  }

  public int getH_w_id() {
    return h_w_id;
  }

  public void setH_w_id(final int h_w_id) {
    this.h_w_id = h_w_id;
  }

  public String getH_date() {
    return h_date;
  }

  public void setH_date(final String h_date) {
    this.h_date = h_date;
  }

  public double getH_amount() {
    return h_amount;
  }

  public void setH_amount(final double h_amount) {
    this.h_amount = h_amount;
  }

  public String getH_data() {
    return h_data;
  }

  public void setH_data(final String h_data) {
    this.h_data = h_data;
  }

  public static HistoryBuilder builder() {
    return new HistoryBuilder();
  }

  public static final class HistoryBuilder {

    private int c_id;
    private int c_d_id;
    private int c_w_id;
    private int d_id;
    private int w_id;
    private String date;
    private double amount;
    private String data;

    HistoryBuilder() {}

    public HistoryBuilder c_id(final int c_id) {
      this.c_id = c_id;
      return this;
    }

    public HistoryBuilder c_d_id(final int c_d_id) {
      this.c_d_id = c_d_id;
      return this;
    }

    public HistoryBuilder c_w_id(final int c_w_id) {
      this.c_w_id = c_w_id;
      return this;
    }

    public HistoryBuilder d_id(final int d_id) {
      this.d_id = d_id;
      return this;
    }

    public HistoryBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public HistoryBuilder date(final String date) {
      this.date = date;
      return this;
    }

    public HistoryBuilder amount(final double amount) {
      this.amount = amount;
      return this;
    }

    public HistoryBuilder data(final String data) {
      this.data = data;
      return this;
    }

    public History build() {
      return new History(
          this.c_id,
          this.c_d_id,
          this.c_w_id,
          this.d_id,
          this.w_id,
          this.date,
          this.amount,
          this.data);
    }
  }
}
