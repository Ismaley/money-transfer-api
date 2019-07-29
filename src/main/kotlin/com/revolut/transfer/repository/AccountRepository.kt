package com.revolut.transfer.repository

import com.revolut.transfer.model.Account
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.spring.tx.annotation.Transactional
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.LockModeType
import javax.persistence.PersistenceContext
import javax.persistence.PessimisticLockScope

@Singleton
open class AccountRepository(@param:CurrentSession @field:PersistenceContext private val entityManager: EntityManager) {

    @Transactional
    open fun save(account: Account): Account {
        entityManager.persist(account)
        return account
    }

    @Transactional
    open fun update(account: Account): Account {
        entityManager.merge(account)
        return account
    }

    @Transactional
    open fun getAccountForUpdate(accountId: Int): Account? =
        entityManager.find(Account::class.java, accountId, LockModeType.PESSIMISTIC_READ, getLockProperties())

    @Transactional
    open fun getAccount(accountId: Int): Account? =
        entityManager.find(Account::class.java, accountId)

    private fun getLockProperties(): Map<String, Any> =
        mapOf("javax.persistence.lock.scope" to PessimisticLockScope.EXTENDED,
            "javax.persistence.lock.timeout" to 1000L)

}