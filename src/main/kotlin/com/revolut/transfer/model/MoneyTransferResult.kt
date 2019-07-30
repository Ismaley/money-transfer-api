package com.revolut.transfer.model

import java.math.BigDecimal
import java.time.LocalDateTime

class MoneyTransferResult(val sourceAccountNumber: Int?,
                          val destinationAccountNumber: Int?,
                          val amount: BigDecimal?,
                          val createdAt: LocalDateTime? = LocalDateTime.now()) {

}
