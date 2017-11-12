package com.mechzombie.transecon.actors

@Singleton
class Registry {

  Map<UUID, BaseEconActor> actors = [:]
  def markets = []


  def addActor(BaseEconActor actor) {
    actors.put(actor.uuid, actor)
    if (actor instanceof MarketActor) markets << actor
    actor.start()
  }

  def messageActor(uuid, message){
    actors.get(uuid).send(message)
  }

  MarketActor[] getMarkets() {
    return markets
  }

  def cleanup() {
    actors.values().each {
      it.stop()
      actors.remove(it)
    }
  }
}
