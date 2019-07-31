package com.revolut.transfer.exception

class NotFoundException(message: String) : RuntimeException(message)

class AccountServiceException(message: String) : RuntimeException(message)