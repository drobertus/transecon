package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import groovy.json.JsonBuilder

class HouseholdActor extends BaseEconActor {

  def demands = [:] //this that the household needs per unit time
  def resources = [:] //this owned by or produced by the household per unit time


  HouseholdActor(UUID id) {
    super(id)
    println("id ${id}")
  }

  @Override
  def status() {
    println('getting status')
    def status
    try {
      status = builder.econactor {
        type this.class
        id this.uuid
        requirements this.demands
        resources this.resources
        transactions this.transactions
      }
    }
      catch(Exception e) {
      println "err ${e}"
      }

    /*
        builder.econactor {
      type this.class
      id this.uuid
      employed employed
    }
    */
    println "here: ${status.toString()}"
    return status.toString()
  }

  @Override
  protected void act() {
    loop {

      react {
        def theResponse
        println "it  ${it}"
        switch (it.type) {
          case String:
            theResponse = "Woohoo!!"
            break
          case Command.STATUS:
            theResponse = status()
            println "status = ${theResponse}"
            break
          default:
            theResponse = "message not understood '${it}'"
            break
        }
        reply theResponse
      }
    }
  }

  def getPrices(product) {

    //make calls to all markets and get prices
    def prices = [:]
    reg.getMarkets().each {
      println("sending getPrice  to ${it.uuid}")
      def priceProm = it.sendAndPromise(new Message(Command.PRICE_ITEM, [product: product]));
      prices.put(it.uuid, priceProm)
    }
    prices.each { k, v ->
      def aPrice = v.get()
      println("price found of ${aPrice}")
      prices.put(k, aPrice)
    }
    return prices
  }
}
