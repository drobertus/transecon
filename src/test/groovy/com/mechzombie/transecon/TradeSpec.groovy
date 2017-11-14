package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.actors.MarketActor
import com.mechzombie.transecon.actors.Registry
import com.mechzombie.transecon.actors.SupplierActor
import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import spock.lang.Shared
import spock.lang.Specification

class TradeSpec extends Specification{

  def product = 'beanbags'
  def producerPrice = 5

  def consumerLowPrice = 4
  def consumerEvenPrice = 5
  def consumerHighPrice = 6
  def salary = 50

  Registry reg = Registry.instance
  @Shared MarketActor market
  @Shared SupplierActor supplier
  @Shared HouseholdActor household

  def setup() {
    market = new MarketActor(UUID.randomUUID())
    supplier = new SupplierActor(UUID.randomUUID())
    household = new HouseholdActor(UUID.randomUUID())
    supplier.setMoney(500)
    supplier.employHousehold(household.uuid, salary)
  }

  def cleanup() {
    reg.cleanup()
  }

  def tradeTest () {
    when: //the producer ships to a market
    def prom = supplier.shipItem(market, product, producerPrice)

    then: //the market should have 1 item
    prom.get()  == 1
    market.inventory.get(product) != null
    market.inventory.get(product).size() == 1

    when: //the household inspects all markets for that product
    def prices = household.getPrices(product)

    then: //the market should have that product at the expected price
    assert prices.size() == 1
    assert prices.keySet().getAt(0) == market.uuid
    assert prices.get(market.uuid) == producerPrice

    when: //the household purchases that product at price
    def purchase =  reg.messageActor(household.uuid,
        new Message (Command.PURCHASE_ITEM, [product: product, price: consumerEvenPrice, market: market.uuid]))

    then: //the purchase should fail for lack of money
    assert purchase.get() == "NSF"

    when: //the supplier pays labor to the household
    def pay = reg.messageActor(supplier.uuid, new Message(Command.RUN_PAYROLL))
//    //TODO: how is labor/households mapped to suppliers?  How are wages set
//
    then: //
    pay.get() == 'OK'
    household.money == salary
    supplier.money == 500 - salary

    when: //the household purchases at full price
    purchase =  reg.messageActor(household.uuid,
        new Message (Command.PURCHASE_ITEM, [product: product, price: this.consumerEvenPrice, market: market.uuid]))

    then: //the purchase should succeed
    assert purchase.get() == "OK"
    assert household.resources.get(product) == 1

  }
}
