package hu.bme.mit.ftsrg.tpcc.entries;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class ItemsData {

  @Property(schema = {"minimum", "0"})
  int ol_supply_w_id;

  @Property(schema = {"minimum", "0"})
  int ol_i_id;

  @Property(schema = {"maxLength", "24"})
  String i_name;

  @Property int ol_quantity;
  @Property int s_quantity;
  @Property String brand_generic;

  @Property(schema = {"minimum", "0"})
  Double i_price;

  @Property Double ol_amount;

  public ItemsData(
      final int ol_supply_w_id,
      final int ol_i_id,
      final String i_name,
      final int ol_quantity,
      final int s_quantity,
      final String brand_generic,
      final Double i_price,
      final Double ol_amount) {
    this.ol_supply_w_id = ol_supply_w_id;
    this.ol_i_id = ol_i_id;
    this.i_name = i_name;
    this.ol_quantity = ol_quantity;
    this.s_quantity = s_quantity;
    this.brand_generic = brand_generic;
    this.i_price = i_price;
    this.ol_amount = ol_amount;
  }
}
