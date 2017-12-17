package com.mechzombie.transecon.messages

class Message {

  def type
  def vals

  Message(Command type, vals = [:]) {

    def params = type.params
    def valid = true
    if(!vals.size() == params.size()) {
      valid = false
    }

    params.each {
      //println ("it= ${it.class}")
      if(vals instanceof String && !vals.get(it)) {
        valid = false
      }
    }

    if (valid) {
      this.type = type
      this.vals = vals
    }
    else {
      throw Exception("Message of type ${type} requires parameters ${type.params}")
    }
  }
}
