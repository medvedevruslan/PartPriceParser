package com.medvedev.partpriceparser.core.util

import org.jsoup.Jsoup
import timber.log.Timber

val Any.printD
    get() = Timber.tag("developer1").d(toString())

val String.html2text: String
    get() = Jsoup.parse(this).text()