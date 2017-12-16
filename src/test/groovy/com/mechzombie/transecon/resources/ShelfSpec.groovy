package com.mechzombie.transecon.resources

import com.mechzombie.transecon.messages.Purchase
import spock.lang.Specification

class ShelfSpec extends Specification {

  Shelf shelf
  def product = 'gnomes'
  def producer1 = UUID.randomUUID()
  def price1 = 8.0d
  def producer2 = UUID.randomUUID()
  def price2 = 10.0d

  def consumerPrice = 9

  def setup() {
    shelf = new Shelf(product)
  }

  def "create a shelf and then get something off it" () {
    when:
    def added = shelf.addToShelf(producer1, 2, price1)

    then:
    assert added == 2

    when:
    def bestPrice = shelf.bestPrice

    then:
    bestPrice == price1

    when:
    Purchase purchasedHigh = shelf.buyAtPrice(consumerPrice, 2)
    def purchased = purchasedHigh.bought.get(producer1)

    then:
    assert purchasedHigh.bought.size() == 1
    assert purchased.get(price1) == 2
    assert purchasedHigh.countBought() == 2
    assert purchasedHigh.totalSpent() == price1 * 2

  }

  def "multiple suppliers at different prices"() {
    when:
    shelf.addToShelf(producer2, 3, price2)
    def bestPrice = shelf.bestPrice

    then:
    assert bestPrice == 10.0

    when:
    shelf.addToShelf(producer1, 2, price1)
    bestPrice = shelf.bestPrice

    then:
    bestPrice == price1

    when:
    Purchase purchasedHigh = shelf.buyAtPrice(consumerPrice, 4)
    def purchased = purchasedHigh.bought.get(producer1)

    then:
    assert purchasedHigh.bought.size() == 1
    assert purchased.get(price1) == 2
    assert purchasedHigh.countBought() == 2
    assert purchasedHigh.totalSpent() == price1 * 2
  }


  def "getAtBestPrice and removeFromShelf"() {
    shelf.addToShelf(producer2, 3, 12)
    shelf.addToShelf(producer1, 2, 11)

    when: "we getAtBestPrice"
    def bestPriced = shelf.getAtBestPrice()
    then: "we should get a supplier and thier best price"
    assert bestPriced == [supplier: producer1, price: 11]

    when: "we takeOffShelf for that supplier/price"
    def removed = shelf.takeOffShelf(bestPriced.supplier, bestPriced.price)

    then:
    assert removed
    shelf.byPrice.get(11.0d).get(producer1) == 1
    shelf.byPrice.get(12.0d).get(producer2) == 3

    when: "we try and remove a non-existint price/supplier combo"
    removed = shelf.takeOffShelf(producer2, 11.0)

    then:
    assert !removed
    shelf.byPrice.get(11.0d).get(producer1) == 1
    shelf.byPrice.get(12.0d).get(producer2) == 3

  }


}
