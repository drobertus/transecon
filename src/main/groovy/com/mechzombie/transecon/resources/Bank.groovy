package com.mechzombie.transecon.resources

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator


class Bank {

  protected static HashMap<UUID, AtomicReference<Double>> account = new HashMap<UUID, AtomicReference<Double>>()
  protected static Map<UUID, UUID> privateLookup = new HashMap<UUID, UUID>()

  protected static ConcurrentHashMap<UUID, Object> locks = new ConcurrentHashMap<UUID, Object>()

  static Double getAccountValue(UUID uuid){
    return account.get(uuid).get()
  }
  /**
  *
  * @param publicUUID required for deposits
  * @return privateUUID required for drawing on account
  */
  static UUID createAccount(publicUUID) {
    def privateID = UUID.randomUUID()
    privateLookup.put(privateID, publicUUID)
    account.put(publicUUID, new AtomicReference<Double>(0.0d))
    return privateID
  }


  static boolean transferFunds(UUID privateSource, UUID publicDeposit, Double amount) {
    UUID sourceAccount = privateLookup.get(privateSource)
    boolean response = false
    if(sourceAccount) {
      account.get(sourceAccount).updateAndGet(new UnaryOperator<Double>() {
        @Override
        Double apply(Double sourceAccountValue) {
          if (sourceAccountValue >= amount) {
            def madeDeposit = Bank.deposit(publicDeposit, amount)
            println("made deposit side of transfer = ${madeDeposit}")
            if (madeDeposit) {
              sourceAccountValue = sourceAccountValue - amount
              response = true
            }
          }
          println("returning val = ${sourceAccountValue}, responseval = ${response}")
          return sourceAccountValue
        }
      })
    }
    println("rsp ${response}")
    return response
  }
  /**
   * Synchronized access
   * @param uuid
   * @param amount
   * @return
   */
  static boolean deposit(UUID uuid, Double amount) {
    AtomicReference<Double> val = account.get(uuid)

    if (val != null) {
      println("val= ${val}")
      val.updateAndGet(new UnaryOperator<Double>() {
        @Override
        Double apply(Double aDouble) {
          println ("adouble = ${aDouble}")
          aDouble = aDouble.doubleValue() + amount
          println("modAmt = ${aDouble}")
          return aDouble
        }
      }) // (value -> return (value + amount))
      return true
    }else {
      false
    }
  }

  static UUID holdDebitAmount(UUID accountToLock, Double amountTolock) {

    def theLock = null
    AtomicReference<Double> val = account.get(accountToLock)
    if (val != null) {
      val.getAndUpdate(new UnaryOperator<Double>() {
        @Override
        Double apply(Double accountVal) {
          if (accountVal >= amountTolock) {
            accountVal = accountVal - amountTolock
            theLock = UUID.randomUUID()
            locks.put(theLock, [account: accountToLock, amount: amountTolock])
          }
          return accountVal
        }
      })
    }

    return theLock

  }

  static boolean completeDebit(UUID lockId){
    def theTrans = locks.get(lockId)
    if(theTrans) {
      locks.remove(theTrans)
      return true
    }
    return false
  }

  static boolean cancelLock(UUID lockId) {
    def theTrans = locks.get(lockId)
    if(theTrans) {
      Bank.deposit(theTrans.account, theTrans.amount)
      locks.remove(theTrans)

      return true
    }
    return false
  }

  static def clear() {
    account.clear()
    privateLookup.clear()
  }


}