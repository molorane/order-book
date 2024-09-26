package com.valr.order_book.repository

import com.valr.order_book.entity.User
import com.valr.order_book.repository.projection.UserProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface UserRepository : JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.wallets w WHERE u.id = :id")
    fun findUserWithWallets(id: Long): Optional<UserProjection>
}