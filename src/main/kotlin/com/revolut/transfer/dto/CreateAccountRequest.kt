package com.revolut.transfer.dto

import javax.validation.constraints.NotBlank

data class CreateAccountRequest(@NotBlank val userId: String)
