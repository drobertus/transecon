package com.mechzombie.transecon.messages

class Transaction {
  TransactionType type //sale or purchase
  def exchanged
  double amount
  Date date
  UUID buyer
  UUID seller

  enum TransactionType {
    PUCHASE,
    SALE
  }

}
