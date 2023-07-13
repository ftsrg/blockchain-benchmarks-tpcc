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

package hu.bme.mit.ftsrg.tpcc.inputs;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DoNewOrderInputParameters {

  /** The warehouse ID. */
  @Property(schema = {"minimum", "0"})
  public int w_id;

  /** The district ID. */
  @Property(schema = {"minimum", "0"})
  public int d_id;

  /** The customer ID. */
  @Property(schema = {"minimum", "0"})
  public int c_id;

  /** The date ISO string for the order entry. */
  @Property public String o_entry_d;

  /** The array of item IDs for the order lines. */
  @Property public int[] i_ids;

  /** The array of warehouse IDs for the order lines. */
  @Property public int[] i_w_ids;

  /** The array of quantities for the order lines. */
  @Property public int[] i_qtys;
}
