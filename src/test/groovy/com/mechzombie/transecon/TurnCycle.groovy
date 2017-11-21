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

  def product = 'food'
  def producerPrice = 5

  def consumerLowPrice = 4
  def consumerEvenPrice = 5
  def consumerHighPrice = 6
  def salary = 50

  Registry reg = Registry.instance
  @Shared MarketActor market
  @Shared SupplierActor supplier
  @Shared HouseholdActor household

  Map hhDemand = [food: 2, housing: 3]
  Map hhResources = [food:1]

  def inputsPerUnit = [iron: 0.1, corn: 1, labor: 1]

  def setup() {
    market = new MarketActor(UUID.randomUUID())

    household = new HouseholdActor(UUID.randomUUID(), hhDemand, hhResources)
    supplier = new SupplierActor(UUID.randomUUID(), product, inputsPerUnit, [iron: 10, corn: 50], ["${household.uuid.toString()}": salary])

    supplier.setMoney(500)
  }

  def cleanup() {
    reg.cleanup()
  }

  def "run a turn"() {
    // this may be a challenge as some operations may be blocking and other not

    when:
    def endState = reg.runTurn()

    then:
   // println "endState= ${endState}"
    endState.each() {
      assert it.get() != null
      println("result = ${it.get()}")

    }
    when:
    def counter = 0
    def supStat = supplier.turnStatus()
    def hhStat = household.turnStatus()

    while(!(supStat.equals('complete')) || !(hhStat.equals('complete'))) {
      println("------  waiting ${counter+ 20}ms to complete ${supStat} ${hhStat}") // ${supplier.turnStatus()}")
      sleep(20)
      supStat = supplier.turnStatus()
      hhStat = household.turnStatus()

    }
    then:
    supStat == 'complete'
    hhStat == 'complete'
    assert household.turnNeeds == [housing:3, food: 1]

  }

}
