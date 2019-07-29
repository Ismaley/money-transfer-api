package com.revolut.transfer.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.revolut.transfer.dto.UserRepresentation
import io.micronaut.context.ApplicationContext
import io.micronaut.core.io.IOUtils
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.InputStream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    private val objectMapper: ObjectMapper = ObjectMapper()

    private var server: EmbeddedServer? = null // <1>
    private var client: HttpClient? = null // <2>

    @BeforeAll
    fun setupServer() {
        server = ApplicationContext
            .build()
            .packages("com.revolut.transfer")
            .run(EmbeddedServer::class.java) // <1>
        client = server!!.applicationContext.createBean(HttpClient::class.java, server!!.url) // <2>
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

//    @Inject
//    @field:Client("/")
//    private lateinit var client: RxHttpClient



    @Test
    fun shouldCreateUser() {
        val input: InputStream = UserControllerTest::class.java.getResourceAsStream("/json/createUserRequest.json")
        val body = IOUtils.readText(input.bufferedReader())

        val request: HttpRequest<String> = HttpRequest.POST("/users", body)

        val response = client!!.toBlocking().exchange(request, UserRepresentation::class.java)
        val responseBody = response.body() as UserRepresentation


        Assertions.assertEquals(HttpStatus.OK, response.status)
        Assertions.assertEquals(200, response.code())
        Assertions.assertEquals("Jhon Doe", responseBody.name)
        Assertions.assertEquals("123.123.001-61", responseBody.documentNumber)
    }




}