/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.data.output;

import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.Customer;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.District;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.History;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.Warehouse;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@EqualsAndHashCode
@DataType
public final class PaymentOutput {

  @Property(schema = {"minimum", "0"})
  private int w_id;

  @Property(schema = {"minimum", "0"})
  private int d_id;

  @Property(schema = {"minimum", "0"})
  private int c_id;

  @Property(schema = {"minimum", "0"})
  private int c_d_id;

  @Property(schema = {"minimum", "0"})
  private int c_w_id;

  @Property private double h_amount;
  @Property private String h_date;

  @Property(schema = {"maxLength", "20"})
  private String w_street_1;

  @Property(schema = {"maxLength", "20"})
  private String w_street_2;

  @Property(schema = {"maxLength", "20"})
  private String w_city;

  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  private String w_state;

  @Property(schema = {"pattern", "[0-9]{4}1111"})
  private String w_zip;

  @Property(schema = {"maxLength", "20"})
  private String d_street_1;

  @Property(schema = {"maxLength", "20"})
  private String d_street_2;

  @Property(schema = {"maxLength", "20"})
  private String d_city;

  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  private String d_state;

  @Property(schema = {"pattern", "[0-9]{4}1111"})
  private String d_zip;

  @Property(schema = {"maxLength", "16"})
  private String c_first;

  @Property(schema = {"minLength", "2", "maxLength", "2"})
  private String c_middle;

  @Property(schema = {"maxLength", "16"})
  private String c_last;

  @Property(schema = {"maxLength", "20"})
  private String c_street_1;

  @Property(schema = {"maxLength", "20"})
  private String c_street_2;

  @Property(schema = {"maxLength", "20"})
  private String c_city;

  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  private String c_state;

  @Property(schema = {"pattern", "[0-9]{4}1111"})
  private String c_zip;

  @Property(schema = {"minLength", "16", "maxLength", "16"})
  private String c_phone;

  @Property private String c_since;

  @Property(schema = {"pattern", "[GB]C"})
  private String c_credit;

  @Property private int c_credit_lim;
  @Property private double c_discount;
  @Property private double c_balance;

  @Property(schema = {"maxLength", "500"})
  private String c_data;

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

  public int getC_d_id() {
    return c_d_id;
  }

  public void setC_d_id(final int c_d_id) {
    this.c_d_id = c_d_id;
  }

  public int getC_w_id() {
    return c_w_id;
  }

  public void setC_w_id(final int c_w_id) {
    this.c_w_id = c_w_id;
  }

  public double getH_amount() {
    return h_amount;
  }

  public void setH_amount(final double h_amount) {
    this.h_amount = h_amount;
  }

  public String getH_date() {
    return h_date;
  }

  public void setH_date(final String h_date) {
    this.h_date = h_date;
  }

  public String getW_street_1() {
    return w_street_1;
  }

  public void setW_street_1(final String w_street_1) {
    this.w_street_1 = w_street_1;
  }

  public String getW_street_2() {
    return w_street_2;
  }

  public void setW_street_2(final String w_street_2) {
    this.w_street_2 = w_street_2;
  }

  public String getW_city() {
    return w_city;
  }

  public void setW_city(final String w_city) {
    this.w_city = w_city;
  }

  public String getW_state() {
    return w_state;
  }

  public void setW_state(final String w_state) {
    this.w_state = w_state;
  }

  public String getW_zip() {
    return w_zip;
  }

  public void setW_zip(final String w_zip) {
    this.w_zip = w_zip;
  }

  public String getD_street_1() {
    return d_street_1;
  }

  public void setD_street_1(final String d_street_1) {
    this.d_street_1 = d_street_1;
  }

  public String getD_street_2() {
    return d_street_2;
  }

  public void setD_street_2(final String d_street_2) {
    this.d_street_2 = d_street_2;
  }

  public String getD_city() {
    return d_city;
  }

  public void setD_city(final String d_city) {
    this.d_city = d_city;
  }

