package com.medvedev.partpriceparser.feature_parsers

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.presentation.models.ProductCart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

abstract class ProductParser {

    abstract val linkToSite: String
    abstract val siteName: String
    abstract val partOfLinkToCatalog: (String) -> String

    protected val documentCatalogAddressLink: (String) -> Document
        get() = { article ->
            Jsoup.connect("$linkToSite${partOfLinkToCatalog(article)}") // 740.1003010-20 пример
                .timeout(10 * 1000).get()
        }

    protected val productList: MutableList<ProductCart> = mutableListOf()


    suspend fun getProduct(articleToSearch: String): Flow<Resource<List<ProductCart>>> = flow {
        try {
            if (productList.size > 0) productList.clear()
            emit(Resource.Loading())
            workWithServer(articleToSearch).collect {
                emit(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(e.toString()))
        }
    }


    protected abstract val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>

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