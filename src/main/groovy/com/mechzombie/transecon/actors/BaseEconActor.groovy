package com.mechzombie.transecon.actors

import groovyx.gpars.actor.DefaultActor

abstract class BaseEconActor extends DefaultActor {

    UUID uuid

    @Override
    protected void act() {
        loop {
            react {
                println "it is $it"
                reply "Woohoo!!"
            }
        }
    }
}
