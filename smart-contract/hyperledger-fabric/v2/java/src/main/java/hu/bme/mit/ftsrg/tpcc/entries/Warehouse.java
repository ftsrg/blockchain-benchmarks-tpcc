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
public class Warehouse {

  /** The warehouse ID. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int w_id;

  /** The name of the warehouse. */
  @Property(schema = {"maxLength", "10"})
  public String w_name;

  /** The first street name of the warehouse. */
  @Property(schema = {"maxLength", "20"})
  public String w_street_1;

  /** The second street name of the warehouse. */
  @Property(schema = {"maxLength", "20"})
  public String w_street_2;

  /** The city of the warehouse. */
  @Property(schema = {"maxLength", "20"})
  public String w_city;

  /** The state of the warehouse. */
  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  public String w_state;

  /** The ZIP code of the warehouse. */
  @Property(schema = {"pattern", "[0-9]{4}1111"})
  public String w_zip;

  /** The sales tax of the warehouse. */
  @Property public Double w_tax;

  /** The year to date balance of the warehouse. */
  @Property public int w_ytd;
}
