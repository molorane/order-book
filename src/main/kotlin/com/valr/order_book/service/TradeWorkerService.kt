package com.valr.order_book.service

import com.valr.order_book.entity.TradeOrder


interface TradeWorkerService {
    fun executeTrade(sellOrder: TradeOrder, buyOrder: TradeOrder)
}