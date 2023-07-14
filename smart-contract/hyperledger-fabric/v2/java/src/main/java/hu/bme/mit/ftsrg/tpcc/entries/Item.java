/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entries;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class Item {

  /** The ID of the item. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int i_id;

  /** The image ID associated with the item. */
  @Property(schema = {"minimum", "0"})
  public int i_im_id;

  /** The name of the item. */
  @Property(schema = {"maxLength", "24"})
  public String i_name;

  /** The price of the item. */
  @Property(schema = {"minimum", "0"})
  public Double i_price;

  /** Brand information. */
  @Property(schema = {"maxLength", "50"})
  public String i_data;
}
