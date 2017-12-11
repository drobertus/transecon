package com.mechzombie.transecon.resources

class AccountLock {

  UUID uuid
  Double amount = 0.0d
  UUID accountSource
  Date creation
  String msg

  AccountLock(source) {
    uuid = UUID.randomUUID()
    accountSource = source
  }
}
