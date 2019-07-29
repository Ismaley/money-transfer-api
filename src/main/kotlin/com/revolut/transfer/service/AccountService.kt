package com.revolut.transfer.service

import com.revolut.transfer.exception.AccountServiceException
import com.revolut.transfer.model.Account
import com.revolut.transfer.model.AccountTransaction
import com.revolut.transfer.model.MoneyTransferResult
import com.revolut.transfer.model.TransactionType
import com.revolut.transfer.model.User
import com.revolut.transfer.repository.AccountRepository
import com.revolut.transfer.repository.AccountTransactionRepository
import javassist.NotFoundException
import java.lang.RuntimeException
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Singleton

@Singleton
class AccountService(private val accountRepository: AccountRepository,
                     private val userService: UserService,
                     private val accountTransactionRepository: AccountTransactionRepository
) {

    fun createAccount(userId: String): Account =
        accountRepository.save(Account(user = findUser(userId)))


    fun transferMoneyBetweenAccounts(userId: String, sourceAccountId: Int, destinationAccountId: Int, amount: BigDecimal): MoneyTransferResult {
        val sourceAccount = getAccountForUpdate(sourceAccountId)
        val destinationAccount = getAccountForUpdate(destinationAccountId)
        if(isOwner(userId, sourceAccount)) {
            if(sourceAccount.hasEnoughBalance(amount)) {
                transferMoney(sourceAccount, destinationAccount, amount)
                update(sourceAccount)
                update(destinationAccount)
                saveAccountTransaction(sourceAccount.user!!, amount, TransactionType.DEPOSIT, destinationAccount)
                saveAccountTransaction(sourceAccount.user, amount, TransactionType.WITHDRAW, sourceAccount)
                return MoneyTransferResult(sourceAccountId, destinationAccountId, sourceAccount!!.user!!.name!!, amount)
            } else {
                throw AccountServiceException("Insufficient funds to transfer")
            }
        } else {
            throw AccountServiceException("You do not own this account to retrieve it's information")
        }
    }

    fun getAccount(accountId: Int, userId: String): Account {
        val account = accountRepository.getAccount(accountId) ?: throw NotFoundException("Account with id: $accountId does not exist")
        if(isOwner(userId, account)) {
            return account
        } else {
            throw AccountServiceException("You do not own this account to retrieve it's information")
        }
    }

    fun deposit(userId: String, accountId: Int, amount: BigDecimal): Account {
        val account = getAccountForUpdate(accountId)
        account.balance = account.balance!!.add(amount)
        update(account)
        saveAccountTransaction(findUser(userId), amount, TransactionType.DEPOSIT, account)
        return account
    }

    fun withdraw(userId: String, accountId: Int, amount: BigDecimal): Account {
        val account = getAccountForUpdate(accountId)
        if(isOwner(userId, account)) {
            if(account.hasEnoughBalance(amount)) {
                account.balance = account.balance!!.subtract(amount)
                update(account)
                saveAccountTransaction(account.user!!, amount, TransactionType.WITHDRAW, account)
                return account
            } else {
                throw AccountServiceException("Insufficient funds to withdraw")
            }
        } else {
            throw AccountServiceException("You do not own this account to withdraw money")
        }
    }

    fun getAccountTransactions(accountId: Int, userId: String): List<AccountTransaction> =
        getAccount(accountId, userId).transactions ?: emptyList()


    private fun isOwner(userId: String, account: Account): Boolean {
        val user = findUser(userId)
        return account.user!!.id == user.id
    }

    private fun transferMoney(sourceAccount: Account, destinationAccount: Account, amount: BigDecimal) {
        sourceAccount.balance = sourceAccount.balance!!.subtract(amount)
        destinationAccount.balance = destinationAccount.balance!!.add(amount)
    }

    private fun update(account: Account) : Account = accountRepository.update(account)

    private fun getAccountForUpdate(accountId: Int): Account =
        accountRepository.getAccountForUpdate(accountId) ?: throw NotFoundException("Account with id: $accountId does not exists")

    private fun saveAccountTransaction(user: User, amount: BigDecimal, transactionType: TransactionType, account: Account): AccountTransaction =
        accountTransactionRepository.save(AccountTransaction(createdBy = user, amount = amount, transactionType = transactionType, account = account))

    private fun findUser(userId: String): User {
        try {
            return userService.getUser(userId)
        } catch (e: Exception) {
            if (e is NotFoundException) {
                throw AccountServiceException("user with id: $userId does not exist")
            } else {
                throw RuntimeException(e)
            }
        }
    }
}

