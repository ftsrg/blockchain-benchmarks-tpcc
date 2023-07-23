/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.data.entity;

import hu.bme.mit.ftsrg.chaincode.dataaccess.KeyPart;
import hu.bme.mit.ftsrg.chaincode.dataaccess.SerializableEntityBase;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Essentially, the STOCK table. */
@EqualsAndHashCode
@DataType
public final class Stock extends SerializableEntityBase<Stock> {

  /** The ID of the item associated with the stock. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int s_i_id;

  /** The ID of the warehouse associated with the stock. Primary key. */
  @KeyPart
  @Property(schema = {"minimum", "0"})
  private final int s_w_id;

  /** The quantity of the related item. */
  @Property private int s_quantity;

  /** Information about district 1. */
  @Property(schema = {"maxLength", "24"})
  private String s_dist_01;

  /** Information about district 2. */
  @Property(schema = {"maxLength", "24"})
  private String s_dist_02;

  /** Information about district 3. */
  @Property(schema = {"maxLength", "24"})
  private String s_dist_03;

  /** Information about district 4. */
  @Property(schema = {"maxLength", "24"})
  private String s_dist_04;

  /** Information about district 5. */
  @Property(schema = {"maxLength", "24"})
  private String s_dist_05;

  /** Information about district 6. */
  @Property(schema = {"maxLength", "24"})
  private String s_dist_06;

  /** Information about district 7. */
  @Property(schema = {"maxLength", "24"})
  private String s_dist_07;

  /** Information about district 8. */
  @Property(schema = {"maxLength", "24"})
  private String s_dist_08;

  /** Information about district 9. */
  @Property(schema = {"maxLength", "24"})
  private String s_dist_09;

  /** Information about district 10. */
  @Property(schema = {"maxLength", "24"})
  private String s_dist_10;

  /** The year to date balance of the stock. */
  @Property(schema = {"minimum", "0"})
  private int s_ytd;

  /** The number of orders for the stock. */
  @Property(schema = {"minimum", "0"})
  private int s_order_cnt;

  /** The number of remote orders for the stock. */
  @Property(schema = {"minimum", "0"})
  private int s_remote_cnt;

  /** Stock information. */
  @Property(schema = {"maxLength", "50"})
  private String s_data;

  public Stock() {
    this.s_i_id = -1;
    this.s_w_id = -1;
  }

  public Stock(
      int i_id,
      int w_id,
      int quantity,
      String dist_01,
      String dist_02,
      String dist_03,
      String dist_04,
      String dist_05,
      String dist_06,
      String dist_07,
      String dist_08,
      String dist_09,
      String dist_10,
      int ytd,
      int order_cnt,
      int remote_cnt,
      String data) {
    this.s_i_id = i_id;
    this.s_w_id = w_id;
    this.s_quantity = quantity;
    this.s_dist_01 = dist_01;
    this.s_dist_02 = dist_02;
    this.s_dist_03 = dist_03;
    this.s_dist_04 = dist_04;
    this.s_dist_05 = dist_05;
    this.s_dist_06 = dist_06;
    this.s_dist_07 = dist_07;
    this.s_dist_08 = dist_08;
    this.s_dist_09 = dist_09;
    this.s_dist_10 = dist_10;
    this.s_ytd = ytd;
    this.s_order_cnt = order_cnt;
    this.s_remote_cnt = remote_cnt;
    this.s_data = data;
  }

  public int getS_i_id() {
    return s_i_id;
  }

  public int getS_w_id() {
    return s_w_id;
  }

  public int getS_quantity() {
    return s_quantity;
  }

  public void setS_quantity(final int s_quantity) {
    this.s_quantity = s_quantity;
  }

  public String getS_dist_01() {
    return s_dist_01;
  }

  // spotless:off
  //@ requires s_dist_01.length() == 24; // C.ENT:STOCK:DIST_01
  // spotless:on
  public void setS_dist_01(final String s_dist_01) {
    this.s_dist_01 = s_dist_01;
  }

  public String getS_dist_02() {
    return s_dist_02;
  }

  // spotless:off
  //@ requires s_dist_02.length() == 24; // C.ENT:STOCK:DIST_02
  // spotless:on
  public void setS_dist_02(final String s_dist_02) {
    this.s_dist_02 = s_dist_02;
  }

  public String getS_dist_03() {
    return s_dist_03;
  }

