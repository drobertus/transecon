package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.resources.Bank
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
            //println ("the shelf = ${shelf}")
            if(shelf) {

              int bought = shelf.buyAtPrice(price, count)

              println("bought= ${bought}")

              //now compare prices
              for(int i=0; i < shelf; i ++) {
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
                  //println("---- sendMoney ${send}")
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

  private class Shelf {

    def product

    Map<UUID, Map<Double, Integer>> suppliers = [:]
    Map<Double, Map<UUID, Integer>> byPrice = [:]

    Shelf(product){
      this.product = product
    }

    /**
     * get up to the count at the set price (average price or total?)
     * @param price - per unit price to buy at
     * @param count - number of units to buy
     */
    def buyAtPrice(price, count) {
      def totalFunds = price * count

      byPrice.sort{ key, val-> key}
      println ("sorted by price = ${byPrice}")
      def toBuy = count
      for(def obj : byPrice) {

        def unitPrice = obj.key
        if(unitPrice < price) {
          def units = obj.value
          for(def supplier : units) {
            if (supplier.value > 0 && toBuy >  0) {
              if(toBuy >= supplier.value)
              def bought = supplier.value
              units.remove(supplier)
              toBuy = toBuy - bought
            }
          }
        }
        //stop conditions - out of need or increasing unit price exceeds available funds
        if(toBuy == count) {
          break
        }
      }
      //return the number bought
      return count - toBuy
    }

    def addToShelf(UUID supplier, int quantity, double price) {
    //  println("adding ${quantity} at ${price} from ${supplier}")
      def source = suppliers.get(supplier)
      if(!source) {
        source = [:]
        suppliers.put(supplier, source)
      //  println("created source for ${supplier}")
      }
      def priced = source.get(price)
     // println("priced = ${priced}")
      if (!priced) {
        source.put(price, quantity)
      } else {
        source.put(price, priced + quantity)
      }

      //now update teh other map
      def atPrice = byPrice.get(price)
      if (!atPrice) {
        atPrice = [:]
        byPrice.put(price, atPrice)
      }
      def bySupplier = atPrice.get(supplier)
      if(!bySupplier) {
        atPrice.put(supplier, quantity)
      } else {
        atPrice.put(supplier, bySupplier + quantity)
      }

      return quantity
    }

    def getBestPrice() {
      def bestPrice
      this.byPrice.each { key, value ->
        if(bestPrice == null || bestPrice > key) {
          bestPrice = key
        }
      }
      return bestPrice
    }

    def getAvailableCount(){
      Integer total = 0
      suppliers.each { k, value ->
        value.values().toArray().every {
          total = total + it
        }
      }
      return total
    }
  }
}
