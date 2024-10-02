package com.valr.order_book.service.impl

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.TakerSide
import com.valr.order_book.exception.OrderProcessingException
import com.valr.order_book.service.OrderQueue
import org.springframework.stereotype.Component
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.random.Random

@Component
class OrderQueueImpl : OrderQueue {

    val sellOrderComparator = compareBy<TradeOrder> { it.price } // Sell orders sorted by price ascending

    // Buy orders sorted by price (descending) and quantity (descending) if price is equal
    val buyOrderComparator = compareByDescending<TradeOrder> { it.price }.thenByDescending { it.quantity }

    // Create a map to hold BlockingQueues
    val sellOrderMap: MutableMap<CurrencyPair, PriorityBlockingQueue<TradeOrder>> = mutableMapOf()

    // Create a map to hold BlockingQueues
    val buyOrderMap: MutableMap<CurrencyPair, PriorityBlockingQueue<TradeOrder>> = mutableMapOf()

    // Create a map to hold live match invocations
    val orderProcess: MutableMap<CurrencyPair, Boolean> = mutableMapOf()
    private val lock = ReentrantLock()

    override fun addOrder(newOrder: TradeOrder) {
        if (newOrder.takerSide == TakerSide.SELL) {
            val sellOrders = sellOrderMap[newOrder.currencyPair]
            // If sellOrders is null, new sell currency pair should be created in a map
            addOrder(sellOrderMap, newOrder, sellOrders, sellOrderComparator)
        } else {
            val buyOrders = buyOrderMap[newOrder.currencyPair]
            // If buyOrders is null, new buy currency pair should be created in a map
            addOrder(buyOrderMap, newOrder, buyOrders, buyOrderComparator)
        }
    }

    // This method is to avoid code duplication for adding sell orders and buy orders into a map
    private fun addOrder(
        orderMap: MutableMap<CurrencyPair, PriorityBlockingQueue<TradeOrder>>,
        newOrder: TradeOrder,
        orders: PriorityBlockingQueue<TradeOrder>?,
        comparator: Comparator<TradeOrder>
    ) {
        if (orders == null) {
            val newCurrencyPairQueue = PriorityBlockingQueue(10_000, comparator)
            newCurrencyPairQueue.put(newOrder)

            synchronized(this) {
                if (orderMap[newOrder.currencyPair] == null)
                    orderMap[newOrder.currencyPair] = newCurrencyPairQueue
                else
                    orderMap[newOrder.currencyPair]?.put(newOrder)
            }

        } else {
            orders.put(newOrder)
        }
    }

    override fun matchOrders(currencyPair: CurrencyPair): Pair<TradeOrder, TradeOrder>? {
        // This means another thread with the same currency pair is currently doing order matching
        // Therefore, current thread must wait

        // Use the lock to ensure that checking and updating the busy status is thread-safe
        lock.withLock {
            // Check if another thread is busy with the same currency pair
            val isThreadBusy = orderProcess[currencyPair] ?: false

            return if (isThreadBusy) {
                // Another thread is busy, so we will return null and not perform matching
                return null
            } else {
                // Set the current thread as busy for this currency pair
                orderProcess[currencyPair] = true

                try {
                    // Perform the match operation
                    match(currencyPair)
                } finally {
                    // Reset the state after matching is done
                    orderProcess[currencyPair] = false
                }
            }
        }
    }

    // This method is to avoid code duplication for adding sell orders and buy orders into a map
    private fun match(currencyPair: CurrencyPair): Pair<TradeOrder, TradeOrder> {
        val sellOrders: PriorityBlockingQueue<TradeOrder> = sellOrderMap[currencyPair]
            ?: throw OrderProcessingException("Currency $currencyPair does not have sell orders")

        val buyOrders = buyOrderMap[currencyPair]
            ?: throw OrderProcessingException("Currency $currencyPair does not have buy orders")

        for (sellOrder in sellOrders) {
            for (buyOrder in buyOrders) {
                if (buyOrder.price >= sellOrder.price) {
                    // Remove the matched orders from the queues
                    buyOrders.remove(buyOrder)
                    sellOrders.remove(sellOrder)

                    return sellOrder to buyOrder
                }
            }
        }

        throw OrderProcessingException("No matching orders for $currencyPair found")
    }

    /*
        This is a pseudo-method, that each worker thread must call to determine what currency pair
        This currency pair will be used to as a key to find orders that will be then be matched together
        My understanding is that there are situations where certain orders may need to be prioritized
        If there are more orders on BTC than any other crypto, the crypto exchange may need to prioritize those orders
        E.g. During a bull run, certain digital currency orders may surge and prioritizing them imply more profits for the business from trade fees
        However, on this implementation, I randomly select currency pairs to ensure that each currency pair in the map has an equal chance of being selected
        Also, I ensure that a currency pair selected has both sell orders and buy orders
     */
    override fun getSellCurrencyPair(): CurrencyPair? {
        // If the map is empty, return null
        if (sellOrderMap.isEmpty()) {
            return null
        }

        // Find the intersection of orders
        val matchingCurrencyPairs = sellOrderMap.keys.intersect(buyOrderMap.keys).toList()

        // Check if there are matching orders
        return if (matchingCurrencyPairs.isNotEmpty()) {
            matchingCurrencyPairs[Random.nextInt(matchingCurrencyPairs.size)]
        } else {
            // This means there are no matching orders for a currency pair
            // This will happen if
            // 1. We have sell orders and no buy orders or the vice versa
            // 2. If we have no orders at all
            null
        }
    }
}