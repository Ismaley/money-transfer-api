package com.revolut.transfer.dto

import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class CreateAccountRequest(@NotBlank val userId: String)

data class TransferRequest(@field:NotBlank val userId: String,
                           @field:NotNull val destinationAccountId: Int,
                           @field:NotNull val amount: BigDecimal)

data class BalanceChangeRequest(@field:NotBlank val userId: String,
                                @field:NotNull val amount: BigDecimal)

data class CreateUserRequest(@field:NotBlank val name: String?,
                             @field:NotBlank val documentNumber: String?,
                             @field:NotNull val birthDate: LocalDate?)