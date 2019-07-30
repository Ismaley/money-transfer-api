package com.revolut.transfer.util

import java.math.BigDecimal

fun Double.toBD(): BigDecimal = BigDecimal(this).setScale(2, BigDecimal.ROUND_HALF_EVEN)