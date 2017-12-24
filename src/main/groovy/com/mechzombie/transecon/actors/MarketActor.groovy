package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.messages.Purchase
import com.mechzombie.transecon.messages.dtos.Order
import com.mechzombie.transecon.resources.Bank
import com.mechzombie.transecon.resources.Shelf
import groovy.json.JsonBuilder
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
     // println("creating new shelf for ${prodName}")

      shelf = new Shelf(prodName)
      inventory.put(prodName, shelf)
    }

    return shelf.addToShelf(supplier, quantity, price)
  }

  @Override
  String status() {
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
    return builder.writer.toString()
  }

  @Override
  def asJson() {
    JsonBuilder jb = new JsonBuilder()
    jb.econactor {
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
    //log.debug "here: ${builder.toString()}"
    return jb.content
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

            theResponse = shelveProduct(supplier, product, price, quantity) // shelf.size()  //respond to the suplier with the number of items in invtentory
            break

          case Command.FULFILL_ORDER:
            //println("purchase => ${it.vals}")
            def params = it.vals
           // def buyer = params.buyer
            Order order = params[0]
            ///def price = params.price
            //TODO: add count to purchase
           // def count = params.count ? params.count : 1

            def paymentsToProducers = [:]

            order.orderItemsRemaining.each { product, count ->
              def shelf = inventory.get(product)
             // println("the shelf = ${shelf}")
              if (shelf) {
                // this market can purchase these items
                def soldItem
                count.times {
                 soldItem = shelf.getAtBestPrice()
                 //sold items allows us to credit a producer
                  def supp = paymentsToProducers.get(soldItem.supplier)
                  if (!supp) {
                    paymentsToProducers.put(soldItem.supplier, soldItem.price)
                  }
                  else {
                    paymentsToProducers.put(soldItem.supplier, soldItem.price + supp)
                  }
                  shelf.takeOffShelf(soldItem.supplier, soldItem.price)
                  order.fulfillItem(product, soldItem.price)
                }
              }
            }

//          println("fulfill order! ${order}")
            theResponse = order

            break
          case Command.TAKE_TURN:
            theResponse = 'hhrutn.'
            break
          case Command.PRICE_ITEM:
            def product = it.vals.product
            def shelf = this.inventory.get(product)
            if(shelf){
              def bestPrice = shelf.getBestPrice()

              theResponse = bestPrice
            } else {
              theResponse = -1.0
            }
            break
          case Command.AS_JSON:
            theResponse = asJson()
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
