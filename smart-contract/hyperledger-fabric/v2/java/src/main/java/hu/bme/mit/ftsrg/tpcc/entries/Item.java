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
public class Item extends SerializableEntityBase<Item> {

  public Item() {}

  @Property()
  // The ID of the item. Primary key.
  public int i_id;

  @Property()
  // The image ID associated with the item.
  public int i_im_id;

  @Property()
  // The name of the item.
  public String i_name;

  @Property()
  // The price of the item.
  public Double i_price;

  @Property()
  // Brand information.
  public String i_data;

  @Override
  public String[] getKeyParts() {
    return new String[] {Common.pad(i_id)};
  }

  // @Override
  // public String getType() {
  //   return TABLES.ITEM;
  // }

  @Override
  public EntityFactory<Item> getFactory() {
    return new EntityFactory<Item>() {
      @Override
      public Item create() {
        return new Item();
      }
    };
  }
}
