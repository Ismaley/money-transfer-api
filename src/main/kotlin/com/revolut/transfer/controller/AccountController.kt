package com.revolut.transfer.controller

import com.revolut.transfer.dto.CreateAccountRequest
import com.revolut.transfer.model.Account
import com.revolut.transfer.service.AccountService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.validation.Validated
import javax.validation.constraints.NotBlank

@Validated
@Controller("/accounts")
class AccountController(val accountService: AccountService) {

    @Post
    fun createAccount(@Body createAccountRequest: CreateAccountRequest) : Account =
        accountService.createAccount(createAccountRequest.userId)

}