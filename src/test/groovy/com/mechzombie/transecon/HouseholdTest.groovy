package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.messages.Command
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

class HouseholdTest extends Specification {

  @Shared hh = new HouseholdActor(UUID.randomUUID())

  def setup() {
    hh.start()
  }

  def cleanup() {
    hh.stop()
  }
  //start an actor and message it
  def "handling basic messages"() {

    def response
    when:
    response = hh.sendAndWait('test') //'Message', {reply -> response = reply}
    then:
    response != null
    println response
    when:
    def getStatus = Command.STATUS
    response = hh.sendAndWait(getStatus)
    then:
    println response
    response == '[econactor:[type:class com.mechzombie.transecon.actors.HouseholdActor, id:' + hh.uuid + ', employed:false]]'
  }

}
