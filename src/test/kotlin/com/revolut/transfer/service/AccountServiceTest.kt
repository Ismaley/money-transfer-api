package com.revolut.transfer.service

import com.revolut.transfer.exception.AccountServiceException
import com.revolut.transfer.model.Account
import com.revolut.transfer.model.User
import com.revolut.transfer.repository.AccountRepository
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import javassist.NotFoundException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month

class AccountServiceTest {

    private lateinit var accountRepository: AccountRepository
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
        accountService = AccountService(accountRepository, userService)
        user = User(userId, userName, docNumber, LocalDate.of(1986, Month.JULY, 28))
        account = Account(user = user)
    }

    @Test
    fun `should create a new account`() {
        every { userService.getUser(userId) } returns user
        every { accountRepository.save(account) } returns account

        val createdAccount = accountService.createAccount(userId)
        verify (exactly = 1) { userService.getUser(userId) }
        verify (exactly = 1) { accountRepository.save(account) }
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
}