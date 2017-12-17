package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.resources.Bank
import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.Promise
import groovyx.gpars.group.DefaultPGroup
import groovyx.gpars.group.PGroup

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentMap
import java.util.function.BiConsumer

import static groovyx.gpars.GParsPool.withPool

@Slf4j
@Singleton
class Registry {

  PGroup pGroup = new DefaultPGroup(Runtime.getRuntime().availableProcessors())
  Map<UUID, BaseEconActor> actors = [:] as ConcurrentHashMap
  List<MarketActor> markets = []
  List<SupplierActor> suppliers = []
  List<HouseholdActor> households = []

  int turnNumber = 0

  def addActor(BaseEconActor actor) {
    actors.put(actor.uuid, actor)
    if (actor instanceof MarketActor) markets << (MarketActor) actor
    if (actor instanceof SupplierActor) suppliers << (SupplierActor) actor
    if (actor instanceof HouseholdActor) households << (HouseholdActor) actor
    actor.setParallelGroup(pGroup)
    if (!actor.isActive()) {
      actor.start()
    }else {
      log.info ("actor ${actor.uuid} was already active")
    }
  }

  Promise messageActor(uuid, Message message){
    log.info("sending message to actor ${uuid}, msg ${message.type}")
    return actors.get(uuid).sendAndPromise(message)
  }

  List<MarketActor> getMarkets() {
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
      actors.eachParallel() { k, v ->
        log.info "adding ${k} of type ${v.class}"
        turnStatus << messageActor(k, turnMsg)
      }
    })
    return turnStatus
  }

  private def _sysState() {
    JsonBuilder status = new JsonBuilder()
    def marketStatus = []
    def hhStatus = []
    def suppStatus = []
    markets.each { it ->
      marketStatus << messageActor(it.uuid, new Message(Command.STATUS)).get()
    }
    households.each {
      hhStatus << messageActor(it.uuid, new Message(Command.STATUS)).get()
    }

    suppliers.each {
      suppStatus << messageActor(it.uuid, new Message(Command.STATUS)).get()
    }

    status.system {
      houseHolds hhStatus
      systemMarkets marketStatus
      theSuppliers suppStatus
      turnData {
        turnNumber this.turnNumber
      }
    }

    return status
  }

  def getSystemStateString() {
    return _sysState().toString()
  }

  def cleanup() {
    markets = []
    suppliers = []
    households = []
    this.turnNumber = 0
    actors.each { k,v ->
      try {
        v.clear()
        v.stop()
        actors.remove(k)
      }catch(Exception e) {
        e.printStackTrace()
      }
    }
    Bank.clear()
  }
}
