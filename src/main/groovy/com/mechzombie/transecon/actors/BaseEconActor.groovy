package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import groovyx.gpars.actor.DefaultActor

@Slf4j
abstract class BaseEconActor extends DefaultActor {

  def lastCompletedTurn = 0
  protected currentTurnStatus = [:]
  protected stepList
  def builder = new JsonBuilder()
  def reg = Registry.instance

  UUID uuid
  def transactions = []

  BaseEconActor(UUID id = UUID.randomUUID()) {
    this.uuid = id
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
      currentTurnStatus.each {k,v ->
        if (v != 'complete'){
          status = 'incomplete';
        }
      }

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

  @Override
  protected void act() {
    loop {
      react {
        println "it is $it"
        reply "no override"
      }
    }
  }

  protected def sendMoney(UUID recipient, amount, reason) {
    return reg.messageActor(recipient, new Message(Command.SEND_MONEY, [from: this.uuid, amount: amount, reason:  reason]))
  }
}
