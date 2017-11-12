package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.actors.MarketActor
import com.mechzombie.transecon.actors.Registry
import com.mechzombie.transecon.actors.SupplierActor
import spock.lang.Specification

class TradeSpec extends Specification{

  def product = 'beanbags'
  def producerPrice = 5
  def consumerPrice = 6

  def tradeTest () {
    //create a household, market and supplier
    //supply a product
    //purchase a product
    when:
    MarketActor market = new MarketActor(UUID.randomUUID())
    SupplierActor supplier = new SupplierActor(UUID.randomUUID())
    HouseholdActor household = new HouseholdActor(UUID.randomUUID())

    then:
    Registry.instance.actors.size() == 3

    when:
    def prom = supplier.shipItem(market, product, producerPrice)

    then:
    prom.get()  == 1
    market.inventory.get(product) != null
    market.inventory.get(product).size() == 1

    when:
    def prices = household.getPrices(product)
    then:
    assert prices.size() == 1
    assert prices.keySet().getAt(0) == market.uuid
    assert prices.get(market.uuid) == producerPrice

  }
}
