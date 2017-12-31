package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.resources.Bank
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.Promise
import groovyx.gpars.group.DefaultPGroup
import groovyx.gpars.group.PGroup

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import static groovyx.gpars.GParsPool.withPool

@Slf4j
@Singleton
class Registry {

  PGroup pGroup = new DefaultPGroup(Runtime.getRuntime().availableProcessors())
  static Map<UUID, BaseEconActor> actors = [:] as ConcurrentHashMap
  static Map<Integer, UUID> modelMap = [:]
  List<MarketActor> markets = []
  List<SupplierActor> suppliers = []
  List<HouseholdActor> households = []

  int turnNumber = 0

  def addActor(BaseEconActor actor) {
    if(actor.id) {
      if (modelMap.get(actor.id) != null) {
        throw new Exception("An actor with id ${actor.id} already exists.  Can not add another")
      }
      modelMap.put(actor.id, actor.uuid)
    }
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

  static Promise messageActor(uuid, Message message){
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
      actors.entrySet().eachParallel() { // k, v ->
        def  k = ((Map.Entry<UUID, BaseEconActor>)it).key
        def  v = ((Map.Entry<UUID, BaseEconActor>)it).value
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
      marketStatus << messageActor(it.uuid, new Message(Command.AS_JSON)).get()
    }
    households.each {
      hhStatus << messageActor(it.uuid, new Message(Command.AS_JSON)).get()
    }

    suppliers.each {
      suppStatus << messageActor(it.uuid, new Message(Command.AS_JSON)).get()
    }

    status.system {
      houseHolds hhStatus
      systemMarkets marketStatus
      theSuppliers suppStatus
      turnData {
        turn this.turnNumber
      }
    }

    return status.toString()
  }

  static def getActorByModelId(id) {
    UUID uuid = modelMap.get(id)
    println("uuid ${uuid}")
    return actors.get(uuid)
  }

  def getSystemStateString() {
    return _sysState().toString()
  }

  def cleanup() {
    markets.clear()
    suppliers.clear()
    households.clear()
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
