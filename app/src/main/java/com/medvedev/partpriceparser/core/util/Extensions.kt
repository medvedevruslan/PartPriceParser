package com.medvedev.partpriceparser.core.util

import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import timber.log.Timber

val Any.printD
    get() = Timber.tag("developer1").d(toString())

val Any.printE
    get() = Timber.tag("developer").e(toString())

val String.html2text: String
    get() = Jsoup.parse(this).text().trim()

val List<TextNode>.safeTakeFirst: String
    get() = run {
        if (this.isNotEmpty()) {
            first().text().html2text
        } else ""
    }