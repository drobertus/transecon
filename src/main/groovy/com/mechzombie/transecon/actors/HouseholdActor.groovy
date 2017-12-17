package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.messages.dtos.Order
import com.mechzombie.transecon.resources.Bank
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.Promise

@Slf4j
class HouseholdActor extends BaseEconActor {

  Map<String, Integer> demands = [:] //this that the household needs per unit time
  Map<String, Integer> resources = [:] //this owned by or produced by the household per unit time
  //def money = 0

  Map<String, Integer> turnNeeds = [:]
  double turnBudget = 0.0

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
        if (!it) {
          println("msg was null!!")
          //reply null
          return
        }
        it = (Message) it
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
            // println("hh cash = ${available}")
            // get prices of products from markets
            turnNeeds.forEach { prod, count ->
              // println("need for ${prod} in quantity ${count}")
              def response  = getPrices(prod)
              // println("response ==> ${response}")
              if (response.size() == 0 ) {
                // TODO: What does this mean for the Household?  Does it die?
                //we need to keep trying t purchase supplies
                //println("No source of ${prod} -- ${response}")
                sleep(100)
                this.reg.messageActor(this.uuid, new Message(Command.PURCHASE_SUPPLIES))
              }else {
                Map<UUID, Double> prices = response
                // TODO: make purchase
                // println("uuid= ${prices.keySet()[0]}")
                def key = prices.keySet()[0]
                def budget = count * prices.get(key)

                def lock = Bank.holdDebitAmount(this.uuid, budget)
                //println("price for uuid = ${prices.get(key)}")
                //if(prices.get(key) > 0) {
                def bought = this.purchaseItem(prod, lock, key, count)
                addItemsToCollection(resources, bought.get().fulfilledItems)

                if(bought.get().orderItemsRemaining == [:]) {
                  this.completeStep(Command.PURCHASE_SUPPLIES)
                }
              }
            }

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
            String prod = it.vals.product
            double price = it.vals.price
            UUID market = it.vals.market
            log.info("purchasing ${it.vals}")
            def theHold = Bank.holdDebitAmount(this.uuid, price)

            if(theHold.amount == price ) {
              Promise<Order> response = purchaseItem(prod, theHold, market)
              log.info("purchase response => ${response.get()}")
              Order order = response.get()
              if (order.orderItemsRemaining == [:]) {
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

    Map<UUID, Double> returnedPrices = [:]
    //make calls to all markets and get prices
    Map<UUID, Promise<Double>> prices = [:]
    this.reg.getMarkets().each {
      def priceProm = it.sendAndPromise(new Message(Command.PRICE_ITEM, [product: product]));
      prices.put(it.uuid, priceProm)
    }
    prices.each { k, v ->
      Double aPrice = v.get()
      // println ("aprive= ${aPrice}")
      if (aPrice > 0) {
        returnedPrices.put(k, aPrice)
      }
    }
    return returnedPrices
  }

  private Promise<Order> purchaseItem(product, accountLock, market, count = 1){
    Map<String, Integer> shoppingList = [:]
    shoppingList.put(product, count)

    return submitOrder(shoppingList, accountLock, market)
  }

  private void addItemsToCollection(Map<String, Integer> source, Map<String, Integer> addition) {
    addition.each {product, count ->
      def prodCount = source.get(product)
      if(!prodCount) {
        source.put(product, count)
      } else {
        source.put(product, count + prodCount)
      }

    }
  }

  private Promise<Order> submitOrder(shoppingList, accountLock, market){
    def o = new Order()
    o.setCustomer(this.uuid)
    o.setMarket(market)
    //o.setBudgetedAmount(accountLock.amount)
    o.setAccountLock(accountLock)
    o.setOrderItemsRemaining(shoppingList)
    def response = reg.messageActor(market, new Message(Command.FULFILL_ORDER, [o]))
    //println("submitORder response = ${response}")
    response
  }

  @Override
  def clear() {
    this.demands.clear()
    this.resources.clear()
  }
}
