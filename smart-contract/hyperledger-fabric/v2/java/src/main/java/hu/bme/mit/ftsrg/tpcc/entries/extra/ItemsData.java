/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entries.extra;

import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@EqualsAndHashCode
@DataType
public final class ItemsData {

  @Property(schema = {"minimum", "0"})
  private int ol_supply_w_id;

  @Property(schema = {"minimum", "0"})
  private int ol_i_id;

  @Property(schema = {"maxLength", "24"})
  private String i_name;

  @Property private int ol_quantity;
  @Property private int s_quantity;
  @Property private String brand_generic;

  @Property(schema = {"minimum", "0"})
  private double i_price;

  @Property private double ol_amount;

  public ItemsData(
      final int ol_supply_w_id,
      final int ol_i_id,
      final String i_name,
      final int ol_quantity,
      final int s_quantity,
      final String brand_generic,
      final double i_price,
      final double ol_amount) {
    this.ol_supply_w_id = ol_supply_w_id;
    this.ol_i_id = ol_i_id;
    this.i_name = i_name;
    this.ol_quantity = ol_quantity;
    this.s_quantity = s_quantity;
    this.brand_generic = brand_generic;
    this.i_price = i_price;
    this.ol_amount = ol_amount;
  }

  public int getOl_supply_w_id() {
    return ol_supply_w_id;
  }

  public void setOl_supply_w_id(final int ol_supply_w_id) {
    this.ol_supply_w_id = ol_supply_w_id;
  }

  public int getOl_i_id() {
    return ol_i_id;
  }

  public void setOl_i_id(final int ol_i_id) {
    this.ol_i_id = ol_i_id;
  }

  public String getI_name() {
    return i_name;
  }

  public void setI_name(final String i_name) {
    this.i_name = i_name;
  }

  public int getOl_quantity() {
    return ol_quantity;
  }

  public void setOl_quantity(final int ol_quantity) {
    this.ol_quantity = ol_quantity;
  }

  public int getS_quantity() {
    return s_quantity;
  }

  public void setS_quantity(final int s_quantity) {
    this.s_quantity = s_quantity;
  }

  public String getBrand_generic() {
    return brand_generic;
  }

  public void setBrand_generic(final String brand_generic) {
    this.brand_generic = brand_generic;
  }

  public double getI_price() {
    return i_price;
  }

  public void setI_price(final double i_price) {
    this.i_price = i_price;
  }

  public double getOl_amount() {
    return ol_amount;
  }

  public void setOl_amount(final double ol_amount) {
    this.ol_amount = ol_amount;
  }

  public static ItemsDataBuilder builder() {
    return new ItemsDataBuilder();
  }

  public static final class ItemsDataBuilder {
    private int ol_supply_w_id;
    private int ol_i_id;
    private String i_name;
    private int ol_quantity;
    private int s_quantity;
    private String brand_generic;
    private double i_price;
    private double ol_amount;

    ItemsDataBuilder() {}

    public ItemsDataBuilder ol_supply_w_id(final int ol_supply_w_id) {
      this.ol_supply_w_id = ol_supply_w_id;
      return this;
    }

    public ItemsDataBuilder ol_i_id(final int ol_i_id) {
      this.ol_i_id = ol_i_id;
      return this;
    }

    public ItemsDataBuilder i_name(final String i_name) {
      this.i_name = i_name;
      return this;
    }

    public ItemsDataBuilder ol_quantity(final int ol_quantity) {
      this.ol_quantity = ol_quantity;
      return this;
    }

    public ItemsDataBuilder s_quantity(final int s_quantity) {
      this.s_quantity = s_quantity;
      return this;
    }

    public ItemsDataBuilder brand_generic(final String brand_generic) {
      this.brand_generic = brand_generic;
      return this;
    }

    public ItemsDataBuilder i_price(final double i_price) {
      this.i_price = i_price;
      return this;
    }

    public ItemsDataBuilder ol_amount(final double ol_amount) {
      this.ol_amount = ol_amount;
      return this;
    }

    public ItemsData build() {
      return new ItemsData(
          this.ol_supply_w_id,
          this.ol_i_id,
          this.i_name,
          this.ol_quantity,
          this.s_quantity,
          this.brand_generic,
          this.i_price,
          this.ol_amount);
    }
  }
}
