package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message


class SupplierActor extends BaseEconActor{

  def output
  def inputs = [:]
  def resources = [:]
  def money = 0

  SupplierActor(UUID id) {
    super(id)
    inputs.labor = [:]
    println ("supplier ${uuid}")
  }

  def employHousehold(UUID uuid, int monthlyWage) {
    getEmployeePayrole().put(uuid, monthlyWage)
  }

  @Override
  def status() {
    def status
    try {
      status = builder.econactor {
        type this.class
        id this.uuid
        inputs this.inputs
        resources this.resources
        money this.money
        transactions this.transactions
      }
    }
    catch(Exception e) {
      println "err ${e}"
    }
    //println "here: ${status.toString()}"
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
            getEmployeePayrole().each { k, v ->
              if (money > v) {
                payroll.put(k, sendMoney(k, v, 'payroll'))
                money -= v
              }else {
                println("Payroll for ${k} of ${v} unable to be met by supplier")
              }
            }

            payroll.keySet().each {
              payroll.get(it).get()
            }
            theResponse = 'OK'
            break
          case Command.SEND_MONEY:
            def from = it.vals.from
            def amount = it.vals.amount
            money += amount
            def reason = it.vals.reason


            theResponse = "OK"
            break

          case 'run_turn':
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

  def getEmployeePayrole() {
    return inputs.labor
  }
}
