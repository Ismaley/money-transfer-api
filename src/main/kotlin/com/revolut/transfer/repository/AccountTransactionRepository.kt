package com.revolut.transfer.repository

import com.revolut.transfer.model.AccountTransaction
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.spring.tx.annotation.Transactional
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.LockModeType
import javax.persistence.PersistenceContext

@Singleton
open class AccountTransactionRepository(@param:CurrentSession @field:PersistenceContext private val entityManager: EntityManager) {

    @Transactional
    open fun save(accountTransaction: AccountTransaction): AccountTransaction {
        entityManager.persist(accountTransaction)
        return accountTransaction
    }
}
