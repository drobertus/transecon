package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import spock.lang.Shared
import spock.lang.Specification

class HouseholdTest extends Specification {


    def id = UUID.randomUUID()
    @Shared hh = new HouseholdActor(uuid: id)


    def setup() {
        hh.start()
    }

    def cleanup() {
        hh.stop()
    }
    //start an actor and message it
    def "maximum of two numbers"() {


        def response
        when:
        response = hh.sendAndWait('test') //'Message', {reply -> response = reply}
        then:
        response != null
        println response


    }

}
