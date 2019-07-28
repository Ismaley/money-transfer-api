package com.revolut.transfer.model

import org.hibernate.annotations.GenericGenerator
import org.jetbrains.annotations.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
data class User(@Id @GeneratedValue(generator = "uuid")
           @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator") val id: String? = "",
                @Column @NotNull val name: String? = "",
                @Column @NotNull val documentNumber: String? = "",
                @Column @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) val birthDate: LocalDate? = null,
                @Column @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) val createdAt: LocalDateTime? = LocalDateTime.now(),
                @OneToMany(fetch = FetchType.EAGER) val accounts: List<Account>? = emptyList()) {

}
