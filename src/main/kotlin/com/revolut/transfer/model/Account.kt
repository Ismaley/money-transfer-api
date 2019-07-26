package com.revolut.transfer.model

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class Account(@Id @GeneratedValue(strategy = GenerationType.AUTO) val number: Int,
                   @Column val balance: BigDecimal,
                   @Column @ManyToOne val user: User,
                   @Column @ManyToOne val transactionLog: TransactionLog,
                   @Column val createdAt: LocalDateTime) {
}