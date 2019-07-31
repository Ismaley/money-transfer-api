package com.revolut.transfer.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class MoneyTransferResult(
    val sourceAccountNumber: Int?,
    val destinationAccountNumber: Int?,
    val amount: BigDecimal?,
    val createdAt: LocalDateTime? = LocalDateTime.now()
)
