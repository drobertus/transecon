package com.mechzombie.transecon

import com.mechzombie.transecon.actors.HouseholdActor
import com.mechzombie.transecon.actors.Registry

class Main {

  //public static Thread theModel;
  //static final Thread mainThread = Thread.currentThread();
  static Registry reg = Registry.getInstance()
  static Scanner scanner
  static def keepGoing = true
  static def model = [:]

  Main () {

  }

  static void main(String[] args) {


    scanner = new Scanner(System.in);
    println "model name?"
    def modelName = scanner.nextLine()// 'What is model name?'
    model.modelName = modelName
    println("How many households?")

    def households = scanner.nextInt() //System.console().readLine 'How many households?'

    households.times {

      def id = UUID.randomUUID()
      def hh = new HouseholdActor()
      reg.addActor(hh)
      println "added household ${id}"
    }

    while (keepGoing) {
      def command = scanner.nextLine()
      def pieces = command.split(' ')

      switch (pieces[0]) {
        case 'help':
          println 'help - get help\nexit - stop app\nlist <types>\nmessage <id> <msg>\n'
          break
        case 'list':
          println 'not implemented'
          break
        case 'message':
          println 'not implemented'
          break
        case 'exit':
          keepGoing = false;
          break;
        default:

          break

      }
    }
  }

}

/*

Question: what do i need to expose?
parameters?


 */
