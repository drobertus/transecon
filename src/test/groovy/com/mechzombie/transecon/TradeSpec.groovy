package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.actors.MarketActor
import com.mechzombie.transecon.actors.Registry
import com.mechzombie.transecon.actors.SupplierActor
import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.resources.Bank
import spock.lang.Shared
import spock.lang.Specification

class TradeSpec extends BaseActorTest {

  def product = 'beanbags'
  def producerPrice = 5

  def consumerLowPrice = 4
  def consumerEvenPrice = 5
  def consumerHighPrice = 6
  def salary = 50

  @Shared MarketActor market
  @Shared SupplierActor supplier
  @Shared HouseholdActor household

  def setup() {
    market = new MarketActor()
    supplier = new SupplierActor( product)
    household = new HouseholdActor()
    Bank.deposit(supplier.uuid, 500)
    supplier.employHousehold(household.uuid, salary)
  }

  def tradeTest () {
    when: //the producer ships to a market
    def prom = supplier.shipItem(market, product, producerPrice, 4)

    then: //the market should have 1 item
    prom.get()  == 4
    market.inventory.get(product).getAvailableCount() == 4
    Bank.getAccountValue( household.uuid) == 0
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
    household.getBankBalance() == salary
    supplier.getBankBalance() == 500 - salary

    when: //the household purchases at full price
    purchase =  reg.messageActor(household.uuid,
        new Message (Command.PURCHASE_ITEM, [product: product, price: this.consumerEvenPrice, market: market.uuid]))

    then: //the purchase should succeed
    assert purchase.get() == "OK"
    assert household.resources.get(product) == 1
    assert market.inventory.get(product).getAvailableCount() == 3
    assert household.getBankBalance() == salary - this.consumerEvenPrice

  }
}
