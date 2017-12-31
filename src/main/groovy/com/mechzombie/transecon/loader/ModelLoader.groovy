package com.mechzombie.transecon.loader

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.actors.MarketActor
import com.mechzombie.transecon.actors.Registry
import com.mechzombie.transecon.actors.SupplierActor
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class ModelLoader {


  private Registry reg = Registry.instance

  /**
   * Read a file, return a JSON data model
   * @param path - path to the Econ model in JSON format
   * @return the model as a JSON object
   */
  Object loadFile(String path) {
    File f = new File(path)
    def slurper = new JsonSlurper()
    def jsonText = f.getText()
    def json = slurper.parseText( jsonText )
    log.info ("loaded ${json.toString()}")
    return json
  }

  /**
   * Create a runnable model/simulation based on a JSON input
   * @param model - the JSON model to be converted to runnable actors
   * @return boolean - true if load succeeded, false if not
   */
  boolean createModel(Object model) {
    println("name=${model.name}")
    println("description=${model.description}")

    model.startCondition.households.each {
      println("adding a household ${it}")
      def hh = new HouseholdActor(it)
      reg.addActor(hh)
    }

    model.startCondition.markets.each {
      println("adding a market ${it}")
      def market = new MarketActor(it)
      reg.addActor(market)
    }

    model.startCondition.suppliers.each {
      println("adding a supplier ${it}")
      def supplier = new SupplierActor(it)
      reg.addActor(supplier)
    }
  }
}
