package com.revolut.transfer.service

import com.revolut.transfer.exception.AccountServiceException
import com.revolut.transfer.exception.NotFoundException
import com.revolut.transfer.model.Account
import com.revolut.transfer.model.AccountTransaction
import com.revolut.transfer.model.TransactionType
import com.revolut.transfer.model.User
import com.revolut.transfer.repository.AccountRepository
import com.revolut.transfer.repository.AccountTransactionRepository
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month

class AccountServiceTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var accountTransactionRepository: AccountTransactionRepository
    private lateinit var accountService: AccountService
    private lateinit var userService: UserService
    private lateinit var user: User
    private lateinit var account: Account

    private val userId = "id"
    private val userName = "ismaley"
    private val docNumber = "123131516-10"


    @BeforeEach
    fun setUp() {
        accountRepository = mockkClass(AccountRepository::class)
        userService = mockkClass(UserService::class)
        accountTransactionRepository = mockkClass(AccountTransactionRepository::class)
        accountService = AccountService(accountRepository, userService, accountTransactionRepository)
        user = User(userId, userName, docNumber, LocalDate.of(1986, Month.JULY, 28))
        account = Account(user = user)
    }

    @Test
    fun `should create a new account`() {
        every { userService.getUser(userId) } returns user
        every { accountRepository.save(any()) } returns account

        val createdAccount = accountService.createAccount(userId)
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 1) { accountRepository.save(any()) }
        Assertions.assertEquals(account, createdAccount)
        Assertions.assertEquals(BigDecimal(0), createdAccount.balance)
        Assertions.assertEquals(user, createdAccount.user)
        Assertions.assertEquals(0, createdAccount.transactions!!.size)
    }

    @Test
    fun `should not create a new account for a non existent user`() {
        every { userService.getUser(userId) } throws NotFoundException("user not found")

        Assertions.assertThrows(AccountServiceException::class.java, {
            accountService.createAccount(userId)
        }, "user with id: $userId does not exist")

        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 0) { accountRepository.save(account) }
    }

    @Test
    fun `should transfer money between accounts`() {
        val sourceAccountId = 1
        val destinationAccountId = 2

        val sourceAccount = Account(number = sourceAccountId, balance = BigDecimal(20), user = user)
        val destinationAccount = Account(number = destinationAccountId, balance = BigDecimal(20), user = user)

        every { accountRepository.getAccountForUpdate(sourceAccountId) } returns sourceAccount
        every { accountRepository.getAccountForUpdate(destinationAccountId) } returns destinationAccount
        every { accountRepository.update(sourceAccount) } returns sourceAccount
        every { accountRepository.update(destinationAccount) } returns destinationAccount
        every { accountTransactionRepository.save(any()) } returns AccountTransaction()
        every { userService.getUser(userId) } returns user

        val result = accountService.transferMoneyBetweenAccounts(userId, sourceAccountId, destinationAccountId, BigDecimal(20.00))

        verify (exactly = 1) { accountRepository.getAccountForUpdate(sourceAccountId) }
        verify (exactly = 1) { accountRepository.getAccountForUpdate(destinationAccountId) }
        verify (exactly = 1) { accountRepository.update(sourceAccount) }
        verify (exactly = 1) { accountRepository.update(destinationAccount) }
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 2) { accountTransactionRepository.save(any()) }

        Assertions.assertEquals(BigDecimal(20), result.amount)
        Assertions.assertEquals(sourceAccountId, result.sourceAccountNumber)
        Assertions.assertNotNull(result.createdAt)
        Assertions.assertEquals(destinationAccountId, result.destinationAccountNumber)
        Assertions.assertEquals(BigDecimal(0), sourceAccount.balance)
        Assertions.assertEquals(BigDecimal(40), destinationAccount.balance)
    }

    @Test
    fun `should deposit money on an account`() {
        val accountId = 1
        val accountToDeposit = Account(number = accountId, balance = BigDecimal(0), user = user)

        every { accountRepository.getAccountForUpdate(accountId) } returns accountToDeposit
        every { accountRepository.update(accountToDeposit) } returns accountToDeposit
        every { accountTransactionRepository.save(any()) } returns AccountTransaction()
        every { userService.getUser(userId) } returns user

        val account = accountService.deposit(userId, accountId, BigDecimal(20))

        verify (exactly = 1) { accountRepository.getAccountForUpdate(accountId) }
        verify (exactly = 1) { accountRepository.update(accountToDeposit) }
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 1) { accountTransactionRepository.save(any()) }

        Assertions.assertEquals(BigDecimal(20), account.balance)
        Assertions.assertEquals(account, accountToDeposit)
    }

    @Test
    fun `should not deposit money if an account does not exist`() {
        val accountId = 1

        every { accountRepository.getAccountForUpdate(accountId) } returns null

        Assertions.assertThrows(NotFoundException::class.java, {
            accountService.deposit(userId, accountId, BigDecimal(20))
        }, "Account with id: $accountId does not exist")

        verify (exactly = 1) { accountRepository.getAccountForUpdate(accountId) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { userService.getUser(any()) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should not deposit money if amount is 0`() {
        Assertions.assertThrows(AccountServiceException::class.java, {
            accountService.deposit(userId, 1, BigDecimal(0))
        }, "amount must be greater than 0")

        verify (exactly = 0) { accountRepository.getAccountForUpdate(any()) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { userService.getUser(any()) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should not deposit money if amount is below 0`() {
        Assertions.assertThrows(AccountServiceException::class.java, {
            accountService.deposit(userId, 1, BigDecimal(-20))
        }, "amount must be greater than 0")

        verify (exactly = 0) { accountRepository.getAccountForUpdate(any()) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { userService.getUser(any()) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should withdraw money from an account`() {
        val accountId = 1
        val accountToWithdraw = Account(number = accountId, balance = BigDecimal(40), user = user)

        every { accountRepository.getAccountForUpdate(accountId) } returns accountToWithdraw
        every { accountRepository.update(accountToWithdraw) } returns accountToWithdraw
        every { accountTransactionRepository.save(any()) } returns AccountTransaction()
        every { userService.getUser(userId) } returns user

        val account = accountService.withdraw(userId, accountId, BigDecimal(20))

        verify (exactly = 1) { accountRepository.getAccountForUpdate(accountId) }
        verify (exactly = 1) { accountRepository.update(accountToWithdraw) }
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 1) { accountTransactionRepository.save(any()) }

        Assertions.assertEquals(BigDecimal(20), account.balance)
        Assertions.assertEquals(account, accountToWithdraw)
    }

    @Test
    fun `should not withdraw money from an account if it does not have sufficient funds`() {
        val accountId = 1
        val accountToWithdraw = Account(number = accountId, balance = BigDecimal(0), user = user)

        every { accountRepository.getAccountForUpdate(accountId) } returns accountToWithdraw
        every { accountRepository.update(accountToWithdraw) } returns accountToWithdraw
        every { accountTransactionRepository.save(any()) } returns AccountTransaction()
        every { userService.getUser(userId) } returns user

        Assertions.assertThrows(AccountServiceException::class.java, {
            accountService.withdraw(userId, accountId, BigDecimal(20))
        }, "Insufficient funds to withdraw")

        verify (exactly = 1) { accountRepository.getAccountForUpdate(accountId) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should not withdraw money if amount is below 0`() {
        Assertions.assertThrows(AccountServiceException::class.java, {
            accountService.withdraw(userId, 1, BigDecimal(-20))
        }, "amount must be greater than 0")

        verify (exactly = 0) { accountRepository.getAccountForUpdate(any()) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { userService.getUser(any()) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should not withdraw money from an account if user does not own the account`() {
        val accountId = 1
        val accountToWithdraw = Account(number = accountId, balance = BigDecimal(0), user = User(id = "AnotherUserId"))

        every { accountRepository.getAccountForUpdate(accountId) } returns accountToWithdraw
        every { accountRepository.update(accountToWithdraw) } returns accountToWithdraw
        every { accountTransactionRepository.save(any()) } returns AccountTransaction()
        every { userService.getUser(userId) } returns user

        Assertions.assertThrows(AccountServiceException::class.java, {
            accountService.withdraw(userId, accountId, BigDecimal(20))
        }, "You do not own this account to withdraw money")

        verify (exactly = 1) { accountRepository.getAccountForUpdate(accountId) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should not transfer money between accounts if amount is below 0`() {
        val sourceAccountId = 1
        val destinationAccountId = 2

        Assertions.assertThrows(AccountServiceException::class.java, {
            accountService.transferMoneyBetweenAccounts(userId, sourceAccountId, destinationAccountId, BigDecimal(-20.00))
        }, "amount must be greater than 0")

        verify (exactly = 0) { accountRepository.getAccountForUpdate(any()) }
        verify (exactly = 0) { accountRepository.getAccountForUpdate(any()) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { userService.getUser(any()) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should not transfer money between accounts if user does not own source account`() {
        val sourceAccountId = 1
        val destinationAccountId = 2

        val sourceAccount = Account(number = sourceAccountId, balance = BigDecimal(20), user = User("anotherUserId"))
        val destinationAccount = Account(number = destinationAccountId, balance = BigDecimal(20), user = user)

        every { accountRepository.getAccountForUpdate(sourceAccountId) } returns sourceAccount
        every { accountRepository.getAccountForUpdate(destinationAccountId) } returns destinationAccount
        every { userService.getUser(userId) } returns user

        Assertions.assertThrows(AccountServiceException::class.java, {
            accountService.transferMoneyBetweenAccounts(userId, sourceAccountId, destinationAccountId, BigDecimal(20.00))
        }, "You do not own this account to retrieve it's information")

        verify (exactly = 1) { accountRepository.getAccountForUpdate(sourceAccountId) }
        verify (exactly = 1) { accountRepository.getAccountForUpdate(destinationAccountId) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should not transfer money between accounts if source account does not have sufficient funds`() {
        val sourceAccountId = 1
        val destinationAccountId = 2

        val sourceAccount = Account(number = sourceAccountId, balance = BigDecimal(0), user = user)
        val destinationAccount = Account(number = destinationAccountId, balance = BigDecimal(20), user = user)

        every { accountRepository.getAccountForUpdate(sourceAccountId) } returns sourceAccount
        every { accountRepository.getAccountForUpdate(destinationAccountId) } returns destinationAccount
        every { userService.getUser(userId) } returns user

        Assertions.assertThrows(AccountServiceException::class.java, {
            accountService.transferMoneyBetweenAccounts(userId, sourceAccountId, destinationAccountId, BigDecimal(20.00))
        }, "Insufficient funds to transfer")

        verify (exactly = 1) { accountRepository.getAccountForUpdate(sourceAccountId) }
        verify (exactly = 1) { accountRepository.getAccountForUpdate(destinationAccountId) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should not transfer money between accounts if source account does not exist`() {
        val sourceAccountId = 1
        val destinationAccountId = 2

        every { accountRepository.getAccountForUpdate(sourceAccountId) } returns null

        Assertions.assertThrows(NotFoundException::class.java, {
            accountService.transferMoneyBetweenAccounts(userId, sourceAccountId, destinationAccountId, BigDecimal(20.00))
        }, "Account with id: $sourceAccountId does not exist")

        verify (exactly = 1) { accountRepository.getAccountForUpdate(sourceAccountId) }
        verify (exactly = 0) { accountRepository.getAccountForUpdate(destinationAccountId) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { userService.getUser(userId) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should not transfer money between accounts if destination account does not exist`() {
        val sourceAccountId = 1
        val destinationAccountId = 2

        val sourceAccount = Account(number = sourceAccountId, balance = BigDecimal(20), user = user)

        every { accountRepository.getAccountForUpdate(sourceAccountId) } returns sourceAccount
        every { accountRepository.getAccountForUpdate(destinationAccountId) } returns null

        Assertions.assertThrows(NotFoundException::class.java, {
            accountService.transferMoneyBetweenAccounts(userId, sourceAccountId, destinationAccountId, BigDecimal(20.00))
        }, "Account with id: $sourceAccountId does not exist")

        verify (exactly = 1) { accountRepository.getAccountForUpdate(sourceAccountId) }
        verify (exactly = 1) { accountRepository.getAccountForUpdate(destinationAccountId) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { userService.getUser(userId) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should not transfer money between accounts if user does not exist`() {
        val sourceAccountId = 1
        val destinationAccountId = 2

        val sourceAccount = Account(number = sourceAccountId, balance = BigDecimal(0), user = user)
        val destinationAccount = Account(number = destinationAccountId, balance = BigDecimal(20), user = user)

        every { accountRepository.getAccountForUpdate(sourceAccountId) } returns sourceAccount
        every { accountRepository.getAccountForUpdate(destinationAccountId) } returns destinationAccount
        every { userService.getUser(userId) } throws NotFoundException("user not found")

        Assertions.assertThrows(AccountServiceException::class.java, {
            accountService.transferMoneyBetweenAccounts(userId, sourceAccountId, destinationAccountId, BigDecimal(20.00))
        }, "Account with id: $sourceAccountId does not exist")

        verify (exactly = 1) { accountRepository.getAccountForUpdate(sourceAccountId) }
        verify (exactly = 1) { accountRepository.getAccountForUpdate(destinationAccountId) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 0) { accountRepository.update(any()) }
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 0) { accountTransactionRepository.save(any()) }
    }

    @Test
    fun `should retrieve account`() {
        val accountId = 1
        val account = Account(number = accountId, balance = BigDecimal(0), user = user)

        every { accountRepository.getAccount(accountId) } returns account
        every { userService.getUser(userId) } returns user

        val foundAccount = accountService.getAccount(userId, accountId)
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 1) { accountRepository.getAccount(accountId) }
        Assertions.assertEquals(account, foundAccount)
        Assertions.assertEquals(BigDecimal(0), foundAccount.balance)
        Assertions.assertEquals(user, foundAccount.user)
        Assertions.assertEquals(0, foundAccount.transactions!!.size)
    }

    @Test
    fun `should not retrieve account if it does not exist`() {
        val accountId = 1

        every { accountRepository.getAccount(accountId) } returns null

        Assertions.assertThrows(NotFoundException::class.java, {
            accountService.getAccount(userId, accountId)
        }, "Account with id: $accountId does not exist")

        verify (exactly = 0) { userService.getUser(any()) }
        verify (exactly = 1) { accountRepository.getAccount(accountId) }

    }

    @Test
    fun `should not retrieve account if user does not own it`() {
        val accountId = 1
        val account = Account(number = accountId, balance = BigDecimal(0), user = User(id = "AnotherUser"))

        every { accountRepository.getAccount(accountId) } returns account
        every { userService.getUser(userId) } returns user

        Assertions.assertThrows(AccountServiceException::class.java, {
            accountService.getAccount(userId, accountId)
        }, "You do not own this account to retrieve it's information")

        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 1) { accountRepository.getAccount(accountId) }
    }

    @Test
    fun `should get account transactions`() {
        val accountId = 1
        val account = Account(number = accountId, balance = BigDecimal(20), user = user,
            transactions = listOf(AccountTransaction(transactionType = TransactionType.DEPOSIT, amount = BigDecimal(20))))

        every { accountRepository.getAccount(accountId) } returns account
        every { userService.getUser(userId) } returns user

        val transactions = accountService.getAccountTransactions(userId, accountId)
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 1) { accountRepository.getAccount(accountId) }
        Assertions.assertEquals(1, transactions.size)
        Assertions.assertEquals(TransactionType.DEPOSIT, transactions[0].transactionType)
        Assertions.assertEquals(BigDecimal(20), transactions[0].amount)
    }
}