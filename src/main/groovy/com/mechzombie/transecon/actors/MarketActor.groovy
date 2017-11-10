package com.mechzombie.transecon.actors

import com.mechzombie.transecon.messages.Product

class MarketActor extends BaseEconActor {

  Date endTime
  UUID initiator
  def bidders = [:]
  def result
  def product

  MarketActor(UUID id, UUID initiator, Date endTime, Product product) {
    super(id)
    this.initiator = initiator
    this.endTime = endTime
    println("id ${id}")
  }

  @Override
  def status() {
    def status = builder.econactor {
      type this.class
      id this.uuid
      initiator this.initiator
      endTime this.endTime
      bidders this.bidders
      result result
    }
    println "here: ${status.toString()}"
    return status.toString()
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
          default:
            theResponse = "unrecognized Command"
            break
        }

        reply theResponse
      }
    }
  }
}
