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

class TurnCycle extends BaseActorTest {

  def product = 'food'
  def producerPrice = 5

  def consumerLowPrice = 4
  def consumerEvenPrice = 5
  def consumerHighPrice = 6
  def salary = 50

  @Shared MarketActor market
  @Shared SupplierActor supplier
  @Shared HouseholdActor household

  Map hhDemand = [food: 2] //, housing: 3]
  Map hhResources = [food:1]

  def inputsPerUnit = [iron: 0.1, corn: 1, labor: 1]

  def setup() {
    market = new MarketActor(UUID.randomUUID())
    household = new HouseholdActor(UUID.randomUUID(), hhDemand, hhResources)
    supplier = new SupplierActor(UUID.randomUUID(), product, inputsPerUnit, [iron: 10, corn: 50], ["${household.uuid.toString()}": salary])

    Bank.deposit(supplier.uuid, 500)
  }

  def "run a turn"() {
    // this may be a challenge as some operations may be blocking and other not

    assert market.inventory.get(product) == null

    assert household.getResources() == [food: 1]
    when:
    def endState = reg.runTurn()

    then:
    endState.each() {
      assert it.get() != null
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
    assert supStat == 'complete'
    assert hhStat == 'complete'
    assert household.turnNeeds == [ food: 1 ] //housing:3,
    assert household.getResources() == [food: 2]
    assert supplier.toBePurchasedForProductionGoal == [labor: 1]
    assert supplier.productionGoalForTurn == 1

    //NOTE: when this passes we are seeing production
    assert supplier.getBankBalance() == 450.0
    assert supplier.resources.get('iron') == 9.9
    assert supplier.resources.get('corn') == 49
    assert market.inventory.get(product).getAvailableCount() == 1
    assert household.getResources() == [food: 2]
    assert household.getBankBalance() == salary - supplier.perUnitPrice

  }

}
