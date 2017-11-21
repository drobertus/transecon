package com.mechzombie.transecon.messages

enum Command {

  /*
  BID('bid', ['bidder', 'amount']),
  CREATE_MARKET('market', ['product', 'end_time', 'price']),
  STATUS('status'),
  TRANSACTION('transaction', ['amount', 'type']),
  END_MARKET('end_market'),
  */
  //create market - suppler, product, end time, price (nullable?)
  TAKE_TURN('take_turn', ['turnNum']),
  STOCK_ITEM('stock_item', ['producer', 'product', 'price']), //[UUID, String, int]), //producer, product, price
  PRICE_ITEM('price_item', ['product']), //item to price
  PURCHASE_ITEM('purchase_item', ['market', 'product', 'price']), //purchaser, product, price
  FULFILL_ORDER('fulfill_order', ['buyer', 'product', 'price']),
  SEND_MONEY('send_money', ['from', 'amount', 'reason']),
  STATUS('status'),
  RUN_PAYROLL('run_payroll'),
  CALC_DEMAND('calc_demand'),
  CALC_NEEDS('calc_needs'),
  PURCHASE_SUPPLIES('purchae_supplies'),
  PRODUCE_ITEMS('produce_items'),
  SHIP_ITEMS('ship_items'),
  FINANCE_TURN('finance_turn'),
  CONSUME('consume')


  def name
  def params = []

  Command(name, vals = []) {
    this.name = name
    this.params = vals
  }

}