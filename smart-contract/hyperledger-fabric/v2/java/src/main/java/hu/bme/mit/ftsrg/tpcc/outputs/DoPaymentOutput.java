package hu.bme.mit.ftsrg.tpcc.outputs;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class DoPaymentOutput {

  @Property(schema = {"minimum", "0"})
  public int w_id;

  @Property(schema = {"minimum", "0"})
  public int d_id;

  @Property(schema = {"minimum", "0"})
  public int c_id;

  @Property(schema = {"minimum", "0"})
  public int c_d_id;

  @Property(schema = {"minimum", "0"})
  public int c_w_id;

  @Property public Double h_amount;
  @Property public String h_date;

  @Property(schema = {"maxLength", "20"})
  public String w_street_1;

  @Property(schema = {"maxLength", "20"})
  public String w_street_2;

  @Property(schema = {"maxLength", "20"})
  public String w_city;

  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  public String w_state;

  @Property(schema = {"pattern", "[0-9]{4}1111"})
  public String w_zip;

  @Property(schema = {"maxLength", "20"})
  public String d_street_1;

  @Property(schema = {"maxLength", "20"})
  public String d_street_2;

  @Property(schema = {"maxLength", "20"})
  public String d_city;

  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  public String d_state;

  @Property(schema = {"pattern", "[0-9]{4}1111"})
  public String d_zip;

  @Property(schema = {"maxLength", "16"})
  public String c_first;

  @Property(schema = {"minLength", "2", "maxLength", "2"})
  public String c_middle;

  @Property(schema = {"maxLength", "16"})
  public String c_last;

  @Property(schema = {"maxLength", "20"})
  public String c_street_1;

  @Property(schema = {"maxLength", "20"})
  public String c_street_2;

  @Property(schema = {"maxLength", "20"})
  public String c_city;

  @Property(schema = {"pattern", "[a-zA-Z]{2}"})
  public String c_state;

  @Property(schema = {"pattern", "[0-9]{4}1111"})
  public String c_zip;

  @Property(schema = {"minLength", "16", "maxLength", "16"})
  public String c_phone;

  @Property public String c_since;

  @Property(schema = {"pattern", "[GB]C"})
  public String c_credit;

  @Property public int c_credit_lim;
  @Property public Double c_discount;
  @Property public Double c_balance;

  @Property(schema = {"maxLength", "500"})
  public String c_data;
}
