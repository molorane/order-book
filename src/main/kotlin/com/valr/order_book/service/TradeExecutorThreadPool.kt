package com.valr.order_book.service

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// This class generate threads that execute trades

@Component
class TradeExecutorThreadPool(private val tradeExecutor: TradeExecutor) {

    private val logger = LoggerFactory.getLogger(TradeExecutorThreadPool::class.java)
    private lateinit var executorService: ExecutorService

    @PostConstruct
    private fun startWorkerThreads() {
        logger.info("============Starting worker threads to process trade orders============")
        executorService = Executors.newFixedThreadPool(2)
        repeat(2) {
            executorService.submit(tradeExecutor)
        }
    }

    @PreDestroy
    private fun stopWorkerThreads() {
        logger.info("============Shutting down executor service============")
        executorService.shutdownNow()
    }
}