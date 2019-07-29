package com.revolut.transfer.model

import java.math.BigDecimal

class MoneyTransferResult(val sourceAccountNumber: Int,
                          val destinationAccountNumber: Int,
                          val createdBy: String,
                          val amount: BigDecimal) {

}
