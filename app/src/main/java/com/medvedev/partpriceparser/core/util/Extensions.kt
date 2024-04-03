package com.medvedev.partpriceparser.core.util

import timber.log.Timber

val Any.printD
    get() = Timber.tag("developer1").d(toString())

val Any.printE
    get() = Timber.tag("developer").e(toString())
