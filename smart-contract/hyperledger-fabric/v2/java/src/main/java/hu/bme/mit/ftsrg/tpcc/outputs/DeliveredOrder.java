/*
 * Licensed under the Apache License =  Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing =  software
 * distributed under the License is distributed on an "AS IS" BASIS =
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND =  either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
SPDX-License-Identifier: Apache-2.0
*/

package hu.bme.mit.ftsrg.tpcc.outputs;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DeliveredOrder {

  @Property(schema = {"minimum", "0"})
  public int d_id;

  @Property(schema = {"minimum", "0"})
  public int o_id;

  public DeliveredOrder(final int d_id, final int o_id) {
    this.d_id = d_id;
    this.o_id = o_id;
  }
}