  public String getD_state() {
    return d_state;
  }

  public void setD_state(final String d_state) {
    this.d_state = d_state;
  }

  public String getD_zip() {
    return d_zip;
  }

  public void setD_zip(final String d_zip) {
    this.d_zip = d_zip;
  }

  public String getC_first() {
    return c_first;
  }

  public void setC_first(final String c_first) {
    this.c_first = c_first;
  }

  public String getC_middle() {
    return c_middle;
  }

  public void setC_middle(final String c_middle) {
    this.c_middle = c_middle;
  }

  public String getC_last() {
    return c_last;
  }

  public void setC_last(final String c_last) {
    this.c_last = c_last;
  }

  public String getC_street_1() {
    return c_street_1;
  }

  public void setC_street_1(final String c_street_1) {
    this.c_street_1 = c_street_1;
  }

  public String getC_street_2() {
    return c_street_2;
  }

  public void setC_street_2(final String c_street_2) {
    this.c_street_2 = c_street_2;
  }

  public String getC_city() {
    return c_city;
  }

  public void setC_city(final String c_city) {
    this.c_city = c_city;
  }

  public String getC_state() {
    return c_state;
  }

  public void setC_state(final String c_state) {
    this.c_state = c_state;
  }

  public String getC_zip() {
    return c_zip;
  }

  public void setC_zip(final String c_zip) {
    this.c_zip = c_zip;
  }

  public String getC_phone() {
    return c_phone;
  }

  public void setC_phone(final String c_phone) {
    this.c_phone = c_phone;
  }

  public String getC_since() {
    return c_since;
  }

  public void setC_since(final String c_since) {
    this.c_since = c_since;
  }

  public String getC_credit() {
    return c_credit;
  }

  public void setC_credit(final String c_credit) {
    this.c_credit = c_credit;
  }

  public int getC_credit_lim() {
    return c_credit_lim;
  }

  public void setC_credit_lim(final int c_credit_lim) {
    this.c_credit_lim = c_credit_lim;
  }

  public double getC_discount() {
    return c_discount;
  }

  public void setC_discount(final double c_discount) {
    this.c_discount = c_discount;
  }

  public double getC_balance() {
    return c_balance;
  }

  public void setC_balance(final double c_balance) {
    this.c_balance = c_balance;
  }

  public String getC_data() {
    return c_data;
  }

  public void setC_data(final String c_data) {
    this.c_data = c_data;
  }

  public PaymentOutput(
      final int w_id,
      final int d_id,
      final int c_id,
      final int c_d_id,
      final int c_w_id,
      final double h_amount,
      final String h_date,
      String w_street_1,
      final String w_street_2,
      final String w_city,
      final String w_state,
      final String w_zip,
      final String d_street_1,
      String d_street_2,
      final String d_city,
      final String d_state,
      final String d_zip,
      final String c_first,
      final String c_middle,
      String c_last,
      final String c_street_1,
      final String c_street_2,
      final String c_city,
      final String c_state,
      final String c_zip,
      String c_phone,
      final String c_since,
      final String c_credit,
      final int c_credit_lim,
      final double c_discount,
      final double c_balance,
      String c_data) {
    this.w_id = w_id;
    this.d_id = d_id;
    this.c_id = c_id;
    this.c_d_id = c_d_id;
    this.c_w_id = c_w_id;
    this.h_amount = h_amount;
    this.h_date = h_date;
    this.w_street_1 = w_street_1;
    this.w_street_2 = w_street_2;
    this.w_city = w_city;
    this.w_state = w_state;
    this.w_zip = w_zip;
    this.d_street_1 = d_street_1;
    this.d_street_2 = d_street_2;
    this.d_city = d_city;
    this.d_state = d_state;
    this.d_zip = d_zip;
    this.c_first = c_first;
    this.c_middle = c_middle;
    this.c_last = c_last;
    this.c_street_1 = c_street_1;
    this.c_street_2 = c_street_2;
    this.c_city = c_city;
    this.c_state = c_state;
    this.c_zip = c_zip;
    this.c_phone = c_phone;
    this.c_since = c_since;
    this.c_credit = c_credit;
    this.c_credit_lim = c_credit_lim;
    this.c_discount = c_discount;
    this.c_balance = c_balance;
    this.c_data = c_data;
  }

