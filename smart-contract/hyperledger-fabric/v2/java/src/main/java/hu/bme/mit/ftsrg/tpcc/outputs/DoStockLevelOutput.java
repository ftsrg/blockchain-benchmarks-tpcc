package hu.bme.mit.ftsrg.tpcc.outputs;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DoStockLevelOutput {

  @Property(schema = {"minimum", "0"})
  public int w_id;

  @Property(schema = {"minimum", "0"})
  public int d_id;

  @Property public int threshold;
  @Property public int low_stock;
}
