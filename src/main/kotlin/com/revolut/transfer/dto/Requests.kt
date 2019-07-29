package com.revolut.transfer.dto

import java.math.BigDecimal
import javax.validation.constraints.NotBlank

data class CreateAccountRequest(@NotBlank val userId: String)

data class TransferRequest(@NotBlank val userId: String,
                           @NotBlank val destinationAccountId: Int,
                           @NotBlank val amount: BigDecimal)

data class BalanceChangeRequest(@NotBlank val userId: String,
                                @NotBlank val amount: BigDecimal)