  public static DoPaymentOutputBuilder builder() {
    return new DoPaymentOutputBuilder();
  }

  public static final class DoPaymentOutputBuilder {
    private int w_id;
    private int d_id;
    private int c_id;
    private int c_d_id;
    private int c_w_id;
    private double h_amount;
    private String h_date;
    private String w_street_1;
    private String w_street_2;
    private String w_city;
    private String w_state;
    private String w_zip;
    private String d_street_1;
    private String d_street_2;
    private String d_city;
    private String d_state;
    private String d_zip;
    private String c_first;
    private String c_middle;
    private String c_last;
    private String c_street_1;
    private String c_street_2;
    private String c_city;
    private String c_state;
    private String c_zip;
    private String c_phone;
    private String c_since;
    private String c_credit;
    private int c_credit_lim;
    private double c_discount;
    private double c_balance;
    private String c_data;

    DoPaymentOutputBuilder() {}

    public DoPaymentOutputBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public DoPaymentOutputBuilder d_id(final int d_id) {
      this.d_id = d_id;
      return this;
    }

    public DoPaymentOutputBuilder c_id(final int c_id) {
      this.c_id = c_id;
      return this;
    }

    public DoPaymentOutputBuilder c_d_id(final int c_d_id) {
      this.c_d_id = c_d_id;
      return this;
    }

    public DoPaymentOutputBuilder c_w_id(final int c_w_id) {
      this.c_w_id = c_w_id;
      return this;
    }

    public DoPaymentOutputBuilder h_amount(final double h_amount) {
      this.h_amount = h_amount;
      return this;
    }

    public DoPaymentOutputBuilder h_date(final String h_date) {
      this.h_date = h_date;
      return this;
    }

    public DoPaymentOutputBuilder w_street_1(final String w_street_1) {
      this.w_street_1 = w_street_1;
      return this;
    }

    public DoPaymentOutputBuilder w_street_2(final String w_street_2) {
      this.w_street_2 = w_street_2;
      return this;
    }

    public DoPaymentOutputBuilder w_city(final String w_city) {
      this.w_city = w_city;
      return this;
    }

    public DoPaymentOutputBuilder w_state(final String w_state) {
      this.w_state = w_state;
      return this;
    }

    public DoPaymentOutputBuilder w_zip(final String w_zip) {
      this.w_zip = w_zip;
      return this;
    }

    public DoPaymentOutputBuilder d_street_1(final String d_street_1) {
      this.d_street_1 = d_street_1;
      return this;
    }

    public DoPaymentOutputBuilder d_street_2(final String d_street_2) {
      this.d_street_2 = d_street_2;
      return this;
    }

    public DoPaymentOutputBuilder d_city(final String d_city) {
      this.d_city = d_city;
      return this;
    }

    public DoPaymentOutputBuilder d_state(final String d_state) {
      this.d_state = d_state;
      return this;
    }

    public DoPaymentOutputBuilder d_zip(final String d_zip) {
      this.d_zip = d_zip;
      return this;
    }

    public DoPaymentOutputBuilder c_first(final String c_first) {
      this.c_first = c_first;
      return this;
    }

    public DoPaymentOutputBuilder c_middle(final String c_middle) {
      this.c_middle = c_middle;
      return this;
    }

    public DoPaymentOutputBuilder c_last(final String c_last) {
      this.c_last = c_last;
      return this;
    }

    public DoPaymentOutputBuilder c_street_1(final String c_street_1) {
      this.c_street_1 = c_street_1;
      return this;
    }

