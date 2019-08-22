package com.revolut.transfer.service

import com.revolut.transfer.exception.AccountServiceException
import com.revolut.transfer.exception.NotFoundException
import com.revolut.transfer.model.Account
import com.revolut.transfer.model.AccountTransaction
import com.revolut.transfer.model.MoneyTransferResult
import com.revolut.transfer.model.TransactionType
import com.revolut.transfer.model.User
import com.revolut.transfer.repository.AccountRepository
import com.revolut.transfer.repository.AccountTransactionRepository
import io.micronaut.spring.tx.annotation.Transactional
import mu.KotlinLogging
import java.math.BigDecimal
import javax.inject.Singleton

@Singleton
open class AccountService(
    private val accountRepository: AccountRepository,
    private val userService: UserService,
    private val accountTransactionRepository: AccountTransactionRepository
) {
    private val log = KotlinLogging.logger { }

    fun createAccount(userId: String): Account =
        accountRepository.save(Account(user = findUser(userId)))

    @Transactional
    open fun transferMoneyBetweenAccounts(
        userId: String,
        sourceAccountId: Int,
        destinationAccountId: Int,
        amount: BigDecimal
    ): MoneyTransferResult {
        validateAmount(amount)
        log.info { "starting transferring $amount from account: $sourceAccountId to account: $destinationAccountId" }
        log.info { "trying to lock source account: $sourceAccountId" }
        val sourceAccount = getAccountForUpdate(sourceAccountId)
//        Thread.sleep(500)
        log.info { "trying to lock destination account: $destinationAccountId" }
        val destinationAccount = getAccountForUpdate(destinationAccountId)
        validateAccountOwnership(userId, sourceAccount)
        if (sourceAccount.hasEnoughBalance(amount)) {
            transferMoney(sourceAccount, destinationAccount, amount)
            update(sourceAccount)
            update(destinationAccount)
            saveAccountTransaction(sourceAccount.user!!, amount, TransactionType.DEPOSIT, destinationAccount)
            saveAccountTransaction(sourceAccount.user, amount, TransactionType.WITHDRAW, sourceAccount)
            log.info { "finishing transferring $amount from account: $sourceAccountId to account: $destinationAccountId with success" }
            return MoneyTransferResult(sourceAccountId, destinationAccountId, amount)
        } else {
            log.info { "finishing transferring $amount from account: $sourceAccountId to account: $destinationAccountId with error" }
            throw AccountServiceException("Insufficient funds to transfer")
        }

    }

    fun getAccount(userId: String, accountId: Int): Account {
        val account = accountRepository.getAccount(accountId)
            ?: throw NotFoundException("Account with id: $accountId does not exist")
        validateAccountOwnership(userId, account)
        return account
    }

    @Transactional
    open fun deposit(userId: String, accountId: Int, amount: BigDecimal): Account {
        validateAmount(amount)
        val account = getAccountForUpdate(accountId)
        log.info { "depositing $amount on account $accountId" }
        account.balance = account.balance!!.add(amount)
        update(account)
        saveAccountTransaction(findUser(userId), amount, TransactionType.DEPOSIT, account)
        log.info { "finishing depositing $amount on account $accountId" }
        return account
    }

    @Transactional
    open fun withdraw(userId: String, accountId: Int, amount: BigDecimal): Account {
        validateAmount(amount)
        val account = getAccountForUpdate(accountId)
        log.info { "withdrawing $amount from account $accountId" }
        validateAccountOwnership(userId, account)
        if (account.hasEnoughBalance(amount)) {
            account.balance = account.balance!!.subtract(amount)
            update(account)
            saveAccountTransaction(account.user!!, amount, TransactionType.WITHDRAW, account)
            log.info { "finishing withdrawing $amount from account $accountId" }
            return account
        } else {
            log.info { "finishing withdrawing $amount from account $accountId with error" }
            throw AccountServiceException("Insufficient funds to withdraw")
        }
    }

    fun getAccountTransactions(userId: String, accountId: Int): List<AccountTransaction> =
        getAccount(userId, accountId).transactions ?: emptyList()

    private fun validateAmount(amount: BigDecimal) {
        if (amount <= BigDecimal(0)) {
            throw AccountServiceException("amount must be greater than 0")
        }
    }

    private fun validateAccountOwnership(userId: String, account: Account) {
        if (!isOwner(userId, account)) {
            throw AccountServiceException("You do not own this account")
        }
    }

    private fun isOwner(userId: String, account: Account): Boolean {
        val user = findUser(userId)
        return account.user!!.id == user.id
    }

    private fun transferMoney(sourceAccount: Account, destinationAccount: Account, amount: BigDecimal) {
        sourceAccount.balance = sourceAccount.balance!!.subtract(amount)
        destinationAccount.balance = destinationAccount.balance!!.add(amount)
    }

    private fun update(account: Account): Account = accountRepository.update(account)

    private fun getAccountForUpdate(accountId: Int): Account =
        accountRepository.getAccountForUpdate(accountId)
            ?: throw NotFoundException("Account with id: $accountId does not exists")

    private fun saveAccountTransaction(
        user: User,
        amount: BigDecimal,
        transactionType: TransactionType,
        account: Account
    ): AccountTransaction =
        accountTransactionRepository.save(
            AccountTransaction(
                createdBy = user,
                amount = amount,
                transactionType = transactionType,
                account = account
            )
        )

    private fun findUser(userId: String): User {
        return try {
            userService.getUser(userId)
        } catch (e: Exception) {
            if (e is NotFoundException) {
                throw AccountServiceException("user with id: $userId does not exist")
            } else {
                throw RuntimeException(e)
            }
        }
    }
}

