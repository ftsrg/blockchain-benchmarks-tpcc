/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
SPDX-License-Identifier: Apache-2.0
*/

package hu.bme.mit.ftsrg.tpcc.entries;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class OrderLine {

  /** The order ID associated with the order line. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int ol_o_id;

  /** The district ID associated with the order line. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int ol_d_id;

  /** The warehouse ID associated with the order line. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int ol_w_id;

  /** The number/position/index of the order line. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int ol_number;

  /** The item ID associated with the order line. */
  @Property(schema = {"minimum", "0"})
  public int ol_i_id;

  /** The ID of the supplying warehouse. */
  @Property(schema = {"minimum", "0"})
  public int ol_supply_w_id;

  /** The date of delivery. */
  @Property public String ol_delivery_d;

  /** The quantity of items in the order line. */
  @Property(schema = {"minimum", "0"})
  public int ol_quantity;

  /** The amount to pay. */
  @Property public Double ol_amount;

  /** Information about the district. */
  @Property(schema = {"maxLength", "24"})
  public String ol_dist_info;
}
