/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.data.entity;

import hu.bme.mit.ftsrg.hypernate.entity.Entity;
import hu.bme.mit.ftsrg.hypernate.entity.KeyPart;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Essentially, the DISTRICT table. */
@EqualsAndHashCode
@DataType()
public final class District implements Entity {

  /** The district ID. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int d_id;

  /** The warehouse ID associated with the district. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int d_w_id;

  /** The name of the district. */
  @Property(schema = {"maxLength", "10"})
  private String d_name;

  /** The first street name of the district. */
  @Property(schema = {"maxLength", "20"})
  private String d_street_1;

  /** The second street name of the district. */
  @Property(schema = {"maxLength", "20"})
  private String d_street_2;

  /** The city of the district. */
  @Property(schema = {"maxLength", "20"})
  private String d_city;

  /** The state of the district. */
  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  private String d_state;

  /** The ZIP code of the district. */
  @Property(schema = {"pattern", "[0-9]{4}1111"})
  private String d_zip;

  /** The sales tax of the district. */
  @Property private double d_tax;

  /** The year to date balance of the district. */
  @Property private double d_ytd;

  /** The next available order ID. */
  @Property(schema = {"minimum", "0"})
  private int d_next_o_id;

  public District() {
    this.d_id = -1;
    this.d_w_id = -1;
  }

  public District(
      final int id,
      final int w_id,
      final String name,
      final String street_1,
      final String street_2,
      final String city,
      final String state,
      final double tax,
      final String zip,
      final int next_o_id,
      final double ytd) {
    this.d_id = id;
    this.d_w_id = w_id;
    this.d_name = name;
    this.d_street_1 = street_1;
    this.d_street_2 = street_2;
    this.d_city = city;
    this.d_state = state;
    this.d_tax = tax;
    this.d_zip = zip;
    this.d_next_o_id = next_o_id;
    this.d_ytd = ytd;
  }

  public int getD_id() {
    return d_id;
  }

  public int getD_w_id() {
    return d_w_id;
  }

  public String getD_name() {
    return d_name;
  }

  public void setD_name(final String d_name) {
    this.d_name = d_name;
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

  public double getD_tax() {
    return d_tax;
  }

  public void setD_tax(final double d_tax) {
    this.d_tax = d_tax;
  }

  public double getD_ytd() {
    return d_ytd;
  }

  public void setD_ytd(final double d_ytd) {
    this.d_ytd = d_ytd;
  }

  public int getD_next_o_id() {
    return d_next_o_id;
  }

  public void setD_next_o_id(final int d_next_o_id) {
    this.d_next_o_id = d_next_o_id;
  }

  public void increaseYTD(final double amount) {
    this.d_ytd += amount;
  }

  public void incrementNextOrderID() {
    ++this.d_next_o_id;
  }

  public static DistrictBuilder builder() {
    return new DistrictBuilder();
  }

  public static final class DistrictBuilder {

    private int id;
    private int w_id;
    private String name;
    private String street_1;
    private String street_2;
    private String city;
    private String state;
    private String zip;
    private double tax;
    private double ytd;
    private int next_o_id;

    DistrictBuilder() {}

    public DistrictBuilder id(final int id) {
      this.id = id;
      return this;
    }

    public DistrictBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public DistrictBuilder name(final String name) {
      this.name = name;
      return this;
    }

    public DistrictBuilder street_1(final String street_1) {
      this.street_1 = street_1;
      return this;
    }

    public DistrictBuilder street_2(final String street_2) {
      this.street_2 = street_2;
      return this;
    }

    public DistrictBuilder city(final String city) {
      this.city = city;
      return this;
    }

    public DistrictBuilder state(final String state) {
      this.state = state;
      return this;
    }

    public DistrictBuilder zip(final String zip) {
      this.zip = zip;
      return this;
    }

    public DistrictBuilder tax(final double tax) {
      this.tax = tax;
      return this;
    }

    public DistrictBuilder ytd(final double ytd) {
      this.ytd = ytd;
      return this;
    }

    public DistrictBuilder next_o_id(final int next_o_id) {
      this.next_o_id = next_o_id;
      return this;
    }

    public District build() {
      return new District(
          this.id,
          this.w_id,
          this.name,
          this.street_1,
          this.street_2,
          this.city,
          this.state,
          this.tax,
          this.zip,
          this.next_o_id,
          this.ytd);
    }
  }
}
