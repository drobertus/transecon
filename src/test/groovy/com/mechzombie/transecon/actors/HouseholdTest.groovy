package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import spock.lang.Shared

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
    //println ("output=  ${response}")
    response.toString() == "[household:[type:HouseholdActor, id:${hh.uuid}, requirements:[:], resources:[:], money:0.0]]"

  }

}
