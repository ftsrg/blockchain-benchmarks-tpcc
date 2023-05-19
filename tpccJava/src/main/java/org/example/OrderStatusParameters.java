/*
 * SPDX-License-Identifier: Apache-2.0
 */

package main.java.org.example;

public class OrderStatusParameters {
   
   @Property()
   //The warehouse ID.
   public int w_id;

   @Property()
   //The district ID.
   public int d_id;

   @Property()
   //The customer ID if provided.
   public int c_id;


   @Property()
   //The last name of the customer if provided.
   public String c_last;


   public OrderStatusParameters(){
      
   }

   // public OrderStatusParameters(int w_id, int d_id, int c_id, String c_last){
   //    this.w_id = w_id;
   //    this.d_id = d_id;
   //    this.c_id = c_id;
   //    this.c_last = c_last;
   // }    
 
 }