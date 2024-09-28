package com.valr.order_book.integration


import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderIntegrationTest(@Autowired val restTemplate: TestRestTemplate) {

    @Test
    fun `Assert order-book, endpoint exist`() {
        val entity = restTemplate.getForEntity<String>("/v1/BTCZAR/orderbook")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        // assertThat(entity.body).contains("<h1>Blog</h1>", "Lorem")
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup(): Unit {
            println(">> Setup")
        }

        @JvmStatic
        @AfterAll
        fun teardown(): Unit {
            println(">> Tear down")
        }
    }
}