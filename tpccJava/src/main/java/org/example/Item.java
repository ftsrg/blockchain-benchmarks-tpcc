package main.java.org.example;

public class Item {

    public Item(){

    }

    @Property()
    //The ID of the item. Primary key.
    public int i_id;


    @Property()
    //The image ID associated with the item.
    public int i_im_id;


    @Property()
    //The name of the item.
    public String i_name;


    @Property()
    //The price of the item.
    public Double i_price;


    @Property()
    //Brand information.
    public String i_data;


}
