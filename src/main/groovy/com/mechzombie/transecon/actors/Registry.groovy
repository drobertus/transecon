package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.resources.Bank
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import groovyx.gpars.group.DefaultPGroup

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

import static groovyx.gpars.GParsPool.withPool

@Slf4j
@Singleton
class Registry {

  def pGroup = new DefaultPGroup(Runtime.getRuntime().availableProcessors())
  ConcurrentHashMap<UUID, BaseEconActor> actors = [:]

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
      log.info ("actor ${actor.uuid} was already active")
    }
  }

  def messageActor(uuid, message){
    log.info("sending message to actor ${uuid}, msg ${message.type}")
   // try {
      return actors.get(uuid).sendAndPromise(message)
//    }catch(ex) {
//      log.info("error sending message to actor ${uuid}, msg ${message.type}")
//      throw new IllegalStateException(ex)
//    }
  }

  MarketActor[] getMarkets() {
    return markets
  }

  /**
   * A blocking call- will block until all actors complete a turn
   */
  def runTurn() {
    ConcurrentLinkedQueue turnStatus = []
    turnNumber ++
    Message turnMsg = new Message(Command.TAKE_TURN, [turnNum: turnNumber])
    //TODO: this needs to be non-blocking
    withPool(Runtime.runtime.availableProcessors(), {
  //    try {
        actors.eachParallel() { k, v ->
          log.info "adding ${k} of type ${v.class}"
          turnStatus << messageActor(k, turnMsg)
        }
//      }catch(IllegalStateException ise) {
//
//        log.info("illegal state " + ise.toString())
//        throw new Exception(ise)
//      }
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
    actors.each { k,v ->
      try {
        v.clear()
        v.stop()
        actors.remove(k)
      }catch(Exception e) {
        e.printStackTrace()
      }


    }
    println("cleanup size = ${actors.size()}")
    Bank.clear()

  }
}
