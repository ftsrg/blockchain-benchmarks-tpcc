/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.data.output;

import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.Customer;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.entity.Order;
import hu.bme.mit.ftsrg.chaincode.tpcc.data.extra.OrderLineData;
import java.util.List;
import lombok.EqualsAndHashCode;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/** Output of {@link TPCC#doOrderStatus(TPCCContext, String)}. */
@EqualsAndHashCode
@DataType
public final class OrderStatusOutput {

  @Property(schema = {"minimum", "0"})
  private int w_id;

  @Property(schema = {"minimum", "0"})
  private int d_id;

  @Property(schema = {"minimum", "0"})
  private int c_id;

  @Property(schema = {"maxLength", "16"})
  private String c_first;

  @Property(schema = {"minLength", "2", "maxLength", "2"})
  private String c_middle;

  @Property(schema = {"maxLength", "16"})
  private String c_last;

  @Property private double c_balance;

  @Property(schema = {"minimum", "0"})
  private int o_id;

  @Property private String o_entry_d;
  @Property private int o_carrier_id;
  @Property private List<OrderLineData> order_lines;

  public OrderStatusOutput(
      final int w_id,
      final int d_id,
      final int c_id,
      final String c_first,
      final String c_middle,
      final String c_last,
      double c_balance,
      final int o_id,
      final String o_entry_d,
      final int o_carrier_id,
      final List<OrderLineData> order_lines) {
    this.w_id = w_id;
    this.d_id = d_id;
    this.c_id = c_id;
    this.c_first = c_first;
    this.c_middle = c_middle;
    this.c_last = c_last;
    this.c_balance = c_balance;
    this.o_id = o_id;
    this.o_entry_d = o_entry_d;
    this.o_carrier_id = o_carrier_id;
    this.order_lines = order_lines;
  }

  public int getW_id() {
    return w_id;
  }

  public void setW_id(final int w_id) {
    this.w_id = w_id;
  }

  public int getD_id() {
    return d_id;
  }

  public void setD_id(final int d_id) {
    this.d_id = d_id;
  }

  public int getC_id() {
    return c_id;
  }

  public void setC_id(final int c_id) {
    this.c_id = c_id;
  }

  public String getC_first() {
    return c_first;
  }

  public void setC_first(final String c_first) {
    this.c_first = c_first;
  }

  public String getC_middle() {
    return c_middle;
  }

  public void setC_middle(final String c_middle) {
    this.c_middle = c_middle;
  }

  public String getC_last() {
    return c_last;
  }

  public void setC_last(final String c_last) {
    this.c_last = c_last;
  }

  public double getC_balance() {
    return c_balance;
  }

  public void setC_balance(final double c_balance) {
    this.c_balance = c_balance;
  }

  public int getO_id() {
    return o_id;
  }

  public void setO_id(final int o_id) {
    this.o_id = o_id;
  }

  public String getO_entry_d() {
    return o_entry_d;
  }

  public void setO_entry_d(final String o_entry_d) {
    this.o_entry_d = o_entry_d;
  }

  public int getO_carrier_id() {
    return o_carrier_id;
  }

  public void setO_carrier_id(final int o_carrier_id) {
    this.o_carrier_id = o_carrier_id;
  }

  public List<OrderLineData> getOrder_lines() {
    return order_lines;
  }

  public void setOrder_lines(final List<OrderLineData> order_lines) {
    this.order_lines = order_lines;
  }

  public static DoOrderStatusOutputBuilder builder() {
    return new DoOrderStatusOutputBuilder();
  }

  public static final class DoOrderStatusOutputBuilder {
    private int w_id;
    private int d_id;
    private int c_id;
    private String c_first;
    private String c_middle;
    private String c_last;
    private double c_balance;
    private int o_id;
    private String o_entry_d;
    private int o_carrier_id;
    private List<OrderLineData> order_lines;

    DoOrderStatusOutputBuilder() {}

    public DoOrderStatusOutputBuilder w_id(final int w_id) {
      this.w_id = w_id;
      return this;
    }

    public DoOrderStatusOutputBuilder d_id(final int d_id) {
      this.d_id = d_id;
      return this;
    }

    public DoOrderStatusOutputBuilder c_id(final int c_id) {
      this.c_id = c_id;
      return this;
    }

    public DoOrderStatusOutputBuilder c_first(final String c_first) {
      this.c_first = c_first;
      return this;
    }

    public DoOrderStatusOutputBuilder c_middle(final String c_middle) {
      this.c_middle = c_middle;
      return this;
    }

    public DoOrderStatusOutputBuilder c_last(final String c_last) {
      this.c_last = c_last;
      return this;
    }

    public DoOrderStatusOutputBuilder c_balance(final double c_balance) {
      this.c_balance = c_balance;
      return this;
    }

    public DoOrderStatusOutputBuilder o_id(final int o_id) {
      this.o_id = o_id;
      return this;
    }

    public DoOrderStatusOutputBuilder o_entry_d(final String o_entry_d) {
      this.o_entry_d = o_entry_d;
      return this;
    }

    public DoOrderStatusOutputBuilder o_carrier_id(final int o_carrier_id) {
      this.o_carrier_id = o_carrier_id;
      return this;
    }

    public DoOrderStatusOutputBuilder order_lines(final List<OrderLineData> order_lines) {
      this.order_lines = order_lines;
      return this;
    }

    public DoOrderStatusOutputBuilder fromCustomer(final Customer customer) {
      this.c_id = customer.getC_id();
      this.c_first = customer.getC_first();
      this.c_middle = customer.getC_middle();
      this.c_last = customer.getC_last();
      this.c_balance = customer.getC_balance();
      return this;
    }

    public DoOrderStatusOutputBuilder fromOrder(final Order order) {
      this.o_id = order.getO_id();
      this.o_entry_d = order.getO_entry_d();
      this.o_carrier_id = order.getO_carrier_id();
      return this;
    }

    public OrderStatusOutput build() {
      return new OrderStatusOutput(
          this.w_id,
          this.d_id,
          this.c_id,
          this.c_first,
          this.c_middle,
          this.c_last,
          this.c_balance,
          this.o_id,
          this.o_entry_d,
          this.o_carrier_id,
          this.order_lines);
    }
  }
}
