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
