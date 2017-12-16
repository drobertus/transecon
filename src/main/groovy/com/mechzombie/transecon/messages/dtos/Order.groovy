package com.mechzombie.transecon.messages.dtos

import groovy.transform.CompileStatic

@CompileStatic
class Order {
  /**
   * The address of the customer
   */
  UUID customer
  /**
   * The address of the "retailer"
   */
  UUID market

  /**
   * An amount that is set aside for this purchase
   *
   *  It cuold be a double for the amount available OR
   *  could be (or in addition) an AccountLock,
   */
  double budgetedAmount = 0.0

  /**
   * Map of the the requested items to types
   */
  Map<String, Integer> orderItemsRemaining = [:]

  //remove items from the order as a market fulfills it

  Map<String, Integer> fulfilledItems = [:]

  double totalSpentInFulfillment = 0.0


  boolean fulfillItem (String product, double amount){
    if(budgetedAmount >= (totalSpentInFulfillment + amount) ) {

      //TODO:

      def remaining = orderItemsRemaining.get(product)
      if (remaining) {
        //remove an order item, delete a key if reaches 0

        remaining = remaining - 1
        if ( remaining == 0) {
          orderItemsRemaining.remove(product)
        } else {
          orderItemsRemaining.put(product, remaining)
        }
        //add a fulfilledItem
        def count = fulfilledItems.get(product)
        if (!count){
          count = 0
        }
        fulfilledItems.put(product, count + 1)

        //increment the totalSpent
        totalSpentInFulfillment += amount

        return true
      }
      else {
        false
      }
    }
    else {
      return false
    }
  }
}
