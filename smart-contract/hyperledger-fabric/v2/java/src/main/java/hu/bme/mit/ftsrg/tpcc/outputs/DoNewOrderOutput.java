package hu.bme.mit.ftsrg.tpcc.outputs;

import hu.bme.mit.ftsrg.tpcc.entries.ItemsData;
import java.util.List;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DoNewOrderOutput {

  @Property(schema = {"minimum", "0"})
  public int w_id;

  @Property(schema = {"minimum", "0"})
  public int d_id;

  @Property(schema = {"minimum", "0"})
  public int c_id;

  @Property(schema = {"maxLength", "16"})
  public String c_last;

  @Property(schema = {"pattern", "[GB]C"})
  public String c_credit;

  @Property public Double c_discount;
  @Property public Double w_tax;
  @Property public Double d_tax;

  @Property(schema = {"minimum", "0"})
  public int o_ol_cnt;

  @Property(schema = {"minimum", "0"})
  public int o_id;

  @Property public String o_entry_d;
  @Property public Double total_amount;
  @Property public List<ItemsData> items;

  public DoNewOrderOutput(
      final int w_id,
      final int d_id,
      final int c_id,
      final String c_last,
      final String c_credit,
      final Double c_discount,
      final Double w_tax,
      final Double d_tax,
      final int o_ol_cnt,
      final int o_id,
      final String o_entry_d,
      final Double total_amount,
      final List<ItemsData> items) {
    this.w_id = w_id;
    this.d_id = d_id;
    this.c_id = c_id;
    this.c_last = c_last;
    this.c_credit = c_credit;
    this.c_discount = c_discount;
    this.w_tax = w_tax;
    this.d_tax = d_tax;
    this.o_ol_cnt = o_ol_cnt;
    this.o_id = o_id;
    this.o_entry_d = o_entry_d;
    this.total_amount = total_amount;
    this.items = items;
  }
}
