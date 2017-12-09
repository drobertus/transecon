package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.messages.Purchase
import com.mechzombie.transecon.resources.Bank
import com.mechzombie.transecon.resources.Shelf
import groovy.util.logging.Slf4j

@Slf4j
class MarketActor extends BaseEconActor {

  Map<String, Shelf> inventory = [:] //products, grouped  by type, ordered  by price.  buy the cheap one first (for now)
  MarketActor(UUID id = UUID.randomUUID()) {
    super(id)
    log.info("Created Market ${id}")
  }

  private def shelveProduct(UUID supplier, String prodName, Double price, Integer quantity) {

    Shelf shelf = inventory.get(prodName)
    if(!shelf) {
      println("creating new shelf for ${prodName}")

      shelf = new Shelf(prodName)
      inventory.put(prodName, shelf)
    }

    return shelf.addToShelf(supplier, quantity, price)
  }

  @Override
  def status() {
    builder.econactor {
      type this.class.simpleName
      id this.uuid
      inventory (this.inventory.collect { shelf ->
        return {
          product shelf.key
          count  shelf.value.availableCount
        }
      })
      money Bank.getAccountValue(this.uuid)
    }
    log.debug "here: ${builder.toString()}"
    return builder.content
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
            def quantity = params.quantity

//            Shelf shelf = inventory.get(product)

            //addToShelf

            theResponse = shelveProduct(supplier, product, price, quantity) // shelf.size()  //respond to the suplier with the number of items in invtentory
            break

          case Command.FULFILL_ORDER:
            //println("purchase => ${it.vals}")
            def params = it.vals
            def buyer = params.buyer
            def product = params.product
            def price = params.price
            //TODO: add count to purchase
            def count = params.count ? params.count : 1

            def shelf = inventory.get(product)
            println ("the shelf = ${shelf}")
            if(shelf) {

              //map of producer: <price: count>
              Purchase bought = shelf.buyAtPrice(price, count)

              log.info("bought= ${bought.totalBought}, price = ${price}, count= ${count}")
              if (bought.totalBought > 0 ){

                bought.getSuppliersAndAmountsToPay().each {source, amt ->
                  Bank.transferFunds(buyer, source, amt)
                }
                //get money from the buyer UUID

                //send money to the various producers

                //in future this may be changed to active purchasing and selling
                //for now it will be a clearing house
                theResponse = 'OK'
              }
//              //now compare prices
//              for(int i=0; i < shelf; i ++) {
//                def prod = shelf.getAt(i)
//              //  println("item on shelf= ${prod}")
//                if (prod[0] <= price) {
//                  //we can purchase
//
//                  def margin = price - prod[0]
//                  Bank.deposit(this.uuid, margin)
//                //  this.money += margin
//                  //message the producer with the amount, keep the difference
//                  def producer = prod[1]
//                  def send = this.sendMoney(producer, (int)prod[0], 'sale')
//                  //println("---- sendMoney ${send}")
//                  if(send) {
//                    theResponse = "OK"
//                    shelf.remove(i)
//                    break
//                  }else {
//                    theResponse = 'reimbursment to supplier failed'
//                  }
//
//                }
//                else {
//                  theResponse = "NSF"
//                }
//              }

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
            def bestPrice = shelf.getBestPrice()
          //  println("found shelf ${shelf} for product ${product}")
//            if(shelf) {
//
//              shelf.each {
//                def aPrice = it[0]
//                if(!bestPrice || aPrice < bestPrice) {
//                  bestPrice = aPrice
//                }
//              }
              // find the best price available
              theResponse = bestPrice
      //      }

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
