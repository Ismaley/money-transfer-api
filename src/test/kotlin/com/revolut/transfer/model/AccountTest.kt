package com.revolut.transfer.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AccountTest {

    @Test
    fun `should return true if account has enough balance`() {
        val account = Account(balance = BigDecimal(200))

        Assertions.assertTrue(account.hasEnoughBalance(BigDecimal(199.97)))
    }

    @Test
    fun `should return false if account does not have enough balance`() {
        val account =  Account(balance = BigDecimal(50))

        Assertions.assertFalse(account.hasEnoughBalance(BigDecimal(199.99)))
    }
}