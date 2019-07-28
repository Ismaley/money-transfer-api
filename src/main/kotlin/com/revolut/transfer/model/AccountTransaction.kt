package com.revolut.transfer.model

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class AccountTransaction(@Id @GeneratedValue(strategy = GenerationType.SEQUENCE) val transactionId: Int? = null,
                              @Column val transactionTime: LocalDateTime? = null,
                              @Column @Enumerated(value = EnumType.STRING) val transactionType: TransactionType? = null,
                              @Column val amount: BigDecimal? = null,
                              @ManyToOne(fetch = FetchType.EAGER) val account: Account? = null) {

}
