/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entries;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class District {

  /** The district ID. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int d_id;

  /** The warehouse ID associated with the district. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int d_w_id;

  /** The name of the district. */
  @Property(schema = {"maxLength", "10"})
  public String d_name;

  /** The first street name of the district. */
  @Property(schema = {"maxLength", "20"})
  public String d_street_1;

  /** The second street name of the district. */
  @Property(schema = {"maxLength", "20"})
  public String d_street_2;

  /** The city of the district. */
  @Property(schema = {"maxLength", "20"})
  public String d_city;

  /** The state of the district. */
  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  public String d_state;

  /** The ZIP code of the district. */
  @Property(schema = {"pattern", "[0-9]{4}1111"})
  public String d_zip;

  /** The sales tax of the district. */
  @Property public Double d_tax;

  /** The year to date balance of the district. */
  @Property public int d_ytd;

  /** The next available order ID. */
  @Property(schema = {"minimum", "0"})
  public int d_next_o_id;
}
