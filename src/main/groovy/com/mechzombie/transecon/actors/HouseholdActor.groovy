package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import groovy.json.JsonBuilder

class HouseholdActor extends BaseEconActor {

  boolean employed

  def demands = [:]
  def resources = [:]

  HouseholdActor(UUID id) {
    super(id)
    println("id ${id}")
  }

  @Override
  def status() {
    println('getting status')
    def status
    try {
      status = builder.econactor {
        type this.class
        id this.uuid
        employed this.employed
      }
    }
      catch(Exception e) {
      println "err ${e}"
      }

    /*
        builder.econactor {
      type this.class
      id this.uuid
      employed employed
    }
    */
    println "here: ${status.toString()}"
    return status.toString()
  }

  @Override
  protected void act() {
    loop {

      react {
        def theResponse
        println 'it' + it
        switch (it) {
          case String:
            theResponse = "Woohoo!!"
            break
          case Command:
            switch (it.name) {
              case 'status':
                theResponse = status()
                println "status = ${theResponse}"
                break
              default:
                theResponse = "unrecognized Command"
                break
            }
            break
          default:
            theResponse = "message not understood '${it}'"
            break
        }
        reply theResponse
      }
    }
  }
}
