package com.mechzombie.transecon

import com.mechzombie.transecon.actors.Registry
import spock.lang.Shared
import spock.lang.Specification

class BaseActorTest extends Specification {

  @Shared Registry reg = Registry.instance

  def cleanup() {
    reg.cleanup()
  }
}
