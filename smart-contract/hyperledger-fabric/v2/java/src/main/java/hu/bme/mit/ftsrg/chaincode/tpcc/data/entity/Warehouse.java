/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.data.entity;

import hu.bme.mit.ftsrg.hypernate.entity.Entity;
import hu.bme.mit.ftsrg.hypernate.entity.KeyPart;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Essentially, the WAREHOUSE table. */
@EqualsAndHashCode
@DataType
public class Warehouse implements Entity<Warehouse> {

  /** The warehouse ID. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int w_id;

  /** The name of the warehouse. */
  @Property(schema = {"maxLength", "10"})
  private String w_name;

  /** The first street name of the warehouse. */
  @Property(schema = {"maxLength", "20"})
  private String w_street_1;

  /** The second street name of the warehouse. */
  @Property(schema = {"maxLength", "20"})
  private String w_street_2;

  /** The city of the warehouse. */
  @Property(schema = {"maxLength", "20"})
  private String w_city;

  /** The state of the warehouse. */
  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  private String w_state;

  /** The ZIP code of the warehouse. */
  @Property(schema = {"pattern", "[0-9]{4}1111"})
  private String w_zip;

  /** The sales tax of the warehouse. */
  @Property private double w_tax;

  /** The year to date balance of the warehouse. */
  @Property private double w_ytd;

  public Warehouse() {
    this.w_id = -1;
  }

  public Warehouse(
      final int id,
      final String name,
      final String street_1,
      final String street_2,
      final String city,
      final String state,
      String zip,
      final double tax,
      final double ytd) {
    this.w_id = id;
    this.w_name = name;
    this.w_street_1 = street_1;
    this.w_street_2 = street_2;
    this.w_city = city;
    this.w_state = state;
    this.w_zip = zip;
    this.w_tax = tax;
    this.w_ytd = ytd;
  }

  public int getW_id() {
    return w_id;
  }

  public String getW_name() {
    return w_name;
  }

  public void setW_name(final String w_name) {
    this.w_name = w_name;
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

  public double getW_tax() {
    return w_tax;
  }

  public void setW_tax(final double w_tax) {
    this.w_tax = w_tax;
  }

  public double getW_ytd() {
    return w_ytd;
  }

  public void setW_ytd(final double w_ytd) {
    this.w_ytd = w_ytd;
  }

  public void increaseYTD(final double amount) {
    this.w_ytd += amount;
  }

  public static WarehouseBuilder builder() {
    return new WarehouseBuilder();
  }

  public static final class WarehouseBuilder {
    private int id;
    private String name;
    private String street_1;
    private String street_2;
    private String city;
    private String state;
    private String zip;
    private double tax;
    private double ytd;

    WarehouseBuilder() {}

    public WarehouseBuilder id(final int id) {
      this.id = id;
      return this;
    }

    public WarehouseBuilder name(final String name) {
      this.name = name;
      return this;
    }

    public WarehouseBuilder street_1(final String street_1) {
      this.street_1 = street_1;
      return this;
    }

    public WarehouseBuilder street_2(final String street_2) {
      this.street_2 = street_2;
      return this;
    }

    public WarehouseBuilder city(final String city) {
      this.city = city;
      return this;
    }

    public WarehouseBuilder state(final String state) {
      this.state = state;
      return this;
    }

    public WarehouseBuilder zip(final String zip) {
      this.zip = zip;
      return this;
    }

    public WarehouseBuilder tax(final double tax) {
      this.tax = tax;
      return this;
    }

    public WarehouseBuilder ytd(final double ytd) {
      this.ytd = ytd;
      return this;
    }

    public Warehouse build() {
      return new Warehouse(
          this.id,
          this.name,
          this.street_1,
          this.street_2,
          this.city,
          this.state,
          this.zip,
          this.tax,
          this.ytd);
    }
  }
}
