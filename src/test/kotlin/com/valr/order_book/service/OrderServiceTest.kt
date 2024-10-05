package com.valr.order_book.service

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.User
import com.valr.order_book.entity.enums.Currency
import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.TakerSide
import com.valr.order_book.exception.InsufficientFundsException
import com.valr.order_book.exception.InvalidOrderException
import com.valr.order_book.model.*
import com.valr.order_book.repository.OrderRepository
import com.valr.order_book.repository.UserRepository
import com.valr.order_book.repository.UserWalletRepository
import com.valr.order_book.repository.projection.WalletBalance
import com.valr.order_book.service.impl.OrderServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class OrderServiceTest {

    @Mock
    lateinit var repository: OrderRepository

    @Mock
    lateinit var userWalletRepository: UserWalletRepository

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var orderQueue: OrderQueue

    @InjectMocks
    lateinit var orderService: OrderServiceImpl

    private val sellRequest = OrderRequestDto(
        side = SideDto.SELL,
        quantity = BigDecimal("10.00000000"),
        price = BigDecimal("10.45000000"),
        pair = CurrencyPairDto.XRPZAR,
        customerOrderId = "123",
    )

    private val buyRequest = OrderRequestDto(
        side = SideDto.BUY,
        quantity = BigDecimal("10.00000000"),
        price = BigDecimal("10.45000000"),
        pair = CurrencyPairDto.XRPZAR,
        customerOrderId = "123",
    )

    @Test
    fun `given validOrder then return true`() {
        // Arrange

        // Act
        val result = orderService.validOrder(sellRequest)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `given matchBuyCurrency with valid currency then return true`() {
        // Arrange

        // Act
        val btcZAR = orderService.matchBuyCurrency(CurrencyPairDto.BTCZAR)
        val xrpZAR = orderService.matchBuyCurrency(CurrencyPairDto.XRPZAR)

        // Assert
        assertThat(btcZAR).isEqualTo(Currency.ZAR)
        assertThat(xrpZAR).isEqualTo(Currency.ZAR)
    }

    @Test
    fun `given matchBuyCurrency with in-valid currency then should throw InvalidOrderException`() {
        // Arrange

        // Act
        val exception = assertThrows<InvalidOrderException> {
            orderService.matchBuyCurrency(CurrencyPairDto.SHIBZAR)
        }

        // Assert
        assertEquals("Invalid currency pair", exception.message)
    }

    @Test
    fun `given matchSellCurrency with valid currency then return currency`() {
        // Arrange

        // Act
        val btcZAR = orderService.matchSellCurrency(CurrencyPairDto.BTCZAR)
        val xrpZAR = orderService.matchSellCurrency(CurrencyPairDto.XRPZAR)

        // Assert
        assertThat(btcZAR).isEqualTo(Currency.BTC)
        assertThat(xrpZAR).isEqualTo(Currency.XRP)
    }

    @Test
    fun `given matchSellCurrency with in-valid currency then should throw InvalidOrderException`() {
        // Arrange

        // Act
        val exception = assertThrows<InvalidOrderException> {
            orderService.matchSellCurrency(CurrencyPairDto.SHIBZAR)
        }

        // Assert
        assertEquals("Invalid currency pair.", exception.message)
    }

    @Test
    fun `given fundsAvailable with valid userId and request then should return true`() {
        // Arrange
        val walletBalance = mock(WalletBalance::class.java)
        lenient().`when`(walletBalance.getTotalIn()).thenReturn(BigDecimal("350.00000000"))
        lenient().`when`(walletBalance.getTotalOut()).thenReturn(BigDecimal("10.45000000"))
        lenient().`when`(walletBalance.getQuantityDifference()).thenReturn(BigDecimal("340.45000000"))

        `when`(userWalletRepository.walletBalance(123L, Currency.XRP)).thenReturn(
            Optional.of(walletBalance)
        )

        // Act
        val fundsAvailable = orderService.fundsAvailable(123L, sellRequest)

        // Assert
        assertTrue(fundsAvailable)
    }

    @Test
    fun `given fundsAvailable with valid userId and sell request then should return false`() {
        // Arrange
        val walletBalance = mock(WalletBalance::class.java)
        lenient().`when`(walletBalance.getTotalIn()).thenReturn(BigDecimal("350.00000000"))
        lenient().`when`(walletBalance.getTotalOut()).thenReturn(BigDecimal("10.45000000"))
        lenient().`when`(walletBalance.getQuantityDifference()).thenReturn(BigDecimal("5.000000"))

        `when`(userWalletRepository.walletBalance(123L, Currency.XRP)).thenReturn(
            Optional.of(walletBalance)
        )

        // Act
        val fundsAvailable = orderService.fundsAvailable(123L, sellRequest)

        // Assert
        assertFalse(fundsAvailable)
    }

    @Test
    fun `given fundsAvailable with valid userId and buy request then should return false`() {
        // Arrange
        val walletBalance = mock(WalletBalance::class.java)
        lenient().`when`(walletBalance.getTotalIn()).thenReturn(BigDecimal("350.00000000"))
        lenient().`when`(walletBalance.getTotalOut()).thenReturn(BigDecimal("10.45000000"))
        lenient().`when`(walletBalance.getQuantityDifference()).thenReturn(BigDecimal("5.000000"))

        `when`(userWalletRepository.walletBalance(123L, Currency.ZAR)).thenReturn(
            Optional.of(walletBalance)
        )

        // Act
        val fundsAvailable = orderService.fundsAvailable(123L, buyRequest)

        // Assert
        assertFalse(fundsAvailable)
    }

    @Test
    fun `given placeOrder with valid userId and sell request then should throw insufficient balance`() {
        // Arrange
        val walletBalance = mock(WalletBalance::class.java)
        lenient().`when`(walletBalance.getTotalIn()).thenReturn(BigDecimal("350.00000000"))
        lenient().`when`(walletBalance.getTotalOut()).thenReturn(BigDecimal("10.45000000"))
        lenient().`when`(walletBalance.getQuantityDifference()).thenReturn(BigDecimal("5.000000"))

        val user = User(
            id = 123L,
            firstName = "Mothusi"
        )

        `when`(userWalletRepository.walletBalance(123L, Currency.XRP)).thenReturn(
            Optional.of(walletBalance)
        )

        lenient().`when`(userRepository.findById(123L)).thenReturn(
            Optional.of(user)
        )

        // Act
        val exception = assertThrows<InsufficientFundsException> {
            orderService.placeOrder(123L, sellRequest)
        }

        // Assert
        assertEquals("Insufficient balance.", exception.message)
    }

    @Test
    fun `given placeOrder with valid userId and sell request then should place an order`() {
        // Arrange
        val walletBalance = mock(WalletBalance::class.java)
        lenient().`when`(walletBalance.getTotalIn()).thenReturn(BigDecimal("350.00000000"))
        lenient().`when`(walletBalance.getTotalOut()).thenReturn(BigDecimal("10.45000000"))
        lenient().`when`(walletBalance.getQuantityDifference()).thenReturn(BigDecimal("340.450000"))

        val user = User(
            id = 123L,
            firstName = "Mothusi"
        )

        val order = TradeOrder(
            sequenceId = 1L,
            id = "id",
            user = user,
            takerSide = TakerSide.SELL,
            quantity = BigDecimal("10.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "123",
        )


        val orderResponse = OrderResponseDto(
            sequenceId = 1,
            id = "id",
            side = SideDto.SELL,
            quantity = BigDecimal("10.00000000"),
            price = BigDecimal("10.45000000"),
            quoteVolume = BigDecimal("0"),
            matchedQuantity = BigDecimal("0"),
            pair = CurrencyPairDto.XRPZAR,
            status = StatusDto.PLACED,
            orderType = OrderTypeDto.LIMIT_ORDER,
            postOnly = false,
            customerOrderId = null,
            timeInForce = TimeInForceDto.GTC,
            allowMargin = false,
            reduceOnly = false
        )

        `when`(userWalletRepository.walletBalance(123L, Currency.XRP)).thenReturn(
            Optional.of(walletBalance)
        )

        `when`(userRepository.findById(123L)).thenReturn(
            Optional.of(user)
        )

        lenient().`when`(repository.save(any())).thenReturn(
            order
        )

        // Act
        val newOrder = orderService.placeOrder(123L, sellRequest)

        // Assert
        assertThat(newOrder)
            .usingRecursiveComparison()
            .ignoringFields("tradedAt")
            .isEqualTo(orderResponse)
    }
}