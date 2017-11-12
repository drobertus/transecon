package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Command
import com.mechzombie.transecon.messages.Message


class SupplierActor extends BaseEconActor{

  def output
  def unitInput = [:]
  def externalInputs = [:]
  def resources = [:]

  SupplierActor(UUID id) {
    super(id)
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
        switch (it.name) {
          case 'status':
            theResponse = status()
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
