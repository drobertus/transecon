package com.mechzombie.transecon.resources

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

@Slf4j
@CompileStatic
class Bank {

  protected static HashMap<UUID, AtomicReference<Double>> account = new HashMap<UUID, AtomicReference<Double>>()
  protected static Map<UUID, UUID> privateLookup = new HashMap<UUID, UUID>()

  static ConcurrentHashMap<UUID, AccountLock> locks = [:] as ConcurrentHashMap

  static Double getAccountValue(UUID uuid){
    return account.get(uuid).get()
  }
  /**
  *
  * @param publicUUID required for deposits
  * @return privateUUID required for drawing on account
  */
  static UUID createAccount(UUID publicUUID) {
    def privateID = UUID.randomUUID()
    privateLookup.put(privateID, publicUUID)
    account.put(publicUUID, new AtomicReference<Double>(0.0d))
    return privateID
  }

  /**
   * Transfer funds directly from one account to another,return true
   *
   * @param privateSource - private UUID for the source Account
   * @param publicDeposit - public UUID for the recipient
   * @param amount - the amont to transfer
   * @return Boolean - true if succeeds, false if fails
   */
  static boolean transferFunds(UUID privateSource, UUID publicDeposit, Double amount) {

    UUID sourceAccount = privateLookup.get(privateSource)
    log.info("transfering funds from ${privateSource}/${sourceAccount} to other in amt ${amount}")
    boolean response = false
    if(sourceAccount) {
      account.get(sourceAccount).updateAndGet(new UnaryOperator<Double>() {
        @Override
        Double apply(Double sourceAccountValue) {
          if (sourceAccountValue >= amount) {
            def madeDeposit = deposit(publicDeposit, amount)
            log.debug("made deposit side of transfer = ${madeDeposit}")
            if (madeDeposit) {
              sourceAccountValue = sourceAccountValue - amount
              response = true
            }
          }
          log.debug("returning val = ${sourceAccountValue}, responseval = ${response}")
          return sourceAccountValue
        }
      })
    }
    return response
  }
  /**
   * Synchronized access
   * @param uuid
   * @param amount
   * @return boolean - true if succeeds
   */
  static boolean deposit(UUID uuid, Double amount) {
    AtomicReference<Double> val = account.get(uuid)
    if (val != null) {
      val.updateAndGet(new UnaryOperator<Double>() {
        @Override
        Double apply(Double aDouble) {
          aDouble = aDouble.doubleValue() + amount
          return aDouble
        }
      })
      return true
    }else {
      false
    }
  }

  static AccountLock holdDebitAmount(UUID accountToLock, Double amountTolock) {

    def theLock = new AccountLock(accountToLock)
    AtomicReference<Double> val = account.get(accountToLock)
    if (val != null) {
      val.getAndUpdate(new UnaryOperator<Double>() {
        @Override
        Double apply(Double accountVal) {
          if (accountVal >= amountTolock) {
            theLock.amount = amountTolock
            accountVal = accountVal - amountTolock
            log.info("adding lock ${theLock.uuid}")
            locks.put(theLock.uuid, theLock)
          } else {
            theLock.msg = 'NSF'
          }
          return accountVal
        }
      })
    }
    return theLock
  }

  static boolean completeDebit(AccountLock lock) {
    return completeDebit(lock.uuid)
  }

  static boolean completeDebit(UUID lockId){

    def theTrans = locks.get(lockId)
    if(theTrans) {
      log.debug("killing lock with a completion ${lockId}")
      locks.remove(lockId)
      return true
    }
    return false
  }

  static boolean cancelLock(AccountLock lock) {
    return cancelLock(lock.uuid)
  }

  static boolean cancelLock(UUID lockId) {
    AccountLock theTrans = locks.get(lockId)
    if(theTrans) {
      deposit(theTrans.accountSource, theTrans.amount)
      log.debug("killing lock as part of cancelation ${lockId}")
      locks.remove(lockId)
      return true
    }
    return false
  }

  static def clear() {
    account.clear()
    privateLookup.clear()
    locks.clear()
  }


}
