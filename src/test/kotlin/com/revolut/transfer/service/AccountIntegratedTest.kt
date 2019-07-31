package com.revolut.transfer.service

import com.revolut.transfer.model.Account
import com.revolut.transfer.model.User
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month

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