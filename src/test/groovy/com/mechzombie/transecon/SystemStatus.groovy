package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.actors.MarketActor
import com.mechzombie.transecon.actors.Registry
import com.mechzombie.transecon.actors.SupplierActor
import spock.lang.Shared
import spock.lang.Specification

class SystemStatus extends BaseActorTest {

  def product = 'beanbags'
  def producerPrice = 5

  def salary = 50

  @Shared MarketActor market
  @Shared SupplierActor supplier
  @Shared HouseholdActor household

  def setup() {
    market = new MarketActor(UUID.randomUUID())
    supplier = new SupplierActor(UUID.randomUUID(), 'monkey')
    household = new HouseholdActor(UUID.randomUUID())
    supplier.setMoney(500)
    supplier.employHousehold(household.uuid, salary)
    household.setMoney(35)
    household.getResources().put('food', 8)
    household.getResources().put('housing', 3)
    market.addProduct(supplier.uuid, product, producerPrice)
  }

  def "get Complete System State"() {
    when:
    def sysState = reg.getSystemState()
    println sysState

    then:
    sysState == "[system:[houseHolds:[[econactor:[type:class com.mechzombie.transecon.actors.HouseholdActor, id:${household.uuid}, requirements:[:], resources:[food:8, housing:3], money:35]]], systemMarkets:[[econactor:[type:class com.mechzombie.transecon.actors.MarketActor, id:${market.uuid}, inventory:[beanbags:[[5, ${supplier.uuid}]]], money:0]]], theSuppliers:[[econactor:[type:class com.mechzombie.transecon.actors.SupplierActor, id:${supplier.uuid}, perUnitInputs:[:], resources:[:], employees:[${household.uuid}:50], money:500]]]]]"
  }


}
