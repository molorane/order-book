package com.valr.order_book.repository

import com.valr.order_book.entity.UserWallet
import com.valr.order_book.entity.enums.Currency
import com.valr.order_book.repository.projection.WalletBalance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface UserWalletRepository : JpaRepository<UserWallet, Long> {

    @Query(
        """
        SELECT    
            SUM(CASE WHEN uw.flowType = 'IN' THEN uw.quantity ELSE 0 END) AS totalIn,
            SUM(CASE WHEN uw.flowType = 'OUT' THEN uw.quantity ELSE 0 END) AS totalOut,
            (SUM(CASE WHEN uw.flowType = 'IN' THEN uw.quantity ELSE 0 END) - SUM(CASE WHEN uw.flowType = 'OUT' THEN uw.quantity ELSE 0 END)) AS quantityDifference
        FROM UserWallet uw
        WHERE uw.user.id = :userId AND uw.currency = :currency
    """
    )
    fun walletBalance(
        userId: Long,
        currency: Currency
    ): Optional<WalletBalance>
}