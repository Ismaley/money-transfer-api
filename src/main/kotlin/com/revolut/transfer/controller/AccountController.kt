package com.revolut.transfer.controller

import com.revolut.transfer.dto.AccountRepresentation
import com.revolut.transfer.dto.AccountTransactionRepresentation
import com.revolut.transfer.dto.BalanceChangeRequest
import com.revolut.transfer.dto.CreateAccountRequest
import com.revolut.transfer.dto.TransferRequest
import com.revolut.transfer.service.AccountService
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.annotation.Status
import io.micronaut.validation.Validated

@Validated
@Controller("/accounts")
class AccountController(private val accountService: AccountService) {

    @Post
    @Status(HttpStatus.CREATED)
    fun createAccount(@Body createAccountRequest: CreateAccountRequest): AccountRepresentation =
        AccountRepresentation(accountService.createAccount(createAccountRequest.userId))

    @Get("/{accountId}")
    fun getAccount(@PathVariable accountId: Int, @QueryValue userId: String): AccountRepresentation =
        AccountRepresentation(accountService.getAccount(userId, accountId))

    @Post("/{accountId}/transfers")
    fun transferMoneyBetweenAccounts(@PathVariable accountId: Int, @Body transferRequest: TransferRequest) =
        accountService.transferMoneyBetweenAccounts(
            transferRequest.userId,
            accountId,
            transferRequest.destinationAccountId,
            transferRequest.amount
        )

    @Post("/{accountId}/deposits")
    fun deposit(@PathVariable accountId: Int, @Body transferRequest: BalanceChangeRequest): AccountRepresentation =
        AccountRepresentation(accountService.deposit(transferRequest.userId, accountId, transferRequest.amount))

    @Post("/{accountId}/withdrawals")
    fun withdraw(@PathVariable accountId: Int, @Body transferRequest: BalanceChangeRequest): AccountRepresentation =
        AccountRepresentation(accountService.withdraw(transferRequest.userId, accountId, transferRequest.amount))

    @Get("/{accountId}/transactions")
    fun getTransactions(@PathVariable accountId: Int, @QueryValue userId: String): List<AccountTransactionRepresentation> =
        accountService.getAccountTransactions(userId, accountId).map { AccountTransactionRepresentation(it) }.toList()
}