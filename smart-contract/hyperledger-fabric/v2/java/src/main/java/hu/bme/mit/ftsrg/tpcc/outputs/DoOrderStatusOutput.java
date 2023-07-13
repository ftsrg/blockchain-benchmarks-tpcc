package hu.bme.mit.ftsrg.tpcc.outputs;

import hu.bme.mit.ftsrg.tpcc.entries.OrderLineData;
import java.util.List;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DoOrderStatusOutput {

  @Property(schema = {"minimum", "0"})
  public int w_id;

  @Property(schema = {"minimum", "0"})
  public int d_id;

  @Property(schema = {"minimum", "0"})
  public int c_id;

  @Property(schema = {"maxLength", "16"})
  public String c_first;

  @Property(schema = {"minLength", "2", "maxLength", "2"})
  public String c_middle;

  @Property(schema = {"maxLength", "16"})
  public String c_last;

  @Property public Double c_balance;

  @Property(schema = {"minimum", "0"})
  public int o_id;

  @Property public String o_entry_d;
  @Property public int o_carrier_id;
  @Property public List<OrderLineData> order_lines;
}
