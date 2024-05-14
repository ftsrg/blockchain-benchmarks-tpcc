/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.data.entity;

import hu.bme.mit.ftsrg.hypernate.entity.Entity;
import hu.bme.mit.ftsrg.hypernate.entity.KeyPart;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Essentially, the ORDER table. */
@EqualsAndHashCode
@DataType
public final class Order implements Entity<Order> {

  /** The order ID. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int o_id;

  /** The district ID associated with the order. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int o_d_id;

  /** The warehouse ID associated with the order. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int o_w_id;

  /** The customer ID associated with the order. */
  @Property(schema = {"minimum", "0"})
  private int o_c_id;

  /** The date when the order was submitted. */
  @Property private String o_entry_d;

  /** The carrier ID associated with the order. */
  @Property private int o_carrier_id;

  /** The number of lines in the order. */
  @Property(schema = {"minimum", "0"})
  private int o_ol_cnt;

  /** 1 if every order lines is local, otherwise 0. */
  @Property(schema = {"minimum", "0", "maximum", "1"})
  private int o_all_local;

  public Order() {
    this.o_id = -1;
    this.o_d_id = -1;
    this.o_w_id = -1;
  }

  public Order(
      final int id,
      final int d_id,
      final int w_id,
      final int c_id,
      final String entry_d,
      final int carrier_id,
      final int ol_cnt,
      final int all_local) {
    this.o_id = id;
    this.o_d_id = d_id;
    this.o_w_id = w_id;
    this.o_c_id = c_id;
    this.o_entry_d = entry_d;
    this.o_carrier_id = carrier_id;
    this.o_ol_cnt = ol_cnt;
    this.o_all_local = all_local;
  }

  public int getO_id() {
    return o_id;
  }

  public int getO_d_id() {
    return o_d_id;
  }

  public int getO_w_id() {
    return o_w_id;
  }

  public int getO_c_id() {
    return o_c_id;
  }

  public void setO_c_id(final int o_c_id) {
    this.o_c_id = o_c_id;
  }

  public String getO_entry_d() {
    return o_entry_d;
  }

  public void setO_entry_d(final String o_entry_d) {
    this.o_entry_d = o_entry_d;
  }

  public int getO_carrier_id() {
    return o_carrier_id;
  }

  public void setO_carrier_id(final int o_carrier_id) {
    this.o_carrier_id = o_carrier_id;
  }

  public int getO_ol_cnt() {
    return o_ol_cnt;
  }

  public void setO_ol_cnt(final int o_ol_cnt) {
    this.o_ol_cnt = o_ol_cnt;
  }

  public int getO_all_local() {
    return o_all_local;
  }

  public void setO_all_local(final int o_all_local) {
    this.o_all_local = o_all_local;
  }

  public static OrderBuilder builder() {
    return new OrderBuilder();
  }

  public static final class OrderBuilder {

    private int id;
    private int d_id;
    private int w_id;
    private int c_id;
    private String entry_d;
    private int carrier_id;
    private int ol_cnt;
    private int all_local;

    OrderBuilder() {}

    public OrderBuilder id(final int id) {
      this.id = id;
      return this;
    }

    public OrderBuilder d_id(final int d_id) {
      this.d_id = d_id;
      return this;
    }

    public OrderBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public OrderBuilder c_id(final int c_id) {
      this.c_id = c_id;
      return this;
    }

    public OrderBuilder entry_d(final String entry_d) {
      this.entry_d = entry_d;
      return this;
    }

    public OrderBuilder carrier_id(final int carrier_id) {
      this.carrier_id = carrier_id;
      return this;
    }

    public OrderBuilder ol_cnt(final int ol_cnt) {
      this.ol_cnt = ol_cnt;
      return this;
    }

    public OrderBuilder all_local(final int all_local) {
      this.all_local = all_local;
      return this;
    }

    public Order build() {
      return new Order(
          this.id,
          this.d_id,
          this.w_id,
          this.c_id,
          this.entry_d,
          this.carrier_id,
          this.ol_cnt,
          this.all_local);
    }
  }
}
