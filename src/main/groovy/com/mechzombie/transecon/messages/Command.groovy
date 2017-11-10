package com.mechzombie.transecon.messages

enum Command {

  BID('bid', ['bidder', 'amount']),
  CREATE_MARKET('market', ['product', 'end_time', 'price']),
  STATUS('status'),
  TRANSACTION('transaction', ['amount', 'type']),
  END_MARKET('end_market')
  //create market - suppler, product, end time, price (nullable?)

  def name, args

  Command(name, args = String[]) {
    this.name = name
    this.args = args
  }

}