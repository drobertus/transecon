package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.actors.MarketActor
import com.mechzombie.transecon.actors.Registry
import com.mechzombie.transecon.actors.SupplierActor
import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import spock.lang.Shared
import spock.lang.Specification

class TurnCycle extends Specification {

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

  def "run a turn"() {
    // this may be a challenge as some operations may be blocking and other not

    when:
    def endState = reg.runTurn()

    then:
    println "endState= ${endState}"

    endState.each() {
      assert it.get() != null
      println("result = ${it.get()}")

    }
    when:
    def counter = 0
    while(supplier.turnStatus() != 'complete'){
      println("waiting ${counter+ 20}ms to complete") // ${supplier.turnStatus()}")
      sleep(20)
    }
    then:
    supplier.turnStatus() == 'complete'

  }

}
