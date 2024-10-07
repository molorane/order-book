package com.valr.order_book.util

import com.valr.order_book.entity.enums.Currency
import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.exception.InvalidOrderException
import com.valr.order_book.model.CurrencyPairDto


// This is a pseudo-method, this can be read from appropriate table of currencies or through an API
fun matchBuyCurrency(currency: CurrencyPairDto): Currency {
    return when (currency) {
        CurrencyPairDto.XRPZAR, CurrencyPairDto.BTCZAR -> Currency.ZAR
        else -> throw InvalidOrderException("Invalid currency pair")
    }
}

// This is a pseudo-method, this can be read from appropriate table of currencies or through an API
fun matchSellCurrency(currency: CurrencyPairDto): Currency {
    return when (currency) {
        CurrencyPairDto.XRPZAR -> Currency.XRP
        CurrencyPairDto.BTCZAR -> Currency.BTC
        else -> throw InvalidOrderException("Invalid currency pair.")
    }
}

// This is a pseudo-method, this can be read from appropriate table of currencies or through an API
fun matchBuyCurrency(currency: CurrencyPair): Currency {
    return when (currency) {
        CurrencyPair.XRPZAR, CurrencyPair.BTCZAR -> Currency.ZAR
        else -> throw InvalidOrderException("Invalid currency pair")
    }
}

// This is a pseudo-method, this can be read from appropriate table of currencies or through an API
fun matchSellCurrency(currency: CurrencyPair): Currency {
    return when (currency) {
        CurrencyPair.XRPZAR -> Currency.XRP
        CurrencyPair.BTCZAR -> Currency.BTC
        else -> throw InvalidOrderException("Invalid currency pair.")
    }
}