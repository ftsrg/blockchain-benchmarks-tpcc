/*
 * SPDX-License-Identifier: Apache-2.0
 */

package main.java.org.example;
 
public class StockLevelParameters {
    @Property()
    //The warehouse ID.
    public int w_id;
 
    @Property()
    //The district ID.
    public int d_id;
 
    @Property()
    //The threshold of minimum quantity in stock to report.
    public int threshold;

    public StockLevelParameters(){
        
    }
}
