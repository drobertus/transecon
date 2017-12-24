package com.mechzombie.transecon.actors

import groovy.json.StreamingJsonBuilder

trait ToJson {

  def toJsonString() {

    return ((BaseEconActor)this).status()

//
//    new StringWriter().with { sw ->
//      StreamingJsonBuilder builder = new StreamingJsonBuilder(sw)
//
//      def content = ((BaseEconActor)this).status();
//      builder.
//  //    println(content.writer.toString())
//    //  builder.sw.toString() content.writer.toString()
//
//      return sw.toString()
//    }
  }
  def toJson() {
    return ((BaseEconActor)this).asJson()
  }
}
