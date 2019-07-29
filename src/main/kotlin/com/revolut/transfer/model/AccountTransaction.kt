package com.revolut.transfer.model

import org.jetbrains.annotations.NotNull
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
import javax.persistence.OneToOne

@Entity
data class AccountTransaction(@Id @GeneratedValue(strategy = GenerationType.SEQUENCE) val transactionId: Int? = null,
                              @Column @NotNull val createdAt: LocalDateTime? = LocalDateTime.now(),
                              @OneToOne(fetch = FetchType.LAZY) @NotNull val createdBy: User? = null,
                              @Column @NotNull @Enumerated(value = EnumType.STRING) val transactionType: TransactionType? = null,
                              @Column @NotNull val amount: BigDecimal? = null,
                              @ManyToOne(fetch = FetchType.EAGER) @NotNull val account: Account? = null) {

}
