package com.mechzombie.transecon

import com.mechzombie.transecon.actors.BaseEconActor
import com.mechzombie.transecon.actors.HouseholdActor

class Main implements Runnable{

    public static Thread theModel;
    static final Thread mainThread = Thread.currentThread();
    WorldModel world;
    Scanner scanner
    def keepGoing = true

    Main( WorldModel wm) {
        this.world = wm
    }

    static void main(String[] args) {
        def wm = new WorldModel()

        Scanner sc = new Scanner(System.in);
        println "model name?"
        def modelName = sc.nextLine()// 'What is model name?'
        wm.modelName = modelName
        println("households?")


        def households = sc.nextInt() //System.console().readLine 'How many households?'

        if (households > 10) {
            households = 10
        }
        households.times {
            def id = UUID.randomUUID()
            def hh = new HouseholdActor(uuid: id)
            wm.households.put(id, hh)
            println "added household ${id}"
        }




        def main = new Main(wm)
        main.scanner = sc

        theModel = new Thread(main).run()

//        def act1 = new HouseholdActor()
//        act1.start()
//        def response = null
//        act1.sendAndContinue 'Message', {reply ->
//            println "I received reply: $reply"
//            response = reply
//        }
//
//
//        while(response == null) {
//            println('waiting')
//            Thread.sleep (20)
//        }
//
//        println("got response!!")
    }

    void run() {

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
