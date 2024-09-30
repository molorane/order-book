package com.valr.order_book.repository

import com.valr.order_book.entity.UserWallet
import org.springframework.data.jpa.repository.JpaRepository

interface UserWalletRepository : JpaRepository<UserWallet, Long>