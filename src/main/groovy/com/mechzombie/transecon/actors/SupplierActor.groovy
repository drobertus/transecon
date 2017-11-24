package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message
import groovy.util.logging.Slf4j

@Slf4j
class SupplierActor extends BaseEconActor{

  def product
  def inputs = [:]
  def resources = [:]
  def employees = [:]
  def money = 0

  def productionGoalForTurn
  def toBePurchasedForProductionGoal

  def sales

  SupplierActor(UUID id=UUID.randomUUID(), product, inputsPerUnit = [:], resources =[:], employees = [:]) {
    super(id)
    this.product = product
    this.employees = employees
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
    log.info("Created Household ${id}")
  }

  def employHousehold(UUID uuid, int monthlyWage) {
    this.employees.put(uuid.toString(), monthlyWage)
  }

  @Override
  def status() {
    def status
    try {
      status = builder.econactor {
        type this.class
        id this.uuid
        perUnitInputs this.inputs
        resources this.resources
        employees this.employees
        money this.money
        //transactions this.transactions
      }
    }
    catch(Exception e) {
      println "err ${e}"
    }
    return status.toString()
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
              println("-----key = ${k}, amount= ${v}")
              if (money > v) {
                println("sending money!")
                payroll.put(k, sendMoney(UUID.fromString(k), v, 'payroll'))
                money -= v
              }else {
                println("Payroll for ${k} of ${v} unable to be met by supplier")
              }
            }

            payroll.values().each {
              println("got payroll response --" && it.get())
            }
            theResponse = 'OK'
            this.completeStep(Command.RUN_PAYROLL)
            break
          case Command.SEND_MONEY:
            def from = it.vals.from
            def amount = it.vals.amount
            money += amount
            def reason = it.vals.reason


            theResponse = "OK"
            break
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
              def ratio  = (onHand / v)

              limitingRatios.put(k, ratio)
            }

            println("ratios unsorted ${limitingRatios}")
            def sorted = limitingRatios.sort { it.value }
            println("ratios sorted ${sorted}")

            //now get all the ones with the first value.
            //if 0 then...?
            // TODO: need a turn production target! Count of units expected to be produced in this turn
            //map of needed type of products and valoume that need to be purchased
            // will be empty is all on hand
            this.toBePurchasedForProductionGoal = [:]

            this.productionGoalForTurn = 1


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

            //given what is needed to fill the gap to produce, make outlays to fill in the gaps

            this.completeStep(Command.PURCHASE_SUPPLIES)
            break
          case Command.PRODUCE_ITEMS:

            //create new things, remove avilable resources that are converted, including labor
            //(fixed things as well as time-sensitive things RE: labor)

            this.completeStep(Command.PRODUCE_ITEMS)
            break
          case Command.FINANCE_TURN:
            // eval cash on hand - payroll - expenses needed for production ( plus expected income?)
            // take out loan
            // TODO: include loan financing as part of cost structure

            this.completeStep(Command.FINANCE_TURN)
            break
          case Command.SHIP_ITEMS:
            this.completeStep(Command.SHIP_ITEMS)
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

  def shipItem(MarketActor destination, String product, int price) {
    destination.sendAndPromise(new Message(Command.STOCK_ITEM, [producer: this.uuid, product: product, price: price]) )
  }

  @Override
  def clear() {
    this.employees.clear()
    this.resources.clear()
    this.money = 0
  }

}
