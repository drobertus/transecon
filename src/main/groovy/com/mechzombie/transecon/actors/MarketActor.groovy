package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.resources.Bank
import groovy.util.logging.Slf4j

@Slf4j
class MarketActor extends BaseEconActor {

  def inventory = [:] //products, grouped  by type, ordered  by price.  buy the cheap one first (for now)

  MarketActor(UUID id = UUID.randomUUID()) {
    super(id)
    log.info("Created Market ${id}")
  }

  private def addProduct(supplier, product, price) {
    def shelf = inventory.get(product)
    if(!shelf) {
      shelf = []
      inventory.put(product, shelf)
    }
    shelf << [price, supplier]
    return shelf.size()
  }

  @Override
  def status() {
    builder.econactor {
      type this.class.simpleName
      id this.uuid
      inventory inventory
      money Bank.getAccountValue(this.uuid)
    }
    log.debug "here: ${builder.toString()}"
    return builder.content //.toString()
  }

  @Override
  protected void act() {
    loop {
      react {
        def theResponse
        log.info("market actor ${this.uuid} got message ${it.type} , ${it.vals}")
        switch (it.type) {
          case Command.STOCK_ITEM:

            def params = it.vals
            def supplier = params.producer
            def product = params.product
            def price = params.price

//            def shelf = inventory.get(product)
//            if(!shelf) {
//              shelf = []
//              inventory.put(product, shelf)
//            }
//            shelf << [price, supplier]
            theResponse = addProduct(supplier, product, price) // shelf.size()  //respond to the suplier with the number of items in invtentory
            break

          case Command.FULFILL_ORDER:
            //println("purchase => ${it.vals}")
            def params = it.vals
            def buyer = params.buyer
            def product = params.product
            def price = params.price
            def shelf = inventory.get(product)
            //println ("the shelf = ${shelf}")
            if(shelf) {
              //now compare prices
              for(int i=0; i < shelf.size(); i ++) {
                def prod = shelf.getAt(i)
              //  println("item on shelf= ${prod}")
                if (prod[0] <= price) {
                  //we can purchase

                  def margin = price - prod[0]
                  Bank.deposit(this.uuid, margin)
                //  this.money += margin
                  //message the producer with the amount, keep the difference
                  def producer = prod[1]
                  def send = this.sendMoney(producer, (int)prod[0], 'sale')
                  println("---- sendMoney ${send}")
                  if(send) {
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
          case Command.TAKE_TURN:
            theResponse = 'hhrutn.'
            break
          case Command.PRICE_ITEM:
            def product = it.vals.product
            def shelf = this.inventory.get(product)
            def bestPrice
          //  println("found shelf ${shelf} for product ${product}")
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
          case Command.STATUS:
            theResponse = status()
            break
          default:
            theResponse = "unrecognized Command"
            break
        }

        log.info("actor: ${uuid}, theResponse: ${theResponse}")
        reply theResponse
      }
    }
  }

  @Override
  def clear() {
    inventory.clear()
  }
}
