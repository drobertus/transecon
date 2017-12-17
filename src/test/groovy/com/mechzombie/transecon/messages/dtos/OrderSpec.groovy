package com.mechzombie.transecon.messages.dtos

import com.mechzombie.transecon.messages.dtos.Order
import com.mechzombie.transecon.resources.AccountLock
import spock.lang.Specification

class OrderSpec extends Specification{


//TODO: consider adding per unit price caps (experimentally) to simulate consumer
  // behavior for when multiple markets are submitted to

   /*

   For now the Order will be an attempt to optomise in order andnumber of markets visited.

   If we assume a high trip transaction cost this is a probable outcome
   or possible optiization
   strategy

    */




  def "create a new order" () {

    when:
    def o = new Order()

    then:
    o.getAccountLock() == null
    o.getFulfilledItems() == [:]
    o.getTotalSpentInFulfillment() == 0.0d
    o.getOrderItemsRemaining() == [:]
  }

  def "Attempt a purchase with no money" () {

    setup:
    def tobeBought = [paper: 3, spoons: 5]
    def o = new Order(orderItemsRemaining: tobeBought)

    when: 'we attempt to fulfill an order with no funds and a price'
    def canFulfill = o.fulfillItem('paper', 13)

    then: 'the order should not be fulfilled'
    canFulfill == false
    // money adds up
    o.accountLock == null
    o.getTotalSpentInFulfillment() == 0.0
    // items add up
    o.getFulfilledItems() == [:]
    o.orderItemsRemaining == [paper: 3, spoons: 5]
  }

  def "Make a purchase with enough money" () {

    setup:
    def tobeBought = [paper: 3, spoons: 5]
    AccountLock lock = new AccountLock(null)
    lock.amount = 100.0
    def o = new Order(orderItemsRemaining: tobeBought, accountLock: lock)

    when: 'we attempt to fulfill an order with no funds and a price'
    def canFulfill = o.fulfillItem('paper', 13)

    then: "the order is partially fulfilled"
    canFulfill
    o.accountLock.amount == 100
    o.getTotalSpentInFulfillment() == 13.0
    o.getFulfilledItems() == [paper: 1]
    o.orderItemsRemaining == [paper: 2, spoons: 5]
  }

  def "When we reach a limit to the ability to purchase"() {

    setup:
    def tobeBought = [paper: 2]
    AccountLock lock = new AccountLock(null)
    lock.amount = 20.0
    def o = new Order(orderItemsRemaining: tobeBought, accountLock: lock)

    when: 'we attempt to fulfill an order with no funds and a price'
    def canFulfill1 = o.fulfillItem('paper', 13)
    def canFulfill2 = o.fulfillItem('paper', 13)

    then: "the order is partially fulfilled"
    canFulfill1
    !canFulfill2

    o.accountLock.amount == 20
    o.getTotalSpentInFulfillment() == 13.0
    o.getFulfilledItems() == [paper: 1]
    o.orderItemsRemaining == [paper: 1]
  }

}
