/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.example;

import org.hyperledger.fabric.contract.annotation.Property;

public class History {

    @Property()
    //The customer ID. Primary key.
    public int h_c_id;


    @Property()
    //The district ID associated with the customer. Primary key.
    public int h_c_d_id;


    @Property()
    //The warehouse ID associated with the customer. Primary key.
    public int h_c_w_id;


    @Property()
    //The district ID.
    public int h_d_id;

    
    @Property()
    //The warehouse ID.
    public int h_w_id;


    @Property()
    //The date for the history. Primary key.
    public String h_date;


    @Property()
    //The amount of payment.
    public Double h_amount;


    @Property()
    //Arbitrary information.
    public String h_data;

    public History(){
        
    }

}
