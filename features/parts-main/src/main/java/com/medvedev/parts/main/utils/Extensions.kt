package com.medvedev.parts.main.utils

import timber.log.Timber

val Any.printD
    get() = Timber.tag("developer_main").d(toString())

val Any.printE
    get() = Timber.tag("developer").e(toString())