  // spotless:off
  //@ requires s_dist_03.length() == 24; // C.ENT:STOCK:DIST_03
  // spotless:on
  public void setS_dist_03(final String s_dist_03) {
    this.s_dist_03 = s_dist_03;
  }

  public String getS_dist_04() {
    return s_dist_04;
  }

  // spotless:off
  //@ requires s_dist_04.length() == 24; // C.ENT:STOCK:DIST_04
  // spotless:on
  public void setS_dist_04(final String s_dist_04) {
    this.s_dist_04 = s_dist_04;
  }

  public String getS_dist_05() {
    return s_dist_05;
  }

  // spotless:off
  //@ requires s_dist_05.length() == 24; // C.ENT:STOCK:DIST_05
  // spotless:on
  public void setS_dist_05(final String s_dist_05) {
    this.s_dist_05 = s_dist_05;
  }

  public String getS_dist_06() {
    return s_dist_06;
  }

  // spotless:off
  //@ requires s_dist_06.length() == 24; // C.ENT:STOCK:DIST_06
  // spotless:on
  public void setS_dist_06(final String s_dist_06) {
    this.s_dist_06 = s_dist_06;
  }

  public String getS_dist_07() {
    return s_dist_07;
  }

  // spotless:off
  //@ requires s_dist_07.length() == 24; // C.ENT:STOCK:DIST_07
  // spotless:on
  public void setS_dist_07(final String s_dist_07) {
    this.s_dist_07 = s_dist_07;
  }

  public String getS_dist_08() {
    return s_dist_08;
  }

  // spotless:off
  //@ requires s_dist_08.length() == 24; // C.ENT:STOCK:DIST_08
  // spotless:on
  public void setS_dist_08(final String s_dist_08) {
    this.s_dist_08 = s_dist_08;
  }

  public String getS_dist_09() {
    return s_dist_09;
  }

  // spotless:off
  //@ requires s_dist_09.length() == 24; // C.ENT:STOCK:DIST_09
  // spotless:on
  public void setS_dist_09(final String s_dist_09) {
    this.s_dist_09 = s_dist_09;
  }

  public String getS_dist_10() {
    return s_dist_10;
  }

  // spotless:off
  //@ requires s_dist_10.length() == 24; // C.ENT:STOCK:DIST_10
  // spotless:on
  public void setS_dist_10(final String s_dist_10) {
    this.s_dist_10 = s_dist_10;
  }

  public int getS_ytd() {
    return s_ytd;
  }

  // spotless:off
  //@ requires s_ytd >= 0; // C.ENT:STOCK:YTD
  // spotless:on
  public void setS_ytd(final int s_ytd) {
    this.s_ytd = s_ytd;
  }

  public int getS_order_cnt() {
    return s_order_cnt;
  }

  // spotless:off
  //@ requires s_order_cnt >= 0; // C.ENT:STOCK:ORDER_CNT
  // spotless:on
  public void setS_order_cnt(final int s_order_cnt) {
    this.s_order_cnt = s_order_cnt;
  }

  public int getS_remote_cnt() {
    return s_remote_cnt;
  }

  // spotless:off
  //@ requires s_remote_cnt >= 0; // C.ENT:STOCK:REMOTE_CNT
  // spotless:on
  public void setS_remote_cnt(final int s_remote_cnt) {
    this.s_remote_cnt = s_remote_cnt;
  }

  public String getS_data() {
    return s_data;
  }

  // spotless:off
  //@ requires s_data.length() <= 50; // C.ENT:STOCK:DATA
  // spotless:on
  public void setS_data(final String s_data) {
    this.s_data = s_data;
  }

  public void decreaseQuantity(final int amount) {
    this.s_quantity -= amount;
  }

  public void increaseYTD(final int amount) {
    this.s_ytd += amount;
  }

  public void incrementOrderCount() {
    ++this.s_order_cnt;
  }

  public void incrementRemoteCount() {
    ++this.s_remote_cnt;
  }

  public static StockBuilder builder() {
    return new StockBuilder();
  }

  public static final class StockBuilder {
    StockBuilder() {}

    private int i_id;
    private int w_id;
    private int quantity;
    private String dist_01;
    private String dist_02;
    private String dist_03;
    private String dist_04;
    private String dist_05;
    private String dist_06;
    private String dist_07;
    private String dist_08;
    private String dist_09;
    private String dist_10;
    private int ytd;
    private int order_cnt;
    private int remote_cnt;
    private String data;

