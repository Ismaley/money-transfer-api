package com.revolut.transfer.service

import com.revolut.transfer.exception.NotFoundException
import com.revolut.transfer.model.User
import com.revolut.transfer.repository.UserRepository
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService
    private lateinit var user: User

    private val userId = "id"
    private val userName = "ismaley"
    private val docNumber = "123131516-10"


    @BeforeEach
    fun setUp() {
        userRepository = mockkClass(UserRepository::class)
        userService = UserService(userRepository)
        user = User(userId, userName, docNumber, LocalDate.of(1986, Month.JULY, 28))
    }

    @Test
    fun `should create a new user`() {
        every { userRepository.save(user) } returns user

        val createdUser = userService.createUser(user)

        verify (exactly = 1) { userRepository.save(user) }

        Assertions.assertEquals(user, createdUser)
        Assertions.assertEquals(LocalDate.of(1986, Month.JULY, 28), createdUser.birthDate)
        Assertions.assertEquals(docNumber, createdUser.documentNumber)
        Assertions.assertEquals(userName, createdUser.name)
        Assertions.assertEquals(userId, createdUser.id)
    }

    @Test
    fun `should find user by id`() {
        every { userRepository.findOne(userId) } returns user

        val foundUser = userService.getUser(userId)

        verify (exactly = 1) { userRepository.findOne(userId) }

        Assertions.assertEquals(user, foundUser)
        Assertions.assertEquals(LocalDate.of(1986, Month.JULY, 28), foundUser.birthDate)
        Assertions.assertEquals(docNumber, foundUser.documentNumber)
        Assertions.assertEquals(userName, foundUser.name)
        Assertions.assertEquals(userId, foundUser.id)
    }

    @Test
    fun `should throw exception if user is not found`() {
        every { userRepository.findOne(userId) } returns null

        Assertions.assertThrows(NotFoundException::class.java, {
            userService.getUser(userId)
        }, "user with id: $userId not found")

        verify (exactly = 1) { userService.getUser(userId) }
    }
}