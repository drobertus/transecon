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
  }

  def employHousehold(UUID uuid, int monthlyWage) {
    inputs.labor << [uuid, monthlyWage]
  }

  @Override
  def status() {
    return null
  }

  @Override
  protected void act() {
    loop {
      react {
        def theResponse
        switch (it.type) {
          case Command.STATUS:
            theResponse = status()
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
}
