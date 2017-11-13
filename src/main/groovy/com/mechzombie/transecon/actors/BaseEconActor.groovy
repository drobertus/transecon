package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import groovy.json.JsonBuilder
import groovyx.gpars.actor.DefaultActor

abstract class BaseEconActor extends DefaultActor {

  def builder = new JsonBuilder()
  def reg = Registry.instance

  UUID uuid
  def transactions = []

  BaseEconActor(UUID id) {
    this.uuid = id
    reg.addActor(this)
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

  protected def sendMoney(UUID recipient, amount, reason) {
    return reg.messageActor(recipient, new Message(Command.SEND_MONEY, [from: this.uuid, amount: amount, reason:  reason]))
  }
}
