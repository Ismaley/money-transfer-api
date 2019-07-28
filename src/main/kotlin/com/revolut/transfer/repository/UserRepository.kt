package com.revolut.transfer.repository

import com.revolut.transfer.model.User
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.spring.tx.annotation.Transactional
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Singleton
open class UserRepository(@param:CurrentSession @field:PersistenceContext private val entityManager: EntityManager) {

    @Transactional
    open fun save(entity: User): User {
        entityManager.persist(entity)
        return entity
    }

    @Transactional
    open fun findOne(id: String): User? = entityManager.find(User::class.java, id)

}