package com.revolut.transfer.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import org.jetbrains.annotations.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.validation.constraints.NotBlank

data class CreateAccountRequest(@NotBlank val userId: String)

data class TransferRequest(@NotBlank val userId: String,
                           @NotBlank val destinationAccountId: Int,
                           @NotBlank val amount: BigDecimal)

data class BalanceChangeRequest(@NotBlank val userId: String,
                                @NotBlank val amount: BigDecimal)

data class CreateUserRequest(@NotBlank val name: String,
                             @NotBlank val documentNumber: String,
                             @NotBlank val birthDate: LocalDate)