/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.data.output;

import hu.bme.mit.ftsrg.chaincode.tpcc.data.extra.ItemsData;
import java.util.List;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@EqualsAndHashCode
@DataType
public final class NewOrderOutput {

  @Property(schema = {"minimum", "0"})
  private int w_id;

  @Property(schema = {"minimum", "0"})
  private int d_id;

  @Property(schema = {"minimum", "0"})
  private int c_id;

  @Property(schema = {"maxLength", "16"})
  private String c_last;

  @Property(schema = {"pattern", "[GB]C"})
  private String c_credit;

  @Property private double c_discount;
  @Property private double w_tax;
  @Property private double d_tax;

  @Property(schema = {"minimum", "0"})
  private int o_ol_cnt;

  @Property(schema = {"minimum", "0"})
  private int o_id;

  @Property private String o_entry_d;
  @Property private double total_amount;
  @Property private List<ItemsData> items;

  public NewOrderOutput(
      final int w_id,
      final int d_id,
      final int c_id,
      final String c_last,
      final String c_credit,
      final double c_discount,
      final double w_tax,
      final double d_tax,
      final int o_ol_cnt,
      final int o_id,
      final String o_entry_d,
      final double total_amount,
      final List<ItemsData> items) {
    this.w_id = w_id;
    this.d_id = d_id;
    this.c_id = c_id;
    this.c_last = c_last;
    this.c_credit = c_credit;
    this.c_discount = c_discount;
    this.w_tax = w_tax;
    this.d_tax = d_tax;
    this.o_ol_cnt = o_ol_cnt;
    this.o_id = o_id;
    this.o_entry_d = o_entry_d;
    this.total_amount = total_amount;
    this.items = items;
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

  public String getC_credit() {
    return c_credit;
  }

  public void setC_credit(final String c_credit) {
    this.c_credit = c_credit;
  }

  public double getC_discount() {
    return c_discount;
  }

  public void setC_discount(final double c_discount) {
    this.c_discount = c_discount;
  }

  public double getW_tax() {
    return w_tax;
  }

  public void setW_tax(final double w_tax) {
    this.w_tax = w_tax;
  }

  public double getD_tax() {
    return d_tax;
  }

  public void setD_tax(final double d_tax) {
    this.d_tax = d_tax;
  }

  public int getO_ol_cnt() {
    return o_ol_cnt;
  }

  public void setO_ol_cnt(final int o_ol_cnt) {
    this.o_ol_cnt = o_ol_cnt;
  }

  public int getO_id() {
    return o_id;
  }

  public void setO_id(final int o_id) {
    this.o_id = o_id;
  }

  public String getO_entry_d() {
    return o_entry_d;
  }

  public void setO_entry_d(final String o_entry_d) {
    this.o_entry_d = o_entry_d;
  }

  public double getTotal_amount() {
    return total_amount;
  }

  public void setTotal_amount(final double total_amount) {
    this.total_amount = total_amount;
  }

  public List<ItemsData> getItems() {
    return items;
  }

  public void setItems(final List<ItemsData> items) {
    this.items = items;
  }

  public static DoNewOrderOutputBuilder builder() {
    return new DoNewOrderOutputBuilder();
  }

  public static final class DoNewOrderOutputBuilder {
    private int w_id;
    private int d_id;
    private int c_id;
    private String c_last;
    private String c_credit;
    private double c_discount;
    private double w_tax;
    private double d_tax;
    private int o_ol_cnt;
    private int o_id;
    private String o_entry_d;
    private double total_amount;
    private List<ItemsData> items;

    DoNewOrderOutputBuilder() {}

    public DoNewOrderOutputBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public DoNewOrderOutputBuilder d_id(final int d_id) {
      this.d_id = d_id;
      return this;
    }

    public DoNewOrderOutputBuilder c_id(final int c_id) {
      this.c_id = c_id;
      return this;
    }

    public DoNewOrderOutputBuilder c_last(final String c_last) {
      this.c_last = c_last;
      return this;
    }

    public DoNewOrderOutputBuilder c_credit(final String c_credit) {
      this.c_credit = c_credit;
      return this;
    }

    public DoNewOrderOutputBuilder c_discount(final double c_discount) {
      this.c_discount = c_discount;
      return this;
    }

    public DoNewOrderOutputBuilder w_tax(final double w_tax) {
      this.w_tax = w_tax;
      return this;
    }

    public DoNewOrderOutputBuilder d_tax(final double d_tax) {
      this.d_tax = d_tax;
      return this;
    }

    public DoNewOrderOutputBuilder o_ol_cnt(final int o_ol_cnt) {
      this.o_ol_cnt = o_ol_cnt;
      return this;
    }

    public DoNewOrderOutputBuilder o_id(final int o_id) {
      this.o_id = o_id;
      return this;
    }

    public DoNewOrderOutputBuilder o_entry_d(final String o_entry_d) {
      this.o_entry_d = o_entry_d;
      return this;
    }

    public DoNewOrderOutputBuilder total_amount(final double total_amount) {
      this.total_amount = total_amount;
      return this;
    }

    public DoNewOrderOutputBuilder items(final List<ItemsData> items) {
      this.items = items;
      return this;
    }

    public NewOrderOutput build() {
      return new NewOrderOutput(
          this.w_id,
          this.d_id,
          this.c_id,
          this.c_last,
          this.c_credit,
          this.c_discount,
          this.w_tax,
          this.d_tax,
          this.o_ol_cnt,
          this.o_id,
          this.o_entry_d,
          this.total_amount,
          this.items);
    }
  }
}
