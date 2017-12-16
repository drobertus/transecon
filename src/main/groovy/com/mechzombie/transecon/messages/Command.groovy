package com.mechzombie.transecon.messages

import com.mechzombie.transecon.messages.dtos.Order

enum Command {

  TAKE_TURN('take_turn', ['turnNum']),
  STOCK_ITEM('stock_item', ['producer', 'product', 'price', 'quantity']), //[UUID, String, int]), //producer, product, price
  PRICE_ITEM('price_item', ['product']), //item to price
  PURCHASE_ITEM('purchase_item', ['market', 'product', 'price']), //purchaser, product, price
  FULFILL_ORDER('fulfill_order', [Order]),
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