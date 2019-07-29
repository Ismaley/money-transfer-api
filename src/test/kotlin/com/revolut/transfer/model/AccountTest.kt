package com.revolut.transfer.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AccountTest {

    @Test
    fun `should return true if account has enough balance`() {
        val transactions = listOf(AccountTransaction(transactionType = TransactionType.DEPOSIT, amount = BigDecimal(199.98)))
        val account = Account(transactions = transactions)

        Assertions.assertTrue(account.hasEnoughBalance(BigDecimal(199.97)))
    }

    @Test
    fun `should return false if account does not have enough balance`() {
        val transactions = listOf(AccountTransaction(transactionType = TransactionType.DEPOSIT, amount = BigDecimal(199.98)))
        val account = Account(transactions = transactions)

        Assertions.assertFalse(account.hasEnoughBalance(BigDecimal(199.99)))
    }
}