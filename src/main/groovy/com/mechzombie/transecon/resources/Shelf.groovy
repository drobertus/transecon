package com.mechzombie.transecon.resources

import com.mechzombie.transecon.messages.Purchase
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
class Shelf {

  String product

  Map<Double, Map<UUID, Integer>> byPrice = [:]

  Shelf(product){
    this.product = product
  }

  /**
   * get up to the count at the set price (average price or total?)
   * @param price - per unit price to buy at
   * @param count - number of units to buy
   */
  Purchase buyAtPrice(price, count) {
    def totalFunds = price * count

    def bought = new Purchase(product: this.product)

    def sorted = byPrice.keySet().sort()//.sort{ key, val-> key}
   // log.info ("sorted by price = ${sorted} ${price} ${count}")
    def toBuy = count
    for(def unitPrice : sorted) {
     // log.info('unitprice=' + unitPrice)
      if(unitPrice <= price) {
        Map<UUID, Integer> units = byPrice.get(unitPrice) //.value
       // log.info "units= ${units}"
        for(def supplier : units) {
          //log.info("supplier ${supplier} ${supplier.value} ${toBuy}")
          if (supplier.value > 0 && toBuy >  0) {


            if(toBuy >= supplier.value) {
             // log.info("we will buy!")
              bought.addCountFromSupplier(supplier.key, unitPrice, supplier.value)
              units.remove(supplier)
            //  log.info("suppler=${supplier}")
              toBuy = toBuy - supplier.value
            //  log.info("toBuy=${toBuy}")
            } else {
              bought.addCountFromSupplier(supplier.key, unitPrice, toBuy)
             // log.info("remainig= ${supplier.value} - ${toBuy}")
              units.put(supplier.key, (supplier.value - toBuy))
              toBuy = 0
            }
          }
        }
      }
      //stop conditions - out of need or increasing unit price exceeds available funds
      if(toBuy == 0) {
        log.info('bought all we need!')
        break
      }
    }
    //return the number bought
    return bought //count - toBuy
  }

  def addToShelf(UUID supplier, int quantity, double price) {
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

  def getAtBestPrice() {

    def bestPrice = [supplier: null, price: null]
    this.byPrice.each { key, value ->
      println("key= ${key}, val= ${value}")
      if(bestPrice.price == null || bestPrice.price > key) {
        bestPrice.price = key
        bestPrice.supplier = value.keySet().getAt(0)
      }
    }
    println("best price for ${product} is ${bestPrice}")
    return bestPrice
  }


  def getBestPrice() {
    if(this.byPrice.size() > 0) {
      return this.byPrice.keySet().sort().get(0)
    }
    return null
  }

  def takeOffShelf(UUID supplier, price) {
    def removed = false
    def supplierMap = byPrice.get(price)
    if(supplierMap) {
      def theSupplierItemCount = supplierMap.get(supplier)
      if (theSupplierItemCount) {
        theSupplierItemCount -- //= theSupplierItemCount - 1
        if (theSupplierItemCount == 0) {
          supplierMap.remove(price)
        } else {
          supplierMap.put(supplier, theSupplierItemCount)
        }
        removed = true
      }
    }

    removed
  }


  def getAvailableCount(){
    Integer total = 0
    byPrice.each { k, value ->
      value.values().toArray().every {
        total =+ it
      }
    }
    return total
  }
}
