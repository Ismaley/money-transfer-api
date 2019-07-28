package com.revolut.transfer.exception

import java.lang.RuntimeException

class NotFoundException(message: String) : RuntimeException(message)

class AccountServiceException(message: String) : RuntimeException(message)