    public StockBuilder i_id(final int i_id) {
      this.i_id = i_id;
      return this;
    }

    public StockBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public StockBuilder quantity(final int quantity) {
      this.quantity = quantity;
      return this;
    }

    // spotless:off
    //@ requires dist_01.length() == 24; // C.ENT:STOCK:DIST_01
    // spotless:on
    public StockBuilder dist_01(final String dist_01) {
      this.dist_01 = dist_01;
      return this;
    }

    // spotless:off
    //@ requires dist_02.length() == 24; // C.ENT:STOCK:DIST_02
    // spotless:on
    public StockBuilder dist_02(final String dist_02) {
      this.dist_02 = dist_02;
      return this;
    }

    // spotless:off
    //@ requires dist_03.length() == 24; // C.ENT:STOCK:DIST_03
    // spotless:on
    public StockBuilder dist_03(final String dist_03) {
      this.dist_03 = dist_03;
      return this;
    }

    // spotless:off
    //@ requires dist_04.length() == 24; // C.ENT:STOCK:DIST_04
    // spotless:on
    public StockBuilder dist_04(final String dist_04) {
      this.dist_04 = dist_04;
      return this;
    }

    // spotless:off
    //@ requires dist_05.length() == 24; // C.ENT:STOCK:DIST_05
    // spotless:on
    public StockBuilder dist_05(final String dist_05) {
      this.dist_05 = dist_05;
      return this;
    }

    // spotless:off
    //@ requires dist_06.length() == 24; // C.ENT:STOCK:DIST_06
    // spotless:on
    public StockBuilder dist_06(final String dist_06) {
      this.dist_06 = dist_06;
      return this;
    }

    // spotless:off
    //@ requires dist_07.length() == 24; // C.ENT:STOCK:DIST_07
    // spotless:on
    public StockBuilder dist_07(final String dist_07) {
      this.dist_07 = dist_07;
      return this;
    }

    // spotless:off
    //@ requires dist_08.length() == 24; // C.ENT:STOCK:DIST_08
    // spotless:on
    public StockBuilder dist_08(final String dist_08) {
      this.dist_08 = dist_08;
      return this;
    }

    // spotless:off
    //@ requires dist_09.length() == 24; // C.ENT:STOCK:DIST_09
    // spotless:on
    public StockBuilder dist_09(final String dist_09) {
      this.dist_09 = dist_09;
      return this;
    }

    // spotless:off
    //@ requires dist_10.length() == 24; // C.ENT:STOCK:DIST_10
    // spotless:on
    public StockBuilder dist_10(final String dist_10) {
      this.dist_10 = dist_10;
      return this;
    }

    public StockBuilder dist_all(final String value) {
      this.dist_01 = value;
      this.dist_02 = value;
      this.dist_03 = value;
      this.dist_04 = value;
      this.dist_05 = value;
      this.dist_06 = value;
      this.dist_07 = value;
      this.dist_08 = value;
      this.dist_09 = value;
      this.dist_10 = value;
      return this;
    }

    // spotless:off
    //@ requires ytd >= 0; // C.ENT:STOCK:YTD
    // spotless:on
    public StockBuilder ytd(final int ytd) {
      this.ytd = ytd;
      return this;
    }

    // spotless:off
    //@ requires order_cnt >= 0; // C.ENT:STOCK:ORDER_CNT
    // spotless:on
    public StockBuilder order_cnt(final int order_cnt) {
      this.order_cnt = order_cnt;
      return this;
    }

    // spotless:off
    //@ requires remote_cnt >= 0; // C.ENT:STOCK:REMOTE_CNT
    // spotless:on
    public StockBuilder remote_cnt(final int remote_cnt) {
      this.remote_cnt = remote_cnt;
      return this;
    }

    // spotless:off
    //@ requires data.length() <= 50; // C.ENT:STOCK:DATA
    // spotless:on
    public StockBuilder data(final String data) {
      this.data = data;
      return this;
    }

    public Stock build() {
      return new Stock(
          this.i_id,
          this.w_id,
          this.quantity,
          this.dist_01,
          this.dist_02,
          this.dist_03,
          this.dist_04,
          this.dist_05,
          this.dist_06,
          this.dist_07,
          this.dist_08,
          this.dist_09,
          this.dist_10,
          this.ytd,
          this.order_cnt,
          this.remote_cnt,
          this.data);
    }
  }
}
