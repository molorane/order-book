package com.valr.order_book.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TradeExecutor(
    private val orderQueue: OrderQueue,
    private val tradeWorkerService: TradeWorkerService
) : Runnable {

    private val logger = LoggerFactory.getLogger(TradeExecutor::class.java)

    override fun run() {
        logger.info("==Worker started==")
        while (true) {
            try {
                val currencyPair = orderQueue.getSellCurrencyPair()
                if (currencyPair != null) {
                    val matchedOrders = orderQueue.matchOrders(currencyPair)
                    if (matchedOrders != null) {
                        val (buyOrder, sellOrder) = matchedOrders
                        tradeWorkerService.executeTrade(buyOrder, sellOrder)
                    } else {
                        logger.info("==Match pair $currencyPair ==")
                        Thread.sleep(500)
                    }
                }
                logger.info("==No orders==")
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                logger.error("Worker interrupted", e)
            } catch (e: Exception) {
                logger.error("Worker interrupted", e)
            }
        }
    }
}