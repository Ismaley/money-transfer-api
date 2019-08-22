package com.revolut.transfer.service

import com.revolut.transfer.model.Account
import com.revolut.transfer.model.User
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import jdk.nashorn.internal.ir.annotations.Ignore
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountIntegratedTest {

    private var server: EmbeddedServer? = null
    private lateinit var user: User
    private lateinit var account: Account
    private lateinit var accountService: AccountService
    private lateinit var userService: UserService

    @BeforeAll
    fun setupServer() {
        server = ApplicationContext
            .build()
            .packages("com.revolut.transfer")
            .run(EmbeddedServer::class.java)

        accountService = server!!.applicationContext.createBean(AccountService::class.java)
        userService = server!!.applicationContext.createBean(UserService::class.java)
        user = createUser()
        account = createAccount()
    }

    @AfterAll
    fun stopServer() {
        server?.stop()
    }

    @Test
    fun shouldTestOperationsConcurrency() {
        val initialBalance = 3000.toBigDecimal()
        accountService.deposit(user.id!!, account.number!!, initialBalance)
        val deposits = (0..50).map { generateAccountDeposits(50.toBigDecimal()) }
        val withdrawals = (0..50).map { generateAccountWithdrawals(50.toBigDecimal()) }

        val operations = deposits + withdrawals
        operations.parallelStream().forEach { it.start() }
        operations.map { it.join() }

        val account = accountService.getAccount(user.id!!, account.number!!)
        Assertions.assertTrue(initialBalance.compareTo(account.balance) == 0)
    }

    @Test
    fun shouldTestDeadLockPossibility() {
        val accountA = createAccount()
        val accountB = createAccount()
        val accountC = createAccount()

        accountService.deposit(user.id!!, accountA.number!!, 30.toBigDecimal())
        accountService.deposit(user.id!!, accountB.number!!, 30.toBigDecimal())
        accountService.deposit(user.id!!, accountC.number!!, 30.toBigDecimal())

        println("balances of accounts A: ${accountA.balance}, B: ${accountB.balance}, C: ${accountC.balance}")

        val transferFromAtoB = generateAccountTransfer(accountA.number!!, accountB.number!!, 10.toBigDecimal())
        val transferFromBtoC = generateAccountTransfer(accountB.number!!, accountC.number!!, 10.toBigDecimal())
        val transferFromCtoA = generateAccountTransfer(accountC.number!!, accountA.number!!, 10.toBigDecimal())

        val transactions = listOf(transferFromAtoB, transferFromBtoC, transferFromCtoA)

        transactions.parallelStream().forEach { it.start() }
        transactions.map { it.join() }

        println("balances of accounts A: ${accountA.balance}, B: ${accountB.balance}, C: ${accountC.balance}")

    }

    private fun generateAccountTransfer(sourceAccountNumber: Int, destinationAccountNumber: Int, amount: BigDecimal): Thread =
        Thread {
            accountService.transferMoneyBetweenAccounts(user.id!!, sourceAccountNumber, destinationAccountNumber, amount)
        }

    private fun generateAccountDeposits(amount: BigDecimal): Thread =
        Thread {
            accountService.deposit(user.id!!, account.number!!, amount)
        }

    private fun generateAccountWithdrawals(amount: BigDecimal): Thread =
        Thread {
            accountService.withdraw(user.id!!, account.number!!, amount)
        }

    private fun createUser(): User =
        userService.createUser(User(name = "ismaley", documentNumber = "1102301023", birthDate = LocalDate.of(1986, Month.JULY, 28)))

    private fun createAccount(): Account =
        accountService.createAccount(user.id!!)


}