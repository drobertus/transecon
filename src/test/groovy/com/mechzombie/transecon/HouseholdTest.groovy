package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.actors.Registry
import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

class HouseholdTest extends Specification {

  @Shared hh //= new HouseholdActor(UUID.randomUUID())

  def setup() {
    hh = new HouseholdActor(UUID.randomUUID())
  }

  def cleanup() {
    Registry.instance.cleanup()
  }
  //start an actor and message it
  def "handling basic messages"() {

    def response
  //  when:
  //  response = hh.sendAndWait('test') //'Message', {reply -> response = reply}
  //  then:
  //  response != null
  //  println response
    when:
    response = hh.sendAndWait(new Message(Command.STATUS))
    then:
    println response
    response == '[econactor:[type:class com.mechzombie.transecon.actors.HouseholdActor, id:' +
      hh.uuid + ', requirements:[:], resources:[:], money:0]]'

  }

}
