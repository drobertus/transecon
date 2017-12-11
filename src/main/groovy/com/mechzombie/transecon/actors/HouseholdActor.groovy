package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.resources.Bank
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j

@Slf4j
class HouseholdActor extends BaseEconActor {

  def demands = [:] //this that the household needs per unit time
  def resources = [:] //this owned by or produced by the household per unit time
  //def money = 0

  def turnNeeds = [:]

  HouseholdActor(UUID id = UUID.randomUUID(), Map demands = [:], Map resources = [:]) {
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
    def moneyAcct = Bank.getAccountValue(this.uuid)
    builder.household {
        type this.class.simpleName
        id this.uuid
        requirements this.demands
        resources this.resources
        money moneyAcct
      }

    return builder.content
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
            //TODO:  we need to buy what the household needs
            log.info("need to purchase ${turnNeeds}")
            def available = getBankBalance()
            // get prices of products from markets
            turnNeeds.forEach { prod, count ->

              Map<UUID, Double> prices = getPrices(prod)
              //TODO: make purchase

              this.purchaseItem(prod, prices.value[0], prices.keySet()[0], count)

            }


            //get optimal prices per prodcut

            //make purchases

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

            def sent = Bank.transferFunds(this.privateUUID, from)

            theResponse = "OK"
            break
          case Command.PURCHASE_ITEM:


            //TODO: encapsulate this business logic
            def prod = it.vals.product
            def price = it.vals.price
            def market = it.vals.market
            log.info("purchasing ${it.vals}")
            def theHold = Bank.holdDebitAmount(this.uuid, price)
            if(theHold.amount == price ) {
              def response = purchaseItem(prod, price, market)
              log.info("purchase response => ${response}")
              if (response.get() == 'OK') {
                def prodCount = resources.get(prod)
                if(!prodCount) {
                  prodCount = 0
                }
                prodCount++
                resources.put(prod, prodCount)
                log.info("purchased- spent ${price}" )

                Bank.completeDebit(theHold)
                theResponse = "OK"
              }
              else {
                Bank.cancelLock(theHold)
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

  Map<UUID, Double> getPrices(product) {

    //make calls to all markets and get prices
    def prices = [:]
    this.reg.getMarkets().each {
      def priceProm = it.sendAndPromise(new Message(Command.PRICE_ITEM, [product: product]));
      prices.put(it.uuid, priceProm)
    }
    prices.each { k, v ->
      def aPrice = v.get()
      prices.put(k, aPrice)
    }
    return prices
  }

  private def purchaseItem(product, price, market, count){
    return reg.messageActor(market, new Message(Command.FULFILL_ORDER, [buyer: this.uuid, product: product, price: price, count: count]))
  }

  @Override
  def clear() {
    demands.clear()
    this.resources.clear()
  }
}
