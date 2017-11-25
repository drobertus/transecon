package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.actors.Registry
import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

class HouseholdTest extends BaseActorTest {

  @Shared hh

  def setup() {
    hh = new HouseholdActor(UUID.randomUUID())
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
    println ("output=  ${response}")
    response.toString() == "[household:[type:HouseholdActor, id:${hh.uuid}, requirements:[:], resources:[:], money:0.0]]"

  }

}
