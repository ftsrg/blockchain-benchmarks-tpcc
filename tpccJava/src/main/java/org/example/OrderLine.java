package main.java.org.example;

import java.util.Date;

public class OrderLine {
    
    public OrderLine(){

    }


    @Property()
    //The order ID associated with the order line. Primary key.
    public int ol_o_id;

    @Property()
    //The district ID associated with the order line. Primary key.
    public int ol_d_id;

    @Property()
    //The warehouse ID associated with the order line. Primary key.
    public int ol_w_id;
    
    @Property()
    //The number/position/index of the order line. Primary key.
    public int ol_number;

    @Property()
    //The item ID associated with the order line.
    public int ol_i_id;

    @Property()
    //The ID of the supplying warehouse.
    public int ol_supply_w_id;

    @Property()
    //The date of delivery.
    public Date ol_delivery_d;

    @Property()
    //The quantity of items in the order line.
    public int ol_quantity;

    @Property()
    //The amount to pay.
    public Double ol_amount;

    @Property()
    //Information about the district.
    public String ol_dist_info;


}
