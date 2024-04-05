package com.medvedev.partsparser.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode


val String.html2text: String
    get() = Jsoup.parse(this).text().trim()

val List<TextNode>.safeTakeFirst: String
    get() = run {
        if (this.isNotEmpty()) {
            first().text().html2text
        } else ""
    }