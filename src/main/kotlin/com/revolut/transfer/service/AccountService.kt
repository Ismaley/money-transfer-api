package com.revolut.transfer.service

import com.revolut.transfer.exception.AccountServiceException
import com.revolut.transfer.model.Account
import com.revolut.transfer.repository.AccountRepository
import javassist.NotFoundException
import java.lang.RuntimeException
import javax.inject.Singleton
@Singleton
class AccountService(private val accountRepository: AccountRepository,
                     private val userService: UserService) {

    fun createAccount(userId: String): Account {
        try {
            return accountRepository.save(Account(user = userService.getUser(userId)))
        } catch (e: Exception) {
            if(e is NotFoundException) {
                throw AccountServiceException("user with id: $userId does not exist")
            } else {
                throw RuntimeException(e)
            }
        }
    }

}

