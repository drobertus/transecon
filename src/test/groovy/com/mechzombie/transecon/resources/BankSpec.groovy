package com.mechzombie.transecon.resources

import spock.lang.Specification

class BankSpec extends Specification {


  def cleanup() {
    Bank.account.clear()
    Bank.privateLookup.clear()
  }

  def "test the creation of an account"() {

    def depositAmt = 7.0

    when: "an account is created"
    def startUUID= UUID.randomUUID()
    def privateID = Bank.createAccount(startUUID)

    then: "objects are created in maps"

    Bank.privateLookup.size() == 1
    Bank.account.size() == 1

    when: "we look for a private Lookup we need a private key "
    def pubVal = Bank.privateLookup.get(privateID)

    then: " and the mapping to the public key is correct"
    assert startUUID == pubVal

    when: "making a deposit requires the public key"
    def canDeposit = Bank.deposit(startUUID, depositAmt)

    then: "the deposit passes"
    assert canDeposit == true
    Bank.account.get(startUUID).get() == depositAmt

    when: "a deposit it attmpted with any other id"
    canDeposit = Bank.deposit(UUID.randomUUID(), depositAmt)

    then: "the deposit will fail"
    canDeposit == false


  }

  def "test transfer capabilities"() {

    def user1 = UUID.randomUUID()
    def private1 = Bank.createAccount(user1)
    def user2 = UUID.randomUUID()
    def private2 = Bank.createAccount(user2)

    when: "we attempt to transfer from a non-existent account"
    def transferAttempt = Bank.transferFunds(UUID.randomUUID(), user2, 4.5d)

    then:
    assert transferAttempt == false

    when: "we attempt to transfer TO a non-existent account"
    transferAttempt = Bank.transferFunds(private1, UUID.randomUUID(), 4.5d)

    then:
    assert transferAttempt == false

    when: "we transfer more money than is available"
    transferAttempt = Bank.transferFunds(private1, user2, 4.5d)

    then:
    assert transferAttempt == false

    when: "we fund the source account"
    def funding = Bank.deposit(user1, 5.0d)

    then:
    assert funding

    when: "when the transfer is attempted"
    transferAttempt = Bank.transferFunds(private1, user2, 4.5d)

    then:
    assert transferAttempt == true
    assert Bank.account.get(user1).get() == 0.5d
  }

  def "test for the locks on accounts" () {
    def startAmt = 100.0
    def acct = UUID.randomUUID()
    def privateId = Bank.createAccount(acct)
    when:
    def dep = Bank.deposit(acct, startAmt)

    then:
    assert dep

    when: 'try to lock on more than allowed'
    def lock = Bank.holdDebitAmount(acct, 200.0)

    then: ' get an error message back'
    lock.msg == 'NSF'
    lock.amount == 0.0
    lock.accountSource == acct
    Bank.getAccountValue(acct) == startAmt

    when: 'ask for a lock for a legal amount'
    lock = Bank.holdDebitAmount(acct, 29.0)

    then:
    lock.msg == null
    lock.amount == 29.0
    lock.accountSource == acct
    Bank.getAccountValue(acct) == startAmt - 29.0

    when: 'we complete the transaction'
    def completed = Bank.completeDebit(lock.uuid)

    then:
    assert completed
    Bank.getAccountValue(acct) == startAmt - 29.0
    Bank.locks.size() == 0

  }

}
