/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.tpcc.entities;

import hu.bme.mit.ftsrg.tpcc.utils.Common;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@EqualsAndHashCode
@DataType
public final class NewOrder extends SerializableEntityBase<NewOrder> {

  /** The order ID. Primary key. */
  @Property(schema = {"minimum", "0"})
  private final int no_o_id;

  /** The district ID associated with the order. Primary key. */
  @Property(schema = {"minimum", "0"})
  private final int no_d_id;

  /** The warehouse ID associated with the order. Primary key. */
  @Property(schema = {"minimum", "0"})
  private final int no_w_id;

  NewOrder() {
    this.no_o_id = -1;
    this.no_d_id = -1;
    this.no_w_id = -1;
  }

  public NewOrder(final int o_id, final int d_id, final int w_id) {
    this.no_o_id = o_id;
    this.no_d_id = d_id;
    this.no_w_id = w_id;
  }

  @Override
  public String[] getKeyParts() {
    return new String[] {Common.pad(no_w_id), Common.pad(no_d_id), Common.pad(no_o_id)};
  }

  @Override
  public EntityFactory<NewOrder> getFactory() {
    return NewOrder::new;
  }

  public int getNo_o_id() {
    return no_o_id;
  }

  public int getNo_d_id() {
    return no_d_id;
  }

  public int getNo_w_id() {
    return no_w_id;
  }

  public static NewOrderBuilder builder() {
    return new NewOrderBuilder();
  }

  public static final class NewOrderBuilder {
    private int o_id;
    private int d_id;
    private int w_id;

    NewOrderBuilder() {}

    public NewOrderBuilder o_id(final int o_id) {
      this.o_id = o_id;
      return this;
    }

    public NewOrderBuilder d_id(final int d_id) {
      this.d_id = d_id;
      return this;
    }

    public NewOrderBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public NewOrder build() {
      return new NewOrder(this.o_id, this.d_id, this.w_id);
    }
  }
}
