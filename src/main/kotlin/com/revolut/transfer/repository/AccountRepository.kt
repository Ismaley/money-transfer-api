package com.revolut.transfer.repository

import com.revolut.transfer.model.Account
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.spring.tx.annotation.Transactional
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Singleton
open class AccountRepository(@param:CurrentSession @field:PersistenceContext private val entityManager: EntityManager) {

    @Transactional
    open fun save(account: Account): Account {
        entityManager.persist(account)
        return account
    }

}