package com.revolut.transfer.model

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
data class Account(@Id @GeneratedValue(strategy = GenerationType.AUTO) val number: Int? = null,
                   @Column val balance: BigDecimal? = BigDecimal(0),
                   @ManyToOne(fetch = FetchType.EAGER) val user: User? = null,
                   @OneToMany(fetch = FetchType.LAZY) val transactions: List<AccountTransaction>? = emptyList(),
                   @Column val createdAt: LocalDateTime? = null) {
}