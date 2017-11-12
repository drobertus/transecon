package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command

class MarketActor extends BaseEconActor {

  def inventory = [:] //products, grouped  by type, ordered  by price.  buy the cheap one first (for now)
  def money = 0

  MarketActor(UUID id) {
    super(id)
    println("id ${id}")
  }

  @Override
  def status() {
    def status = builder.econactor {
      type this.class
      id this.uuid
      inventory inventory
      money money
      result result
    }
    println "here: ${status.toString()}"
    return status.toString()
  }

  @Override
  protected void act() {
    loop {
      react {
        def theResponse
        println("market actor ${this.uuid} got message ${it.type} , ${it.vals}")
        switch (it.type) {
          case Command.STOCK_ITEM:

            def params = it.vals
            def supplier = params.producer
            def product = params.product
            def price = params.price
            def shelf = inventory.get(product)
            if(!shelf) {
              shelf = []
              inventory.put(product, shelf)
            }
            shelf << [price, supplier]
            theResponse = shelf.size()  //respond to the suplier with the number of items in invtentory
            break

          case Command.PURCHASE_ITEM:
            def params = it.vals
            def buyer = params[0]
            def product = params[1]
            def price = params[2]
            def shelf = inventory.get(product)
            if(shelf) {
              //now compare prices
              shelf.each {
                if (it[0] <= price) {
                  //we can purchase
                  shelf.removeElement(it)
                  theResponse(product)
                  def margin = price - it[0]
                  this.money += margin
                  //message the producer with the amount, keep the difference
                  def producer = it[1]
                  reg.messageActor(producer, Command.SEND_MONEY, [producer: producer, amount: it[0]])
                }
              }

            }
            else {
              theResponse = '404'
            }
            break
          case Command.PRICE_ITEM:
            def product = it.vals.product
            def shelf = this.inventory.get(product)
            def bestPrice
            println("found shelf ${shelf} for product ${product}")
            if(shelf) {

              shelf.each {
                def aPrice = it[0]
                if(!bestPrice || aPrice < bestPrice) {
                  bestPrice = aPrice
                }
              }
              // find the best price available
              theResponse = bestPrice
            }

            break
          case 'status':
            theResponse = status()
            break
          default:
            theResponse = "unrecognized Command"
            break
        }

        reply theResponse
      }
    }
  }
}
