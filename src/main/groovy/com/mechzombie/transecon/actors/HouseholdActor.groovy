package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import groovy.util.logging.Slf4j

@Slf4j
class HouseholdActor extends BaseEconActor {

  def demands = [:] //this that the household needs per unit time
  def resources = [:] //this owned by or produced by the household per unit time
  def money = 0

  def turnNeeds = [:]
  HouseholdActor(UUID id, Map demands = [:], Map resources = [:]) {
    super(id)
    setDemands demands
    setResources(resources)
    this.stepList = [Command.CALC_NEEDS,
                     Command.FINANCE_TURN,
                     Command.PURCHASE_SUPPLIES,
                     Command.CONSUME]
    this
    resetTurnStatus()
    log.info("Created Household ${id}")
  }

  @Override
  def status() {
    def status
    try {
      status = builder.econactor {
        type this.class
        id this.uuid
        requirements this.demands
        resources this.resources
        money this.money
      }
    }
    catch(Exception e) {
      log.error(e)
    }
    return status.toString()
  }

  @Override
  protected void act() {
    loop {

      react {
        def theResponse
        log.debug "HouseHold ${this.uuid} received ${it.type}"
        switch (it.type) {
          case String:
            theResponse = "Woohoo!!"
            break
          case Command.STATUS:
            theResponse = status()
            //println "status = ${theResponse}"
            break
          case Command.CALC_NEEDS:
            //loop through demands, compare to resources
            turnNeeds = [:]
            this.demands.each {k, v ->
              turnNeeds.put(k, v - (resources.get(k) ? resources.get(k) : 0 ))
            }
            this.completeStep(Command.CALC_NEEDS)
            break
          case Command.FINANCE_TURN:

            this.completeStep(Command.FINANCE_TURN)
            break
          case Command.PURCHASE_SUPPLIES:

            this.completeStep(Command.PURCHASE_SUPPLIES)
            break
          case Command.CONSUME:

            this.completeStep(Command.CONSUME)
            break

          case Command.SEND_MONEY:
            def from = it.vals.from
            def amount = it.vals.amount
            money += amount
            def reason = it.vals.reason

            theResponse = "OK"
            break
          case Command.PURCHASE_ITEM:
            def prod = it.vals.product
            def price = it.vals.price
            def market = it.vals.market

            if (price <= this.money) {
              def response = purchaseItem(prod, price, market)
              if (response.get() == 'OK') {
                money -= price
                def prodCount = resources.get(prod)
                if(!prodCount) {
                  prodCount = 0
                }
                prodCount++
                resources.put(prod, prodCount)
                theResponse = "OK"
              }
              else {
                theResponse = "was unable to make purchase"
              }
            }
            else {
              theResponse = "NSF"
            }
            //this need to be done in line to prevent double dipping
            break
          case Command.TAKE_TURN:
            theResponse = runTurn()
            break
          default:
            def msg = "message not understood '${it.type}' for class ${this.class} instance ${uuid}"
            log.error msg
            theResponse = msg
            break
        }
        reply theResponse
      }
    }
  }

  def getPrices(product) {

    //make calls to all markets and get prices
    def prices = [:]
    reg.getMarkets().each {
      // println("sending getPrice  to ${it.uuid}")
      def priceProm = it.sendAndPromise(new Message(Command.PRICE_ITEM, [product: product]));
      prices.put(it.uuid, priceProm)
    }
    prices.each { k, v ->
      def aPrice = v.get()
      // println("price found of ${aPrice}")
      prices.put(k, aPrice)
    }
    return prices
  }

  private def purchaseItem(product, price, market){
    return reg.messageActor(market, new Message(Command.FULFILL_ORDER, [buyer: this.uuid, product: product, price: price]))
  }

  @Override
  def clear() {
    demands.clear()
    this.resources.clear()
    this.money = 0
  }
}
