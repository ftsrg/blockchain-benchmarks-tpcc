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
public class History {

  /** The customer ID. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int h_c_id;

  /** The district ID associated with the customer. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int h_c_d_id;

  /** The warehouse ID associated with the customer. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int h_c_w_id;

  /** The district ID. */
  @Property(schema = {"minimum", "0"})
  public int h_d_id;

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  public int h_w_id;

  /** The date for the history. Primary key. */
  @Property public String h_date;

  /** The amount of payment. */
  @Property public Double h_amount;

  /** Arbitrary information. */
  @Property(schema = {"maxLength", "24"})
  public String h_data;
}
