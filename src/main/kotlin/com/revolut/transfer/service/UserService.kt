package com.revolut.transfer.service

import com.revolut.transfer.exception.NotFoundException
import com.revolut.transfer.model.User
import com.revolut.transfer.repository.UserRepository
import javax.inject.Singleton

@Singleton
class UserService(private val userRepository: UserRepository) {

    fun createUser(newUser: User) = userRepository.save(newUser)
    fun getUser(userId: String): User = userRepository.findOne(userId) ?: throw NotFoundException("user with: $userId not found")

}