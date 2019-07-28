package com.revolut.transfer.controller

import com.revolut.transfer.model.User
import com.revolut.transfer.service.UserService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post


@Controller("/users")
class UserController(private val userService: UserService) {

    @Post
    fun createUser(@Body newUser: User): User = userService.createUser(newUser)

    @Get("/{id}")
    fun getUser(@PathVariable userId: String): User = userService.getUser(userId)

}