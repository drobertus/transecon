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


  SupplierActor(UUID id, product, inputsPerUnit = [:], resources =[:], employees = [:]) {
    super(id)
    this.product = product
    this.employees = employees
    this.inputs = inputsPerUnit
    this.resources = resources

    println ("supplier ${uuid}")
    this.stepList = [Command.RUN_PAYROLL,
                     Command.CALC_DEMAND,
                     Command.CALC_NEEDS,
                     Command.PURCHASE_SUPPLIES,
                     Command.PRODUCE_ITEMS,
                     Command.SHIP_ITEMS]
                     //Command.STOCK_ITEM]
    resetTurnStatus()
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
        println "Supplier ${this.uuid} received ${it.type}"
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
          case Command.CALC_DEMAND:
            this.completeStep(Command.CALC_DEMAND)
            break

          case Command.CALC_NEEDS:
            // analyze - cash on hand, current payroll, supply costs, expected income
            def neededPayroll, neededSupplyCosts, expectedIncome

            this.completeStep(Command.CALC_NEEDS)
            break
          case Command.PURCHASE_SUPPLIES:
            this.completeStep(Command.PURCHASE_SUPPLIES)
            break
          case Command.PRODUCE_ITEMS:
            this.completeStep(Command.PRODUCE_ITEMS)
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
