package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.actors.Registry
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class ModelLoader {


  def reg = Registry.instance

  def loadFile(String path) {
    File f = new File(path)
    def slurper = new JsonSlurper()
    def jsonText = f.getText()
    def json = slurper.parseText( jsonText )
    log.info ("loaded ${json.toString()}")
    return json
  }

  def createModel(model) {
    println("name=${model.name}")
    println("description=${model.description}")

    model.startCondition.households.each {
      println("adding a household ${it}")
      def hh = new HouseholdActor(it)
      reg.addActor(hh)
    }

  }
}
