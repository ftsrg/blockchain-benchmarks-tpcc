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

}
