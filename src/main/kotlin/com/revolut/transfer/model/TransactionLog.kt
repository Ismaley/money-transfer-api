package com.revolut.transfer.model

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

data class TransactionLog(@Id @GeneratedValue(strategy = GenerationType.SEQUENCE) val transactionId: Int,
                          @Column val transactionTime: LocalDateTime,
                          @Column @Enumerated val transactionType: TransactionType,
                          @Column val amount: BigDecimal,
                          @Column @ManyToOne val account: Account) {

}
