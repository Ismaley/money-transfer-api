package com.revolut.transfer.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.revolut.transfer.dto.UserRepresentation
import com.revolut.transfer.exception.AccountServiceException
import io.micronaut.context.ApplicationContext
import io.micronaut.core.io.IOUtils
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
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
class UserControllerTest {
    
    private var server: EmbeddedServer? = null
    private var client: HttpClient? = null

    @BeforeAll
    fun setupServer() {
        server = ApplicationContext
            .build()
            .packages("com.revolut.transfer")
            .run(EmbeddedServer::class.java)
        client = server!!.applicationContext.createBean(HttpClient::class.java, server!!.url)
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
    fun shouldCreateUser() {
        val requestBody = IOUtils.readText(UserControllerTest::class.java.getResourceAsStream("/json/create-user-request.json").bufferedReader())

        val response = createUserRequest(requestBody)
        val responseBody = response.body() as UserRepresentation

        Assertions.assertEquals(201, response.code())
        Assertions.assertEquals("Jhon Doe", responseBody.name)
        Assertions.assertEquals("123.123.001-61", responseBody.documentNumber)
    }

    @Test
    fun shouldNotCreateUserWithInvalidInput() {
        val requestBody = IOUtils.readText(UserControllerTest::class.java.getResourceAsStream("/json/create-user-request-invalid.json").bufferedReader())

        val error = Assertions.assertThrows(HttpClientResponseException::class.java) {
            createUserRequest(requestBody)
        }
        Assertions.assertEquals(400, error.response.code())
    }

    @Test
    fun shouldGetUser() {
        val requestBody = IOUtils.readText(UserControllerTest::class.java.getResourceAsStream("/json/create-user-request.json").bufferedReader())

        val createdUser = createUserRequest(requestBody).body() as UserRepresentation

        val request: HttpRequest<String> = HttpRequest.GET("/users/${createdUser.id}")
        val response = client!!.toBlocking().exchange(request, UserRepresentation::class.java)
        val foundUser = response.body() as UserRepresentation

        Assertions.assertEquals(200, response.code())
        Assertions.assertEquals(createdUser.id, foundUser.id)
        Assertions.assertEquals("Jhon Doe", foundUser.name)
        Assertions.assertEquals("123.123.001-61", foundUser.documentNumber)
    }

    @Test
    fun shouldNotGetNonExistingUser() {
        val request: HttpRequest<String> = HttpRequest.GET("/users/NonExistentUser")
        val error = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client!!.toBlocking().exchange(request, JsonNode::class.java)
        }
        Assertions.assertEquals(404, error.response.code())
        Assertions.assertEquals("user with id: NonExistentUser not found", error.message)
    }

    private fun createUserRequest(requestBody: String): HttpResponse<UserRepresentation> {
        val request: HttpRequest<String> = HttpRequest.POST("/users", requestBody)
        return client!!.toBlocking().exchange(request, UserRepresentation::class.java)
    }
}