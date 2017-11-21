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

  BaseEconActor(UUID id) {
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
      println("completing step ${step}, ${this.currentTurnStatus}")
    } else {
      println("steps are : ${currentTurnStatus}")
      throw new Exception("step ${step.toString()} not found for ${this.class}".toString())
    }
  }

  def turnStatus(){
    def status = 'complete'
    if (currentTurnStatus.size() == stepList.size()){
      currentTurnStatus.each {k,v ->
        if (v != 'complete'){
          //println("${k} was not complete")
          status = 'incomplete';
        }
      }

      return status
    }
    return 'incomplete - mismatch'
    //return currentStepStatus
  }


  protected resetTurnStatus() {
    currentTurnStatus = [:]
    println("steps = ${stepList}")
    stepList.each {
      println("adding ${it} to ${this.class}")
      currentTurnStatus.put(it, 'incomplete')
      reg.messageActor(this.uuid, new Message(it))
    }
  }

  protected def runTurn() {

    resetTurnStatus()
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
