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

    when:
    response = hh.sendAndWait(new Message(Command.STATUS))
    then:

    String resp = '{"household":{"type":"HouseholdActor","id":"' +
      hh.uuid.toString() +
     '","requirements":{},"resources":{},"money":0.0}}'
    response.toString() == resp

  }

  def "asJson test" () {
    def response

    when:
    response = hh.asJson()
    then:

    String resp = '{"household":{"type":"HouseholdActor","id":"' +
        hh.uuid.toString() +
        '","requirements":{},"resources":{},"money":0.0}}'
    response.toString() == "[household:[type:HouseholdActor, id:${hh.uuid}, requirements:[:], resources:[:], money:0.0]]"

  }

}
