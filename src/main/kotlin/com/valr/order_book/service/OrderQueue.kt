package com.valr.order_book.service

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.CurrencyPair
import java.util.concurrent.PriorityBlockingQueue

interface OrderQueue {

    fun getSellOrderQueue(): MutableMap<CurrencyPair, PriorityBlockingQueue<TradeOrder>>;

    fun getBuyOrderQueue(): MutableMap<CurrencyPair, PriorityBlockingQueue<TradeOrder>>;

    fun addOrder(newOrder: TradeOrder)

    fun matchOrders(currencyPair: CurrencyPair): Pair<TradeOrder, TradeOrder>?

    fun getSellCurrencyPair(): CurrencyPair?
}