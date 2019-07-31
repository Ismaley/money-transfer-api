package com.revolut.transfer.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.revolut.transfer.dto.AccountRepresentation
import com.revolut.transfer.dto.UserRepresentation
import com.revolut.transfer.model.MoneyTransferResult
import com.revolut.transfer.util.toBD
import io.micronaut.context.ApplicationContext
import io.micronaut.core.io.IOUtils
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountControllerTest {
    
    private var server: EmbeddedServer? = null
    private var client: HttpClient? = null
    private var user: UserRepresentation? = null
    private val objectMapper = ObjectMapper()

    @BeforeAll
    fun setupServer() {
        server = ApplicationContext
            .build()
            .packages("com.revolut.transfer")
            .run(EmbeddedServer::class.java)
        client = server!!.applicationContext.createBean(HttpClient::class.java, server!!.url)

        user = createUser(IOUtils.readText(AccountControllerTest::class.java.getResourceAsStream("/json/create-user-request.json").bufferedReader()))

    }

    @AfterAll
    fun stopServer() {
        if (server != null) {
            server!!.stop()
        }
        if (client != null) {
            client!!.stop()
        }
    }

    @Test
    fun shouldCreateAccount() {
        val request = HttpRequest.POST("/accounts", objectMapper.createObjectNode().put("userId", user!!.id))
        val response = client!!.toBlocking().exchange(request, AccountRepresentation::class.java)
        val responseBody = response.body() as AccountRepresentation

        Assertions.assertEquals(201, response.code())
        Assertions.assertNotNull(responseBody.number)
        Assertions.assertEquals(BigDecimal(0), responseBody.balance)
    }

    @Test
    fun shouldNotCreateAccountForANonExistentUser() {
        val request = HttpRequest.POST("/accounts", objectMapper.createObjectNode().put("userId", "NonExistentUser"))

        val error = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client!!.toBlocking().exchange(request, JsonNode::class.java)
        }
        Assertions.assertEquals(400, error.response.code())
        Assertions.assertEquals("user with id: NonExistentUser does not exist", error.message)
    }

    @Test
    fun shouldDepositMoneyOnAccount() {
        val accountToDeposit = createAccount()

        val request = HttpRequest.POST("/accounts/${accountToDeposit.number}/deposits",
            objectMapper.createObjectNode()
                .put("userId", user!!.id)
                .put("amount", 10.99.toBD()))

        val response = client!!.toBlocking().exchange(request, AccountRepresentation::class.java)
        val responseBody = response.body() as AccountRepresentation

        Assertions.assertEquals(200, response.code())
        Assertions.assertEquals(accountToDeposit.number, responseBody.number)
        Assertions.assertEquals(10.99.toBD(), responseBody.balance)
    }

    @Test
    fun shouldNotDepositMoneyOnNonExistentAccount() {
        val request = HttpRequest.POST("/accounts/0/deposits",
            objectMapper.createObjectNode()
                .put("userId", user!!.id)
                .put("amount", 10.99.toBD()))

        val error = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client!!.toBlocking().exchange(request, JsonNode::class.java)
        }
        Assertions.assertEquals(404, error.response.code())
        Assertions.assertEquals("Account with id: 0 does not exists", error.message)
    }

    @Test
    fun shouldNotDepositInvalidAmountOnAccount() {
        val accountToDeposit = createAccount()

        val request = HttpRequest.POST("/accounts/${accountToDeposit.number}/deposits",
            objectMapper.createObjectNode()
                .put("userId", user!!.id)
                .put("amount", BigDecimal(0)))

        val error = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client!!.toBlocking().exchange(request, JsonNode::class.java)
        }
        Assertions.assertEquals(400, error.response.code())
        Assertions.assertEquals("amount must be greater than 0", error.message)
    }


    @Test
    fun shouldWithdrawMoneyFromAccount() {
        val accountToWithDraw = createAccount()
        depositOnAccount(accountToWithDraw.number, 20.99.toBD())

        val request = HttpRequest.POST("/accounts/${accountToWithDraw.number}/withdrawals",
            objectMapper.createObjectNode()
                .put("userId", user!!.id)
                .put("amount", 10.50.toBD()))

        val response = client!!.toBlocking().exchange(request, AccountRepresentation::class.java)
        val responseBody = response.body() as AccountRepresentation

        Assertions.assertEquals(200, response.code())
        Assertions.assertEquals(accountToWithDraw.number, responseBody.number)
        Assertions.assertEquals(10.49.toBD(), responseBody.balance)
    }

    @Test
    fun shouldNotWithdrawMoneyFromNonExistentAccount() {
        val request = HttpRequest.POST("/accounts/0/withdrawals",
            objectMapper.createObjectNode()
                .put("userId", user!!.id)
                .put("amount", 10.99.toBD()))

        val error = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client!!.toBlocking().exchange(request, JsonNode::class.java)
        }
        Assertions.assertEquals(404, error.response.code())
        Assertions.assertEquals("Account with id: 0 does not exists", error.message)
    }

    @Test
    fun shouldNotWithdrawInvalidAmountFromAccount() {
        val accountToDeposit = createAccount()

        val request = HttpRequest.POST("/accounts/${accountToDeposit.number}/withdrawals",
            objectMapper.createObjectNode()
                .put("userId", user!!.id)
                .put("amount", BigDecimal(0)))

        val error = Assertions.assertThrows(HttpClientResponseException::class.java) {
             client!!.toBlocking().exchange(request, JsonNode::class.java)
        }
        Assertions.assertEquals(400, error.response.code())
        Assertions.assertEquals("amount must be greater than 0", error.message)
    }

    @Test
    fun shouldTransferMoneyBetweenAccounts() {
        val sourceAccount = createAccount()
        val destinationAccount = createAccount()
        depositOnAccount(sourceAccount.number, 20.99.toBD())

        val request = HttpRequest.POST("/accounts/${sourceAccount.number}/transfers",
            objectMapper.createObjectNode()
                .put("userId", user!!.id)
                .put("destinationAccountId", destinationAccount.number)
                .put("amount", 10.45.toBD()))

        val response = client!!.toBlocking().exchange(request, MoneyTransferResult::class.java)
        val responseBody = response.body() as MoneyTransferResult

        Assertions.assertEquals(200, response.code())
        Assertions.assertEquals(sourceAccount.number, responseBody.sourceAccountNumber)
        Assertions.assertEquals(destinationAccount.number, responseBody.destinationAccountNumber)
        Assertions.assertEquals(10.45.toBD(), responseBody.amount)
        Assertions.assertNotNull(responseBody.createdAt)

        Assertions.assertEquals(10.54.toBD(), checkBalance(sourceAccount.number))
        Assertions.assertEquals(10.45.toBD(), checkBalance(destinationAccount.number))
    }

    @Test
    fun shouldNotTransferMoneyBetweenAccountsIfOneAccountDoesNotExists() {
        val sourceAccount = createAccount()

        val request = HttpRequest.POST("/accounts/${sourceAccount.number}/transfers",
            objectMapper.createObjectNode()
                .put("userId", user!!.id)
                .put("destinationAccountId", 0)
                .put("amount", 10.45.toBD()))

        val error = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client!!.toBlocking().exchange(request, JsonNode::class.java)
        }
        Assertions.assertEquals(404, error.response.code())
        Assertions.assertEquals("Account with id: 0 does not exists", error.message)
    }

    @Test
    fun shouldNotTransferMoneyBetweenAccountsIfSourceAccountDoesNotHaveSufficientFunds() {
        val sourceAccount = createAccount()
        val destinationAccount = createAccount()
        depositOnAccount(sourceAccount.number, 20.99.toBD())

        val request = HttpRequest.POST("/accounts/${sourceAccount.number}/transfers",
            objectMapper.createObjectNode()
                .put("userId", user!!.id)
                .put("destinationAccountId", destinationAccount.number)
                .put("amount", 500.45.toBD()))

        val error = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client!!.toBlocking().exchange(request, JsonNode::class.java)
        }
        Assertions.assertEquals(400, error.response.code())
        Assertions.assertEquals("Insufficient funds to transfer", error.message)
        Assertions.assertEquals(20.99.toBD(), checkBalance(sourceAccount.number))
    }

    @Test
    fun shouldListAccountTransactions() {
        val account = createAccount()
        depositOnAccount(account.number, 20.99.toBD())
        withdrawFromAccount(account.number, 10.44.toBD())

        val request: HttpRequest<String> = HttpRequest.GET("/accounts/${account.number}/transactions?userId=${user!!.id}")

        val response = client!!.toBlocking().exchange(request, ArrayNode::class.java)
        val responseBody = response.body() as ArrayNode

        Assertions.assertEquals(200, response.code())
        Assertions.assertEquals(2, responseBody.size())
        Assertions.assertEquals("DEPOSIT", responseBody.get(0).get("transactionType").asText())
        Assertions.assertEquals(user!!.name, responseBody.get(0).get("doneBy").asText())
        Assertions.assertEquals(20.99.toBD(), responseBody.get(0).get("amount").asDouble().toBD())
        Assertions.assertEquals("WITHDRAW", responseBody.get(1).get("transactionType").asText())
        Assertions.assertEquals(user!!.name, responseBody.get(1).get("doneBy").asText())
        Assertions.assertEquals(10.44.toBD(), responseBody.get(1).get("amount").asDouble().toBD())
    }

    private fun createUser(requestBody: String): UserRepresentation {
        val request: HttpRequest<String> = HttpRequest.POST("/users", requestBody)
        return client!!.toBlocking().exchange(request, UserRepresentation::class.java).body() as UserRepresentation
    }

    private fun createAccount(): AccountRepresentation {
        val request = HttpRequest.POST("/accounts", objectMapper.createObjectNode().put("userId", user!!.id))
        return client!!.toBlocking().exchange(request, AccountRepresentation::class.java).body() as AccountRepresentation
    }

    private fun withdrawFromAccount(accountNumber: Int, amount: BigDecimal) {
        performAccountTransaction(accountNumber, amount, "withdrawals")
    }

    private fun depositOnAccount(accountNumber: Int, amount: BigDecimal) {
        performAccountTransaction(accountNumber, amount)
    }

    private fun performAccountTransaction(accountNumber: Int, amount: BigDecimal, service: String = "deposits") {
        val request = HttpRequest.POST("/accounts/$accountNumber/$service",
            objectMapper.createObjectNode()
                .put("userId", user!!.id)
                .put("amount", amount))

        client!!.toBlocking().exchange(request, AccountRepresentation::class.java)
    }

    private fun checkBalance(accountNumber: Int): BigDecimal {
        val request: HttpRequest<String> = HttpRequest.GET("/accounts/$accountNumber/?userId=${user!!.id}")
        val response = client!!.toBlocking().exchange(request, AccountRepresentation::class.java).body() as AccountRepresentation
        return response.balance
    }

}