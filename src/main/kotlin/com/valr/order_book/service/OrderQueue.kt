package com.valr.order_book.service

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.CurrencyPair

interface OrderQueue {
    fun addOrder(newOrder: TradeOrder)

    fun matchOrders(currencyPair: CurrencyPair): Pair<TradeOrder, TradeOrder>?

    fun getSellCurrencyPair(): CurrencyPair?
}