/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entries;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class Stock {

  /** The ID of the item associated with the stock. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int s_i_id;

  /** The ID of the warehouse associated with the stock. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int s_w_id;

  /** The quantity of the related item. */
  @Property public int s_quantity;

  /** Information about district 1. */
  @Property(schema = {"maxLength", "24"})
  public String s_dist_01;

  /** Information about district 2. */
  @Property(schema = {"maxLength", "24"})
  public String s_dist_02;

  /** Information about district 3. */
  @Property(schema = {"maxLength", "24"})
  public String s_dist_03;

  /** Information about district 4. */
  @Property(schema = {"maxLength", "24"})
  public String s_dist_04;

  /** Information about district 5. */
  @Property(schema = {"maxLength", "24"})
  public String s_dist_05;

  /** Information about district 6. */
  @Property(schema = {"maxLength", "24"})
  public String s_dist_06;

  /** Information about district 7. */
  @Property(schema = {"maxLength", "24"})
  public String s_dist_07;

  /** Information about district 8. */
  @Property(schema = {"maxLength", "24"})
  public String s_dist_08;

  /** Information about district 9. */
  @Property(schema = {"maxLength", "24"})
  public String s_dist_09;

  /** Information about district 10. */
  @Property(schema = {"maxLength", "24"})
  public String s_dist_10;

  /** The year to date balance of the stock. */
  @Property(schema = {"minimum", "0"})
  public int s_ytd;

  /** The number of orders for the stock. */
  @Property(schema = {"minimum", "0"})
  public int s_order_cnt;

  /** The number of remote orders for the stock. */
  @Property(schema = {"minimum", "0"})
  public int s_remote_cnt;

  /** Stock information. */
  @Property(schema = {"maxLength", "50"})
  public String s_data;

  public Stock(
      final int s_i_id,
      final int s_w_id,
      final int s_quantity,
      final String s_dist_01,
      final String s_dist_02,
      final String s_dist_03,
      final String s_dist_04,
      final String s_dist_05,
      final String s_dist_06,
      final String s_dist_07,
      final String s_dist_08,
      final String s_dist_09,
      final String s_dist_10,
      final int s_ytd,
      final int s_order_cnt,
      final int s_remote_cnt,
      final String s_data) {
    this.s_i_id = s_i_id;
    this.s_w_id = s_w_id;
    this.s_quantity = s_quantity;
    this.s_dist_01 = s_dist_01;
    this.s_dist_02 = s_dist_02;
    this.s_dist_03 = s_dist_03;
    this.s_dist_04 = s_dist_04;
    this.s_dist_05 = s_dist_05;
    this.s_dist_06 = s_dist_06;
    this.s_dist_07 = s_dist_07;
    this.s_dist_08 = s_dist_08;
    this.s_dist_09 = s_dist_09;
    this.s_dist_10 = s_dist_10;
    this.s_ytd = s_ytd;
    this.s_order_cnt = s_order_cnt;
    this.s_remote_cnt = s_remote_cnt;
    this.s_data = s_data;
  }
}
