package main.java.org.example;

public class NewOrder {
    @Property()
    //The order ID. Primary key.
    public int no_o_id;


    @Property()
    //The district ID associated with the order. Primary key.
    public int no_d_id;


    @Property()
    //The warehouse ID associated with the order. Primary key.
    public int no_w_id;

    public NewOrder(){

    }
}
