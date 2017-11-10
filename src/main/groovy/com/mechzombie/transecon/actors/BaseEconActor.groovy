package com.mechzombie.transecon.actors

import groovy.json.JsonBuilder
import groovyx.gpars.actor.DefaultActor

abstract class BaseEconActor extends DefaultActor {

  def builder = new JsonBuilder()

  UUID uuid
  def transactions = []

  BaseEconActor(UUID id) {
    this.uuid = id
    println "ID= ${this.uuid}"
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
}
