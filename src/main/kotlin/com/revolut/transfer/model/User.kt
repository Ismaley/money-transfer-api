package com.revolut.transfer.model

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column

class User(@Column val userName: String,
           @Column val documentNumber: String,
           @Column val birthDate: LocalDate,
           @Column val createdAt: LocalDateTime) {

}
