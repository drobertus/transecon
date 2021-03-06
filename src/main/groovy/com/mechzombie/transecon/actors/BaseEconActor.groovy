package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.resources.Bank
import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.actor.DefaultActor

import java.util.concurrent.atomic.AtomicInteger

@Slf4j
@CompileStatic
abstract class BaseEconActor extends DefaultActor {

  int lastCompletedTurn = 0
  protected Map<Command, String> currentTurnStatus = [:]
  protected List<Command> stepList
  protected StreamingJsonBuilder builder = new StreamingJsonBuilder(new StringWriter(1000)) //JsonBuilder builder = new JsonBuilder()

  Registry reg = Registry.instance

  UUID uuid
  int id
  String name
  UUID privateUUID
  private static AtomicInteger idSeq = new AtomicInteger(0)

  /**
   * A default constructor, based on UUID
   * @param id
   */
  BaseEconActor(UUID uuid = UUID.randomUUID()) {
    this.uuid = uuid
    this.id = idSeq.incrementAndGet()
    privateUUID = Bank.createAccount(uuid)
    reg.addActor(this)
  }

  /**
   * Constructor used from eco file, based on Integer id
   * @param id
   */
  BaseEconActor(Integer id) {
    this.id = id
    this.uuid = UUID.randomUUID()
    privateUUID = Bank.createAccount(uuid)
  }

  abstract def clear()

  /**
   * Set the status of the most recent step to "complete"
   * @param step
   * @return String the status of the step
   */
  def completeStep(Command step) {

    def found = currentTurnStatus.get(step)
    if(found != null) {
      currentTurnStatus.put(step, 'complete')
      log.info("completing step ${step}, ${this.currentTurnStatus}")
    } else {
      log.error("step ${step.toString()} not found for ${this.class}- available steps are ${this.stepList}".toString())
      throw new Exception("step ${step.toString()} not found for ${this.class}".toString())
    }
  }

  /**
   * Return the status of the last run
   * @return
   */
  def turnStatus(){
    String status = 'complete'
    if (currentTurnStatus.size() == stepList.size()){
      currentTurnStatus.entrySet().each {
        if (it.value != 'complete'){
          status = 'incomplete'
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
  /**
   *
   * @return String representing the onbject serialized to JSON
   */
  abstract String status()

  /**
   *
   * @return JsonBuilder.content
   */
  abstract def asJson()

  def getBankBalance() {
    return Bank.getAccountValue(this.uuid)
  }
}
