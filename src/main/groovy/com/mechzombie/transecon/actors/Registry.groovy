package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import groovyx.gpars.group.DefaultPGroup

import static groovyx.gpars.GParsPool.withPool

@Slf4j
@Singleton
class Registry {

  def pGroup = new DefaultPGroup(Runtime.getRuntime().availableProcessors())
  Map<UUID, BaseEconActor> actors = [:]
  def markets = []
  def suppliers = []
  def households = []

  def turnNumber = 0

  def addActor(BaseEconActor actor) {
    actors.put(actor.uuid, actor)
    if (actor instanceof MarketActor) markets << actor
    if (actor instanceof SupplierActor) suppliers << actor
    if (actor instanceof HouseholdActor) households << actor
    actor.parallelGroup = pGroup
    if (!actor.isActive()) {
      actor.start()
    }else {
      println ("actor ${actor.uuid} was already active")
    }
  }

  def messageActor(uuid, message){
    return actors.get(uuid).sendAndPromise(message)
  }

  MarketActor[] getMarkets() {
    return markets
  }

  /**
   * A blocking call- will block until all actors complete a turn
   */
  def runTurn() {
    def turnStatus = []
    turnNumber ++
    Message turnMsg = new Message(Command.TAKE_TURN, [turnNum: turnNumber])
    //TODO: this needs to be non-blocking
    withPool(Runtime.runtime.availableProcessors(), {
      actors.eachParallel() { k, v ->
        log.info "adding ${k}"
        turnStatus << messageActor(k, turnMsg)
      }
    })


    //block until turn completes
    return turnStatus
  }

  def getSystemState() {
    def status
    def marketStatus = []
    def hhStatus = []
    def suppStatus = []
    markets.each {
      marketStatus << messageActor(it.uuid, new Message(Command.STATUS)).get()
    }
    households.each {
      hhStatus << messageActor(it.uuid, new Message(Command.STATUS)).get()
    }

    suppliers.each {
      suppStatus << messageActor(it.uuid, new Message(Command.STATUS)).get()
    }

    try {
      status = new JsonBuilder().system {
        houseHolds hhStatus
        systemMarkets marketStatus
        theSuppliers suppStatus
      }
    }
    catch(Exception e) {
      println "err ${e}"
    }
    //println "here: ${status.toString()}"
    return status.toString()
  }

  def cleanup() {
    markets = []
    suppliers = []
    households = []
   // actors = [:]
    //pGroup.shutdown()
    actors.values().each {
      try {
        it.clear()
        it.stop()
      }catch(Exception e) {
        e.printStackTrace()
      }
      actors.remove(it)
    }

  }
}
