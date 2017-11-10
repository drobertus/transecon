package com.mechzombie.transecon.actors

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
          default:
            theResponse = "unrecognized Command"
            break
        }

        reply theResponse
      }
    }
  }
}
