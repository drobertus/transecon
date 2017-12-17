package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.resources.Bank
import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.actor.DefaultActor

@Slf4j
//@CompileStatic
abstract class BaseEconActor extends DefaultActor {

  int lastCompletedTurn = 0
  protected Map currentTurnStatus = [:]
  protected List stepList
  def builder = new JsonBuilder()
  Registry reg = Registry.instance

  UUID uuid
  UUID privateUUID
  def transactions = []

  BaseEconActor(UUID id = UUID.randomUUID()) {
    this.uuid = id
    privateUUID = Bank.createAccount(uuid)
    reg.addActor(this)
  }

  def getSteps() {
    return stepList
  }

  abstract def clear()

  def completeStep(step) {

    def found = currentTurnStatus.get(step)
    if(found != null) {
      currentTurnStatus.put(step, 'complete')
      log.info("completing step ${step}, ${this.currentTurnStatus}")
    } else {
      log.error("step ${step.toString()} not found for ${this.class}- available steps are ${this.stepList}".toString())
      throw new Exception("step ${step.toString()} not found for ${this.class}".toString())
    }
  }

  def turnStatus(){
    def status = 'complete'
    if (currentTurnStatus.size() == stepList.size()){
      for(def obj : currentTurnStatus) { //.each {k,v ->
        if (obj.value != 'complete'){
          status = 'incomplete'
          break
        }
      }
      log.info("returning status of ${status} for ${this.uuid}")
      return status
    }
    return 'incomplete - mismatch'
  }

  /**
   * This is called to start a new turn and refreshes the status of existing steps
   * @return
   */
  protected resetTurnStatus() {
    currentTurnStatus = [:]
    stepList.each {
      currentTurnStatus.put(it, 'incomplete')
    }
  }

  protected def runTurn() {
    resetTurnStatus()
    stepList.each {
      reg.messageActor(this.uuid, new Message(it))
    }
    return 'messages fired'
  }

  int lastCompletedTurn() {
    return lastCompletedTurn
  }
  abstract def status()

  protected def sendMoney(UUID recipient, Double amount, reason = null) {
    return Bank.deposit(recipient, amount)
  }

  def getBankBalance() {
    return Bank.getAccountValue(this.uuid)
  }
}
