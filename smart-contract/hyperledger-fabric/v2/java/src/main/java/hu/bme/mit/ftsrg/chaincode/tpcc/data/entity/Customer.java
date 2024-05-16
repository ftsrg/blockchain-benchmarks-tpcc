/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.data.entity;

import hu.bme.mit.ftsrg.hypernate.entity.Entity;
import hu.bme.mit.ftsrg.hypernate.entity.KeyPart;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Essentially, the CUSTOMER table. */
@DataType
@EqualsAndHashCode
public final class Customer implements Entity {

  /** The customer ID. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int c_id;

  /** The district ID associated with the customer. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int c_d_id;

  /** The warehouse ID associated with the customer. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int c_w_id;

  /** The first name of the customer. */
  @Property(schema = {"maxLength", "16"})
  private String c_first;

  /** The middle name of the customer. */
  @Property(schema = {"minLength", "2", "maxLength", "2"})
  private String c_middle;

  /** The last name of the customer. */
  @Property(schema = {"maxLength", "16"})
  private String c_last;

  /** The first street name of the customer. */
  @Property(schema = {"maxLength", "20"})
  private String c_street_1;

  /** The second street name of the customer. */
  @Property(schema = {"maxLength", "20"})
  private String c_street_2;

  /** The city of the customer. */
  @Property(schema = {"maxLength", "20"})
  private String c_city;

  /** The state of the customer. */
  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  private String c_state;

  /** The ZIP code of the customer. */
  @Property(schema = {"pattern", "[0-9]{4}1111"})
  private String c_zip;

  /** The phone number of the customer */
  @Property(schema = {"minLength", "16", "maxLength", "16"})
  private String c_phone;

  /** The date when the customer was registered. */
  @Property private String c_since;

  /** The credit classification of the customer (GC or BC). */
  @Property(schema = {"pattern", "[GB]C"})
  private String c_credit;

  /** The credit limit of the customer. */
  @Property private int c_credit_lim;

  /** The discount for the customer. */
  @Property private double c_discount;

  /** The balance of the customer. */
  @Property private double c_balance;

  /** The year to date payment of the customer. */
  @Property private double c_ytd_payment;

  /** The number of times the customer paid. */
  @Property(schema = {"minimum", "0"})
  private int c_payment_cnt;

  /** The number of times a delivery was made for the customer. */
  @Property(schema = {"minimum", "0"})
  private int c_delivery_cnt;

  /** Arbitrary information. */
  @Property(schema = {"maxLength", "500"})
  private String c_data;

  public Customer() {
    this.c_id = -1;
    this.c_d_id = -1;
    this.c_w_id = -1;
  }

  public Customer(
      final int id,
      final int d_id,
      final int w_id,
      final String first,
      final String middle,
      final String last,
      final String street_1,
      final String street_2,
      final String city,
      final String state,
      final String zip,
      final String phone,
      final String since,
      final String credit,
      final int credit_lim,
      final double discount,
      final double balance,
      final double ytd_payment,
      final int payment_cnt,
      final int delivery_cnt,
      final String data) {
    this.c_id = id;
    this.c_d_id = d_id;
    this.c_w_id = w_id;
    this.c_first = first;
    this.c_middle = middle;
    this.c_last = last;
    this.c_street_1 = street_1;
    this.c_street_2 = street_2;
    this.c_city = city;
    this.c_state = state;
    this.c_zip = zip;
    this.c_phone = phone;
    this.c_since = since;
    this.c_credit = credit;
    this.c_credit_lim = credit_lim;
    this.c_discount = discount;
    this.c_balance = balance;
    this.c_ytd_payment = ytd_payment;
    this.c_payment_cnt = payment_cnt;
    this.c_delivery_cnt = delivery_cnt;
    this.c_data = data;
  }

  public int getC_id() {
    return c_id;
  }

  public int getC_d_id() {
    return c_d_id;
  }

