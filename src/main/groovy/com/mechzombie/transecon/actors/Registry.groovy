package com.mechzombie.transecon.actors

import groovyx.gpars.group.DefaultPGroup

@Singleton
class Registry {

  def pGroup = new DefaultPGroup(Runtime.getRuntime().availableProcessors())
  Map<UUID, BaseEconActor> actors = [:]
  def markets = []


  def addActor(BaseEconActor actor) {
    actors.put(actor.uuid, actor)
    if (actor instanceof MarketActor) markets << actor
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

  def cleanup() {
    markets = []
   // actors = [:]
    //pGroup.shutdown()
    actors.values().each {
      try {
        it.stop()
      }catch(Exception e) {
        e.printStackTrace()
      }
      actors.remove(it)
    }

  }
}
