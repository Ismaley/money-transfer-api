package com.revolut.transfer.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.revolut.transfer.model.Account
import com.revolut.transfer.model.AccountTransaction
import com.revolut.transfer.model.MoneyTransferResult
import com.revolut.transfer.model.User
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.time.LocalDateTime

data class AccountRepresentation(val number: Int,
                                 val balance: BigDecimal,
                                 @JsonFormat(shape = JsonFormat.Shape.STRING,
                                     pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
                                 ) val createdAt: LocalDateTime) {

    constructor(account: Account) : this(account.number!!, account.balance!!, account.createdAt!!)
}

data class UserRepresentation(val id: String,
                              val name: String,
                              val documentNumber: String,
                              @JsonFormat(shape = JsonFormat.Shape.STRING,
                              pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
                              ) val createdAt: LocalDateTime) {

    constructor(user: User) : this(user.id!!, user.name!!, user.documentNumber!!, user.createdAt!!)
}

data class AccountTransactionRepresentation(@JsonFormat(shape = JsonFormat.Shape.STRING,
                                                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
                                            ) val createdAt: LocalDateTime,
                                            val transactionType: String,
                                            val amount: BigDecimal,
                                            val doneBy: String
                                            ) {

    constructor(accountTransaction: AccountTransaction) : this(accountTransaction.createdAt!!,
        accountTransaction.transactionType!!.name,
        accountTransaction.amount!!,
        accountTransaction.createdBy!!.name!!)
}