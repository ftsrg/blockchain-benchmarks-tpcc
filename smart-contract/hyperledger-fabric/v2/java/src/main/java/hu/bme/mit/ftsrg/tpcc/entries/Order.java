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

package hu.bme.mit.ftsrg.tpcc.entries;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class Order {

  /** The order ID. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int o_id;

  /** The district ID associated with the order. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int o_d_id;

  /** The warehouse ID associated with the order. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int o_w_id;

  /** The customer ID associated with the order. */
  @Property(schema = {"minimum", "0"})
  public int o_c_id;

  /** The date when the order was submitted. */
  @Property public String o_entry_d;

  /** The carrier ID associated with the order. */
  @Property public int o_carrier_id;

  /** The number of lines in the order. */
  @Property(schema = {"minimum", "0"})
  public int o_ol_cnt;

  /** 1 if every order lines is local, otherwise 0. */
  @Property(schema = {"minimum", "0", "maximum", "1"})
  public int o_all_local;
}
