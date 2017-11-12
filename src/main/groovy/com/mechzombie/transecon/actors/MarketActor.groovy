package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message

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

          case Command.FULFILL_ORDER:
            println("purchase => ${it.vals}")
            def params = it.vals
            def buyer = params.buyer
            def product = params.product
            def price = params.price
            def shelf = inventory.get(product)
            println ("the shelf = ${shelf}")
            if(shelf) {
              //now compare prices
              for(int i=0; i < shelf.size(); i ++) {
                def prod = shelf.getAt(i)
                println("item on shelf= ${prod}")
                if (prod[0] <= price) {
                  //we can purchase

                  def margin = price - prod[0]
                  this.money += margin
                  //message the producer with the amount, keep the difference
                  def producer = prod[1]
                  def send = reg.messageActor(producer, new Message(Command.SEND_MONEY, [from: this.uuid, amount: (int)prod[0], reason:'sale']))
                  if(send.get() == 'OK') {
                    theResponse = "OK"
                    shelf.remove(i)
                    break
                  }else {
                    theResponse = 'reimbursment to supplier failed'
                  }

                }
                else {
                  theResponse = "NSF"
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
