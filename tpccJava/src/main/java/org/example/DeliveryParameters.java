/*
 * SPDX-License-Identifier: Apache-2.0
 */

package main.java.org.example;

public class DeliveryParameters {
    @Property()
    //The warehouse ID.
    public int w_id;

    @Property()
    //The carrier ID for the order.
    public int o_carrier_id;

    @Property()
    //The delivery date of the order.
    public String ol_delivery_d;

    public DeliveryParameters()
    {
        
    }

    // public DeliveryParameters(int w_id, int o_carrier_id, int ol_delivery_d){
    //     this.w_id = w_id;
    //     this.o_carrier_id = o_carrier_id;
    //     this.ol_delivery_d = ol_delivery_d;
    // }

}
