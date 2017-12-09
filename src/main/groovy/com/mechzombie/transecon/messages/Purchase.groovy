package com.mechzombie.transecon.messages

class Purchase {
  String product
  Map<UUID, Map<Double, Integer>> bought = [:]

  int totalBought = 0
  double spentAmount = 0.0

  def addCountFromSupplier(UUID uuid, double price, int count) {
    def supplier = bought.get(uuid)
    if (!supplier) {
      supplier = [:]// new Map<Double, Integer>()
      bought.put(uuid, supplier)
    }
    def atPrice = supplier.get(price) ? supplier.get(price) + count : count
    supplier.put(price, atPrice)
    totalBought += atPrice
    spentAmount += (atPrice * price)
    return atPrice
  }

  int countBought() {
    return totalBought
  }

  double totalSpent() {
    return spentAmount
  }

  Map<UUID, Double> getSuppliersAndAmountsToPay() {
    def suppliersAndAmt = [:]
    bought.each {key, val ->
      def amt = 0.0
      val.each { price, qty ->
        amt += qty * price
      }
      suppliersAndAmt.put(key, amt)
    }
    return suppliersAndAmt
  }

}
