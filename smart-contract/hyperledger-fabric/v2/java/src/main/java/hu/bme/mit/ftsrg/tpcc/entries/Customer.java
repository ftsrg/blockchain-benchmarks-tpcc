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

package hu.bme.mit.ftsrg.tpcc.entries;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class Customer {

  /** The customer ID. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int c_id;

  /** The district ID associated with the customer. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int c_d_id;

  /** The warehouse ID associated with the customer. Primary key. */
  @Property(schema = {"minimum", "0"})
  public int c_w_id;

  /** The first name of the customer. */
  @Property(schema = {"maxLength", "16"})
  public String c_first;

  /** The middle name of the customer. */
  @Property(schema = {"minLength", "2", "maxLength", "2"})
  public String c_middle;

  /** The last name of the customer. */
  @Property(schema = {"maxLength", "16"})
  public String c_last;

  /** The first street name of the customer. */
  @Property(schema = {"maxLength", "20"})
  public String c_street_1;

  /** The second street name of the customer. */
  @Property(schema = {"maxLength", "20"})
  public String c_street_2;

  /** The city of the customer. */
  @Property(schema = {"maxLength", "20"})
  public String c_city;

  /** The state of the customer. */
  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  public String c_state;

  /** The ZIP code of the customer. */
  @Property(schema = {"pattern", "[0-9]{4}1111"})
  public String c_zip;

  /** The phone number of the customer */
  @Property(schema = {"minLength", "16", "maxLength", "16"})
  public String c_phone;

  /** The date when the customer was registered. */
  @Property public String c_since;

  /** The credit classification of the customer (GC or BC). */
  @Property(schema = {"pattern", "[GB]C"})
  public String c_credit;

  /** The credit limit of the customer. */
  @Property public int c_credit_lim;

  /** The discount for the customer. */
  @Property public Double c_discount;

  /** The balance of the customer. */
  @Property public Double c_balance;

  /** The year to date payment of the customer. */
  @Property public int c_ytd_payment;

  /** The number of times the customer paid. */
  @Property(schema = {"minimum", "0"})
  public int c_payment_cnt;

  /** The number of times a delivery was made for the customer. */
  @Property(schema = {"minimum", "0"})
  public int c_delivery_cnt;

  /** Arbitrary information. */
  @Property(schema = {"maxLength", "500"})
  public String c_data;

  public Customer(
      final int c_id,
      final int c_d_id,
      final int c_w_id,
      final String c_first,
      final String c_middle,
      final String c_last,
      final String c_street_1,
      final String c_street_2,
      final String c_city,
      final String c_state,
      final String c_zip,
      final String c_phone,
      final String c_since,
      final String c_credit,
      final int c_credit_lim,
      final Double c_discount,
      final Double c_balance,
      final int c_ytd_payment,
      final int c_payment_cnt,
      final int c_delivery_cnt,
      final String c_data) {
    this.c_id = c_id;
    this.c_d_id = c_d_id;
    this.c_w_id = c_w_id;
    this.c_first = c_first;
    this.c_middle = c_middle;
    this.c_last = c_last;
    this.c_street_1 = c_street_1;
    this.c_street_2 = c_street_2;
    this.c_city = c_city;
    this.c_state = c_state;
    this.c_zip = c_zip;
    this.c_phone = c_phone;
    this.c_since = c_since;
    this.c_credit = c_credit;
    this.c_credit_lim = c_credit_lim;
    this.c_discount = c_discount;
    this.c_balance = c_balance;
    this.c_ytd_payment = c_ytd_payment;
    this.c_payment_cnt = c_payment_cnt;
    this.c_delivery_cnt = c_delivery_cnt;
    this.c_data = c_data;
  }
}
