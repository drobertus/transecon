package com.mechzombie.transecon.messages

class Message {

  def type
  def vals

  Message(Command type, vals = [:]) {

    def params = type.params
    def valid = true
    if(!vals.size() == params.size()) {
      valid = false
      println("expected ${params} but got ${vals}")
    }

    params.each {
      if (!vals.get(it)) {
        println("missing ${it}")
        valid = false
      }
    }

    if (valid) {
      this.type = type
      this.vals = vals
    }
    else{
      throw Exception("Message of type ${type} requires parameters ${type.params}")
    }
  }
}
