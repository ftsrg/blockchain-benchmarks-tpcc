/*
 * SPDX-License-Identifier: Apache-2.0
 */

package main.java.org.example;


@DataType()
public class Stock {
   
    @Property()
    //The ID of the item associated with the stock. Primary key.
    public int s_i_id;

    @Property()
    //The ID of the warehouse associated with the stock. Primary key.
    public int s_w_id;

    @Property()
    //The quantity of the related item.
    public int s_quantity;

    @Property()
    //Information about district 1.
    public String s_dist_01;

    @Property()
    //Information about district 2.
    public String s_dist_02;

    @Property()
    //Information about district 3.
    public String s_dist_03;
   
    @Property()
    //Information about district 4.
    public String s_dist_04;

    @Property()
    //Information about district 5.
    public String s_dist_05;

    @Property()
    //Information about district 6.
    public String s_dist_06;

    @Property()
    //Information about district 7.
    public String s_dist_07;

    @Property()
    //Information about district 8.
    public String s_dist_08;

    @Property()
    //Information about district 9.
    public String s_dist_09;

    @Property()
    //Information about district 10.
    public String s_dist_10;
    
    @Property()
    //The year to date balance of the stock.
    public int s_ytd;

    @Property()
    //The number of orders for the stock.
    public int s_order_cnt;

    @Property()
    //The number of remote orders for the stock.
    public int s_remote_cnt;

    @Property()
    //Stock information.
    public String s_data;

    public stock(){
        
    }

    // public stock(int s_i_id, int s_w_id, int s_quantity, String s_dist_01, String s_dist_02, String s_dist_03, String s_dist_04, 
    // String s_dist_05, String s_dist_06, String s_dist_07, String s_dist_08, String s_dist_09, String s_dist_10, 
    // int s_ytd, int s_order_cnt, int s_remote_cnt, String s_data){

    // }

}