    public DoPaymentOutputBuilder c_street_2(final String c_street_2) {
      this.c_street_2 = c_street_2;
      return this;
    }

    public DoPaymentOutputBuilder c_city(final String c_city) {
      this.c_city = c_city;
      return this;
    }

    public DoPaymentOutputBuilder c_state(final String c_state) {
      this.c_state = c_state;
      return this;
    }

    public DoPaymentOutputBuilder c_zip(final String c_zip) {
      this.c_zip = c_zip;
      return this;
    }

    public DoPaymentOutputBuilder c_phone(final String c_phone) {
      this.c_phone = c_phone;
      return this;
    }

    public DoPaymentOutputBuilder c_since(final String c_since) {
      this.c_since = c_since;
      return this;
    }

    public DoPaymentOutputBuilder c_credit(final String c_credit) {
      this.c_credit = c_credit;
      return this;
    }

    public DoPaymentOutputBuilder c_credit_lim(final int c_credit_lim) {
      this.c_credit_lim = c_credit_lim;
      return this;
    }

    public DoPaymentOutputBuilder c_discount(final double c_discount) {
      this.c_discount = c_discount;
      return this;
    }

    public DoPaymentOutputBuilder c_balance(final double c_balance) {
      this.c_balance = c_balance;
      return this;
    }

    public DoPaymentOutputBuilder c_data(final String c_data) {
      this.c_data = c_data;
      return this;
    }

    public DoPaymentOutputBuilder fromWarehouse(final Warehouse warehouse) {
      this.w_id(warehouse.getW_id());
      this.w_street_1(warehouse.getW_street_1());
      this.w_street_2(warehouse.getW_street_2());
      this.w_city(warehouse.getW_city());
      this.w_state(warehouse.getW_state());
      this.w_zip(warehouse.getW_zip());
      return this;
    }

    public DoPaymentOutputBuilder fromDistrict(final District district) {
      this.d_id(district.getD_id());
      this.d_street_1(district.getD_street_1());
      this.d_street_2(district.getD_street_2());
      this.d_city(district.getD_city());
      this.d_state(district.getD_state());
      this.d_zip(district.getD_zip());
      return this;
    }

    public DoPaymentOutputBuilder fromCustomer(final Customer customer) {
      this.c_id(customer.getC_id());
      this.c_d_id(customer.getC_d_id());
      this.c_w_id(customer.getC_w_id());
      this.c_first(customer.getC_first());
      this.c_middle(customer.getC_middle());
      this.c_last(customer.getC_last());
      this.c_street_1(customer.getC_street_1());
      this.c_street_2(customer.getC_street_2());
      this.c_city(customer.getC_city());
      this.c_state(customer.getC_state());
      this.c_zip(customer.getC_zip());
      this.c_phone(customer.getC_phone());
      this.c_since(customer.getC_since());
      this.c_credit(customer.getC_credit());
      this.c_credit_lim(customer.getC_credit_lim());
      this.c_discount(customer.getC_discount());
      this.c_balance(customer.getC_balance());
      return this;
    }

    public DoPaymentOutputBuilder fromHistory(final History history) {
      this.h_amount = history.getH_amount();
      this.h_date = history.getH_date();
      return this;
    }

    public PaymentOutput build() {
      return new PaymentOutput(
          this.w_id,
          this.d_id,
          this.c_id,
          this.c_d_id,
          this.c_w_id,
          this.h_amount,
          this.h_date,
          this.w_street_1,
          this.w_street_2,
          this.w_city,
          this.w_state,
          this.w_zip,
          this.d_street_1,
          this.d_street_2,
          this.d_city,
          this.d_state,
          this.d_zip,
          this.c_first,
          this.c_middle,
          this.c_last,
          this.c_street_1,
          this.c_street_2,
          this.c_city,
          this.c_state,
          this.c_zip,
          this.c_phone,
          this.c_since,
          this.c_credit,
          this.c_credit_lim,
          this.c_discount,
          this.c_balance,
          this.c_data);
    }
  }
}
