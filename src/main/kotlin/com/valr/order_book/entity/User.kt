package com.valr.order_book.entity

import jakarta.persistence.*

@Entity
@Table(name = "tbl_user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val firstName: String,

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    val wallets: List<UserWallet>
) {
    override fun toString(): String {
        return "User(id=$id, firstName=$firstName)"
    }
}