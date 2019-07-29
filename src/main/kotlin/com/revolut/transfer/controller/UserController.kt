package com.revolut.transfer.controller

import com.revolut.transfer.dto.CreateUserRequest
import com.revolut.transfer.dto.UserRepresentation
import com.revolut.transfer.model.User
import com.revolut.transfer.service.UserService
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.validation.Validated
import javax.validation.Valid

@Validated
@Controller("/users")
class UserController(private val userService: UserService) {

    @Post
    @Status(HttpStatus.CREATED)
    fun createUser(@Valid @Body createUserRequest: CreateUserRequest): UserRepresentation =
        UserRepresentation(userService.createUser(User(documentNumber = createUserRequest.documentNumber,
            birthDate = createUserRequest.birthDate,
            name = createUserRequest.name)))

    @Get("/{userId}")
    fun getUser(@PathVariable userId: String): UserRepresentation = UserRepresentation(userService.getUser(userId))

}