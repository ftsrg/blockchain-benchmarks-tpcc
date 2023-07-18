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

import hu.bme.mit.ftsrg.tpcc.entities.EntityFactory;
import hu.bme.mit.ftsrg.tpcc.entities.SerializableEntityBase;
import hu.bme.mit.ftsrg.tpcc.utils.Common;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public class NewOrder extends SerializableEntityBase<NewOrder> {
  @Property()
  // The order ID. Primary key.
  public int no_o_id;

  @Property()
  // The district ID associated with the order. Primary key.
  public int no_d_id;

  @Property()
  // The warehouse ID associated with the order. Primary key.
  public int no_w_id;

  public NewOrder() {}

  public NewOrder(int no_o_id, int no_d_id, int no_w_id) {
    this.no_o_id = no_o_id;
    this.no_d_id = no_d_id;
    this.no_w_id = no_w_id;
  }

  @Override
  public String[] getKeyParts() {
    return new String[] {Common.pad(no_w_id), Common.pad(no_d_id), Common.pad(no_o_id)};
  }

  @Override
  public EntityFactory<NewOrder> getFactory() {
    return new EntityFactory<NewOrder>() {
      @Override
      public NewOrder create() {
        return new NewOrder();
      }
    };
  }
}
