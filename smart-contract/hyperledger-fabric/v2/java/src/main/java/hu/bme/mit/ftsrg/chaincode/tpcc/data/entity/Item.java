/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.data.entity;

import hu.bme.mit.ftsrg.hypernate.entity.Entity;
import hu.bme.mit.ftsrg.hypernate.entity.KeyPart;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Essentially, the ITEM table. */
@EqualsAndHashCode
@DataType
public final class Item implements Entity<Item> {

  /** The ID of the item. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int i_id;

  /** The image ID associated with the item. */
  @Property(schema = {"minimum", "0"})
  private int i_im_id;

  /** The name of the item. */
  @Property(schema = {"maxLength", "24"})
  private String i_name;

  /** The price of the item. */
  @Property(schema = {"minimum", "0"})
  private double i_price;

  /** Brand information. */
  @Property(schema = {"maxLength", "50"})
  private String i_data;

  public Item() {
    this.i_id = -1;
  }

  public Item(
      final int id, final int im_id, final String name, final double price, final String data) {
    this.i_id = id;
    this.i_im_id = im_id;
    this.i_name = name;
    this.i_price = price;
    this.i_data = data;
  }

  public int getI_id() {
    return i_id;
  }

  public int getI_im_id() {
    return i_im_id;
  }

  public void setI_im_id(final int i_im_id) {
    this.i_im_id = i_im_id;
  }

  public String getI_name() {
    return i_name;
  }

  public void setI_name(final String i_name) {
    this.i_name = i_name;
  }

  public double getI_price() {
    return i_price;
  }

  public void setI_price(final double i_price) {
    this.i_price = i_price;
  }

  public String getI_data() {
    return i_data;
  }

  public void setI_data(final String i_data) {
    this.i_data = i_data;
  }

  public static ItemBuilder builder() {
    return new ItemBuilder();
  }

  public static final class ItemBuilder {
    private int id;
    private int im_id;
    private String name;
    private double price;
    private String data;

    ItemBuilder() {}

    public ItemBuilder id(final int id) {
      this.id = id;
      return this;
    }

    public ItemBuilder im_id(final int im_id) {
      this.im_id = im_id;
      return this;
    }

    public ItemBuilder name(final String name) {
      this.name = name;
      return this;
    }

    public ItemBuilder price(final double price) {
      this.price = price;
      return this;
    }

    public ItemBuilder data(final String data) {
      this.data = data;
      return this;
    }

    public Item build() {
      return new Item(this.id, this.im_id, this.name, this.price, this.data);
    }
  }
}
