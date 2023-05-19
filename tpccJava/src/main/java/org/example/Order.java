package main.java.org.example;

public class Order {
    @Property() 
    //The order ID. Primary key.
    public int o_id;

    @Property()
    // The district ID associated with the order. Primary key.
    public int o_d_id;

    @Property() 
    //The warehouse ID associated with the order. Primary key.
    public int o_w_id;

    @Property() 
    //The customer ID associated with the order.
    public int o_c_id;

    @Property() 
    //The date when the order was submitted.
    public String o_entry_d;

    @Property() 
    //The carrier ID associated with the order.
    public int o_carrier_id;

    @Property() 
    //The number of lines in the order.
    public int o_ol_cnt;

    @Property()
    // 1 if every order lines is local, otherwise 0.
    public int o_all_local;


    public Order(){
        
    }

}
