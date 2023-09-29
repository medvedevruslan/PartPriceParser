package com.medvedev.partpriceparser.feature_parsers

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.presentation.models.ProductCart
import kotlinx.coroutines.flow.Flow

interface ProductParser {

    val linkToSite: String
    val siteName: String

    suspend fun getProduct(articleToSearch: String): Flow<Resource<List<ProductCart>>>

}


/* KSOUP library пример
LaunchedEffect(true) {

    var parseText: String = ""

    val url = URL("https://auto-motors.ru/catalog/?q=740.1003010-20")
    val urlConnection =
        withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection

    try {
        withContext(Dispatchers.IO) {
            parseText = urlConnection.inputStream.bufferedReader().readText()
            // .also { it.printD }
        }
    } finally {
        urlConnection.disconnect()
    }
    parseHtml(parseText)
    // parseText.printD
}



    private fun parseHtml(html: String) {
        var parseText: String = ""

        val handler = KsoupHtmlHandler
            .Builder()
            .onOpenTag { name, attributes, isImlied ->
                /*"name: $name".printD
                "attributes: $attributes".printD
                "isImplied: $isImlied".printD*/
            }

            .onAttribute { name, value, quote ->
                "name: $name".printD
                "value: $value".printD
                "quote: $quote".printD
            }
            .onOpenTagName {

            }
            .onText { text ->
                parseText = text
                // text.printD
            }.build()

        val ksoupHtmlParser = KsoupHtmlParser(handler = handler)

        ksoupHtmlParser.write(html)
        // ksoupHtmlParser.end()

        // parseText.printD
    }



*/