package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.actors.MarketActor
import com.mechzombie.transecon.actors.Registry
import com.mechzombie.transecon.actors.SupplierActor
import spock.lang.Shared
import spock.lang.Specification

class SystemStatus extends Specification{

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
    household.setMoney(35)
    household.getResources().put('food', 8)
    household.getResources().put('housing', 3)
  }

  def cleanup() {
    reg.cleanup()
  }

  def getCompleteSystemState() {


    def sysState = reg.getSystemState()


  }


}
