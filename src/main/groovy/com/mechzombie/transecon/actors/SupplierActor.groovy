package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import com.mechzombie.transecon.resources.Bank
import groovy.util.logging.Slf4j

@Slf4j
class SupplierActor extends BaseEconActor{

  def product
  def perUnitPrice = 20 //TODO: figure out how to handle price
  def inputs = [:]
  def resources = [:]
  Map<UUID, Double> employees = [:]

  Integer productionGoalForTurn
  def toBePurchasedForProductionGoal

  def sales

  SupplierActor(UUID id=UUID.randomUUID(), product, inputsPerUnit = [:], resources =[:], employees = [:]) {
    super(id)
    this.product = product
    employees.each { k, v ->
      employHousehold(UUID.fromString(k), v)
    }
    //this.employees = employees
    this.inputs = inputsPerUnit
    this.resources = resources

    //println ("supplier ${uuid}")
    this.stepList = [
        Command.CALC_NEEDS,
        Command.FINANCE_TURN,
        //Command.CALC_DEMAND,
        Command.RUN_PAYROLL,
        Command.PURCHASE_SUPPLIES,
        Command.PRODUCE_ITEMS,
        Command.SHIP_ITEMS]
                     //Command.STOCK_ITEM]
    resetTurnStatus()
    log.info("Created Supplier ${id}")
  }

  def employHousehold(UUID uuid, int monthlyWage) {
    this.employees.put(uuid, monthlyWage)
  }

  @Override
  def status() {
    builder.econactor {
        type this.class.simpleName
        id this.uuid
        output product
        perUnitInputs this.inputs
        resources this.resources
        employees this.employees
        money Bank.getAccountValue(this.uuid)
      }

    return builder.content //.toString()
  }

  @Override
  protected void act() {
    loop {
      react {
        def theResponse
        log.info "Supplier ${this.uuid} received ${it.type}"
        switch (it.type) {
          case Command.STATUS:
            theResponse = status()
            break
          case Command.RUN_PAYROLL:
            def payroll = [:]

            //TODO: move payment calc to CALC_NEEDS
            employees.each { k, v ->
              payroll.put(k, Bank.transferFunds(this.privateUUID, k, v))
            }
            def allSent = true
            payroll.values().each {
            }
            theResponse = 'OK'
            this.completeStep(Command.RUN_PAYROLL)
            break
//          case Command.SEND_MONEY:
//            def from = it.vals.from
//            def amount = it.vals.amount
//            money += amount
//            def reason = it.vals.reason
//
//
//            theResponse = "OK"
//            break
          case Command.CALC_NEEDS:

            def salaries = 0
            employees.each { k, v ->
              salaries += v
            }
            def laborAvailableAtTurn = employees.size()

            //find the limiting factor for input

            def limitingRatios = [:]
            inputs.each { k, v ->
              def onHand = resources.get(k)
              if (k == 'labor') {
                onHand = laborAvailableAtTurn
              }
              def ratio = 0.0
              if(onHand) {
                ratio = (onHand / v)
              }
              limitingRatios.put(k, ratio)
            }
            def sorted = limitingRatios.sort { it.value }

            //now get all the ones with the first value.
            //if 0 then...?
            // TODO: need a turn production target! Count of units expected to be produced in this turn
            //map of needed type of products and volume that need to be purchased
            // will be empty is all on hand
            this.toBePurchasedForProductionGoal.put('labor', limitingRatios.get('labor'))
            this.productionGoalForTurn = sorted.values().toArray()[0]

            // look at resources
            // look at labor
            // look at input costs
            // determine what to buy to maximize production

            // TODO: find cost minimization strategy



            // look at payroll demands
            // cost of inputs
            // expected income?
            // cost for shipment?s


            this.completeStep(Command.CALC_NEEDS)
            break

          case Command.PURCHASE_SUPPLIES:
            this.toBePurchasedForProductionGoal.each{
              //purchase everything but labor
              if(it.key != 'labor') {
                // get prices from markets
                // make purchase
                // do all this in blocking form
                // TODO: if purchases can not be made then adjust production goals?

              }

            }
            //given what is needed to fill the gap to produce, make outlays to fill in the gaps

            this.completeStep(Command.PURCHASE_SUPPLIES)
            break
          case Command.PRODUCE_ITEMS:

            //create new things, remove avilable resources that are converted, including labor
            //(fixed things as well as time-sensitive things RE: labor)
            def consumed = [:]
            inputs.each { product, amount ->

              def amountUsed = amount * this.productionGoalForTurn
              def onHand = resources.get(product)

              if(product == 'labor' && onHand == null) {
                onHand = amountUsed
              }
              consumed.put(product, amountUsed)
              resources.put(product, onHand - amountUsed)
            }

            resources.put(this.product, this.productionGoalForTurn)

            this.completeStep(Command.PRODUCE_ITEMS)
            break
          case Command.FINANCE_TURN:
            // eval cash on hand - payroll - expenses needed for production ( plus expected income?)
            // take out loan
            // TODO: include loan financing as part of cost structure

            this.completeStep(Command.FINANCE_TURN)
            break
          case Command.SHIP_ITEMS:

            //TODO: find a way to optomise which markets to ship to

            def shipment = reg.messageActor(reg.markets[0].uuid, new Message(Command.STOCK_ITEM,
                [producer: this.uuid, product: this.product, price: this.perUnitPrice, quantity: productionGoalForTurn]))

            if(shipment.get() == this.productionGoalForTurn) {
              this.completeStep(Command.SHIP_ITEMS)
            }
            theResponse = 'SHIPPED'
            break
          case Command.TAKE_TURN:

//            this.currentTurnStatus = [:]
//            this.stepList.each {
//              println("adding ${it} to ${this.class}")
//              this.currentTurnStatus.put(it, 'incomplete')
//              reg.messageActor(this.uuid, new Message(it))
//            }


            // reset turn parameters
            this.toBePurchasedForProductionGoal = [:]
            this.productionGoalForTurn = 1
            theResponse = runTurn()

            break
          default:
            theResponse = "unrecognized Command"
            break
        }

        reply theResponse
      }
    }
  }

  def shipItem(MarketActor destination, String product, double price, int quantity = 1) {
    destination.sendAndPromise(new Message(Command.STOCK_ITEM, [producer: this.uuid, product: product, price: price, quantity: quantity]) )
  }

  @Override
  def clear() {
    this.employees.clear()
    this.resources.clear()
  }

}
