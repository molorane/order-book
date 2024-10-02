package com.valr.order_book.service.impl

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.TakerSide
import com.valr.order_book.exception.OrderProcessingException
import com.valr.order_book.service.OrderQueue
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.PriorityBlockingQueue
import kotlin.random.Random

@Component
class OrderQueueImpl : OrderQueue {

    val sellOrderComparator = Comparator<TradeOrder> { o1, o2 ->
        o1.price.compareTo(o2.price) // Ascending order by price
    }

    val buyOrderComparator = Comparator<TradeOrder> { o1, o2 ->
        o2.price.compareTo(o1.price) // Descending order by price
    }

    // Create a map to hold BlockingQueues
    val sellOrderMap: MutableMap<CurrencyPair, PriorityBlockingQueue<TradeOrder>> = mutableMapOf()

    // Create a map to hold BlockingQueues
    val buyOrderMap: MutableMap<CurrencyPair, PriorityBlockingQueue<TradeOrder>> = mutableMapOf()

    private val queue: BlockingQueue<TradeOrder> = LinkedBlockingQueue()

    // Create a map to hold live match invocations
    val orderProcess: MutableMap<CurrencyPair, Int> = mutableMapOf()

    override fun addOrder(newOrder: TradeOrder) {
        if (newOrder.takerSide == TakerSide.SELL) {
            val sellOrders = sellOrderMap[newOrder.currencyPair]
            // If sellOrders is null, new sell currency pair should be created in a map
            if (sellOrders == null) {
                val newCurrencyPairQueue = PriorityBlockingQueue(10_000, sellOrderComparator)
                newCurrencyPairQueue.put(newOrder)

                synchronized(this) {
                    if (sellOrderMap[newOrder.currencyPair] == null)
                        sellOrderMap[newOrder.currencyPair] = newCurrencyPairQueue
                    else
                        sellOrderMap[newOrder.currencyPair]?.put(newOrder)
                }

            } else {
                sellOrders.put(newOrder)
            }
        } else {
            val buyOrders = buyOrderMap[newOrder.currencyPair]
            // If buyOrders is null, new buy currency pair should be created in a map
            if (buyOrders == null) {
                val newCurrencyPairQueue = PriorityBlockingQueue(10_000, buyOrderComparator)
                newCurrencyPairQueue.put(newOrder)

                synchronized(this) {
                    if (buyOrderMap[newOrder.currencyPair] == null)
                        buyOrderMap[newOrder.currencyPair] = newCurrencyPairQueue
                    else
                        buyOrderMap[newOrder.currencyPair]?.put(newOrder)
                }

            } else {
                buyOrders.put(newOrder)
            }
        }
        queue.put(newOrder)
    }

    override fun matchOrders(currencyPair: CurrencyPair): Pair<TradeOrder, TradeOrder>? {
        val count = orderProcess[currencyPair] ?: 0

        // This means another thread with the same currency pair is currently doing order matching
        // Therefore, current thread must wait
        // If count is 0, current thread does not need to wait
        if(count > 0) {
            synchronized(this) {
                return match(currencyPair, count)
            }
        } else {
            return match(currencyPair, count)
        }
    }

    private fun match(currencyPair: CurrencyPair, count: Int) : Pair<TradeOrder, TradeOrder> {
        orderProcess[currencyPair] = count.plus(1)

        val sellOrders: PriorityBlockingQueue<TradeOrder> = sellOrderMap[currencyPair]
            ?: throw  OrderProcessingException("Currency $currencyPair does not have sell orders")

        val buyOrders = buyOrderMap[currencyPair]
            ?: throw  OrderProcessingException("Currency $currencyPair does not have buy orders")

        for (sellOrder in sellOrders) {
            for (buyOrder in buyOrders) {
                if(buyOrder.price >= sellOrder.price) {
                    // Remove the matched orders from the queues
                    buyOrders.remove(buyOrder)
                    sellOrders.remove(sellOrder)

                    orderProcess[currencyPair] = count.minus(1)
                    return sellOrder to buyOrder
                }
            }
        }

        throw OrderProcessingException("No matching orders for $currencyPair found")
    }

    /*
        This is a pseudo-method, that each worker thread must call to determine what orders to process
        My understanding is that there are situations where certain orders may need to be prioritized
        If there are more orders on BTC than any other crypto, the crypto exchange may need to prioritize those
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
        if (matchingCurrencyPairs.isNotEmpty()) {
            val matchPair = matchingCurrencyPairs[Random.nextInt(matchingCurrencyPairs.size)]
            return matchPair
        } else {
            // This means there are no matching orders
            return null
        }
    }
}