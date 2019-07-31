package com.revolut.transfer

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("com.revolut.transfer")
            .mainClass(Application.javaClass)
            .start()
    }
}