  public int getC_w_id() {
    return c_w_id;
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

  public double getC_ytd_payment() {
    return c_ytd_payment;
  }

  public void setC_ytd_payment(final double c_ytd_payment) {
    this.c_ytd_payment = c_ytd_payment;
  }

  public int getC_payment_cnt() {
    return c_payment_cnt;
  }

  public void setC_payment_cnt(final int c_payment_cnt) {
    this.c_payment_cnt = c_payment_cnt;
  }

  public int getC_delivery_cnt() {
    return c_delivery_cnt;
  }

  public void setC_delivery_cnt(final int c_delivery_cnt) {
    this.c_delivery_cnt = c_delivery_cnt;
  }

  public String getC_data() {
    return c_data;
  }

  public void setC_data(final String c_data) {
    this.c_data = c_data;
  }

  public void increaseBalance(final double amount) {
    this.c_balance += amount;
  }

  public void decreaseBalance(final double amount) {
    this.c_balance -= amount;
  }

  public void increaseYTDPayment(final double amount) {
    this.c_ytd_payment += amount;
  }

  public void incrementDeliveryCount() {
    ++this.c_delivery_cnt;
  }

  public void incrementPaymentCount() {
    ++this.c_payment_cnt;
  }

  public static CustomerBuilder builder() {
    return new CustomerBuilder();
  }

  public static final class CustomerBuilder {
    private int id;
    private int d_id;
    private int w_id;
    private String first;
    private String middle;
    private String last;
    private String street_1;
    private String street_2;
    private String city;
    private String state;
    private String zip;
    private String phone;
    private String since;
    private String credit;
    private int credit_lim;
    private double discount;
    private double balance;
    private double ytd_payment;
    private int payment_cnt;
    private int delivery_cnt;
    private String data;

    CustomerBuilder() {}

    public CustomerBuilder id(final int id) {
      this.id = id;
      return this;
    }

    public CustomerBuilder d_id(final int d_id) {
      this.d_id = d_id;
      return this;
    }

    public CustomerBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public CustomerBuilder first(final String first) {
      this.first = first;
      return this;
    }

    public CustomerBuilder middle(final String middle) {
      this.middle = middle;
      return this;
    }

    public CustomerBuilder last(final String last) {
      this.last = last;
      return this;
    }

    public CustomerBuilder street_1(final String street_1) {
      this.street_1 = street_1;
      return this;
    }

    public CustomerBuilder street_2(final String street_2) {
      this.street_2 = street_2;
      return this;
    }

    public CustomerBuilder city(final String city) {
      this.city = city;
      return this;
    }

    public CustomerBuilder state(final String state) {
      this.state = state;
      return this;
    }

    public CustomerBuilder zip(final String zip) {
      this.zip = zip;
      return this;
    }

    public CustomerBuilder phone(final String phone) {
      this.phone = phone;
      return this;
    }

    public CustomerBuilder since(final String since) {
      this.since = since;
      return this;
    }

    public CustomerBuilder credit(final String credit) {
      this.credit = credit;
      return this;
    }

    public CustomerBuilder credit_lim(final int credit_lim) {
      this.credit_lim = credit_lim;
      return this;
    }

    public CustomerBuilder discount(final double discount) {
      this.discount = discount;
      return this;
    }

    public CustomerBuilder balance(final double balance) {
      this.balance = balance;
      return this;
    }

    public CustomerBuilder ytd_payment(final double ytd_payment) {
      this.ytd_payment = ytd_payment;
      return this;
    }

    public CustomerBuilder payment_cnt(final int payment_cnt) {
      this.payment_cnt = payment_cnt;
      return this;
    }

    public CustomerBuilder delivery_cnt(final int delivery_cnt) {
      this.delivery_cnt = delivery_cnt;
      return this;
    }

    public CustomerBuilder data(final String data) {
      this.data = data;
      return this;
    }

    public Customer build() {
      return new Customer(
          this.id,
          this.d_id,
          this.w_id,
          this.first,
          this.middle,
          this.last,
          this.street_1,
          this.street_2,
          this.city,
          this.state,
          this.zip,
          this.phone,
          this.since,
          this.credit,
          this.credit_lim,
          this.discount,
          this.balance,
          this.ytd_payment,
          this.payment_cnt,
          this.delivery_cnt,
          this.data);
    }
  }
}
