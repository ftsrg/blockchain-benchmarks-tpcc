/*
* SPDX-License-Identifier: Apache-2.0
*/

package main.java.org.example;

public class Warehouse {
    @Property()
    //The warehouse ID. Primary key.
    public int w_id;

    @Property()
    //The name of the warehouse.
    public String w_name;

    @Property()
    //The first street name of the warehouse.
    public String w_street_1;

    @Property()
    //The second street name of the warehouse.
    public String w_street_2;

    @Property()
    //The city of the warehouse.
    public String w_city;

    @Property()
    //The state of the warehouse.
    public String w_state;

    @Property()
    //The ZIP code of the warehouse.
    public String w_zip;

    @Property()
    //The sales tax of the warehouse.
    public int w_tax;

    @Property()
    //The year to date balance of the warehouse.
    public int w_ytd;

    public Warehouse(){

    }

}
