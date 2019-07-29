package com.revolut.transfer.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.revolut.transfer.exception.AccountServiceException
import com.revolut.transfer.exception.NotFoundException
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import javax.inject.Singleton

@Produces
@Singleton
@Requires(classes = [NotFoundException::class, ExceptionHandler::class])
class NotFoundExceptionHandler : ExceptionHandler<NotFoundException, HttpResponse<Any>> {

    private val jsonMapper: ObjectMapper = ObjectMapper()

    override fun handle(request: HttpRequest<Any>, exception: NotFoundException): HttpResponse<Any> {
        return HttpResponse.notFound(jsonMapper.createObjectNode().put("message", exception.message))
    }
}

@Produces
@Singleton
@Requires(classes = [AccountServiceException::class, ExceptionHandler::class])
class AccountServiceExceptionHandler : ExceptionHandler<AccountServiceException, HttpResponse<Any>> {

    private val jsonMapper: ObjectMapper = ObjectMapper()

    override fun handle(request: HttpRequest<Any>, exception: AccountServiceException): HttpResponse<Any> {
        return HttpResponse.badRequest(jsonMapper.createObjectNode().put("message", exception.message))
    }
}