package com.medvedev.partpriceparser.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medvedev.partpriceparser.core.util.UIEvents
import com.medvedev.partpriceparser.core.util.printD
import com.medvedev.partpriceparser.core.util.safeTakeFirst
import com.medvedev.partpriceparser.domain.use_cases.GetProductsUseCase
import com.medvedev.partpriceparser.presentation.models.ParserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.launch
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

class ParserViewModel : ViewModel() {

    private val getProductsUseCase = GetProductsUseCase()

    private val _uiEvents = MutableSharedFlow<UIEvents>()
    val uiEvents: SharedFlow<UIEvents> = _uiEvents.asSharedFlow()

    private val _foundedProductList: SnapshotStateList<ParserData> = mutableStateListOf()
    val foundedProductList = _foundedProductList

    private fun addUIEvent(event: UIEvents) {
        viewModelScope.launch {
            _uiEvents.emit(event)
        }
    }


    private val Any.printMR
        get() = Timber.tag("developerMR").d(toString())

    fun temporaryParseProducts(articleToSearch: String) {

        viewModelScope.launch(Dispatchers.IO) {

            val linkToSite: String = "https://klassauto.ru"

            val siteName = "Марк"

            val localPartOfLinkToCatalog: (String) -> String = { article ->
                "/search/?search=$article"
            }

            "fullLink: $linkToSite${localPartOfLinkToCatalog(articleToSearch)}$".printMR

            val markLogin = "a9173959992@gmail.com"
            val markPassword = "уке987гр"
            val authLink = "https://klassauto.ru/cabinet/"

            val authCookies: Connection.Response = Jsoup.connect(authLink)
                .data(
                    "AuthPhase", "1",
                    "REQUESTED_FROM", "/",
                    "REQUESTED_BY", "GET",
                    "catalogue", "1",
                    "sub", "7",
                    "cc", "74",
                    "AUTH_USER", "a9173959992@gmail.com",
                    "AUTH_PW", "уке987гр",
                    "submit", "Авторизоваться"
                )
                .method(Connection.Method.POST)
                .execute()

            val cookies = authCookies.cookies()


            val document: Document =
                Jsoup.connect("$linkToSite${localPartOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                    .timeout(20 * 1000)
                    .cookies(cookies)
                    .post()

            val productElements = document
                .select("div.is-multiline")
                .select("div.catalog-item__tile-item-content")

            productElements.forEach { element ->

                val imageUrl = element.select("figure.image").select("img").attr("src")
                    .apply { "imageUrl: $this".printMR }

                val price = element.select("span.price").textNodes().safeTakeFirst
                    .apply { "price: $this".printMR }

                val brand =
                    element.select("a.param-vendor-value").textNodes().safeTakeFirst
                        .apply { "brand: $this".printMR }

                val article =
                    element.select("span.param-article-value").textNodes().safeTakeFirst
                        .apply { "article: $this".printMR }

                val name =
                    element.select("a.catalog-item__tile-item-title").textNodes().safeTakeFirst
                        .apply { "name: $this".printMR }

                val lintToProduct = element.select("a.catalog-item__tile-item-title").attr("href")
                    .apply { "lintToProduct: $this".printMR }

                val quantity =
                    element.select("span.stock-value").textNodes().safeTakeFirst
                        .apply { "quantity: $this".printMR }


                val existence = element.select("span.stock-title").textNodes().safeTakeFirst
                    .apply { "existence: $this".printMR }

                "\n".printMR
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    fun parseProducts(article: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _foundedProductList.clear()
            getProductsUseCase.execute(article)
                .buffer(10)
                .collect { data ->
                    synchronized(Object()) {
                        val iterator: MutableIterator<ParserData> = _foundedProductList.iterator()

                        while (iterator.hasNext()) {
                            val value = iterator.next()
                            if (value.siteName == data.siteName) {
                                iterator.remove()
                            }
                        }
                        _foundedProductList.add(data)
                    }
                }
        }
    }

    fun openBrowser(context: Context, linkToSite: String) {
        if (linkToSite.isNotEmpty()) {
            "start activity: $linkToSite".printD
            val openPageIntent = Intent(Intent.ACTION_VIEW, Uri.parse(linkToSite))
            context.startActivity(openPageIntent)
        } else {
            addUIEvent(event = UIEvents.SnackbarEvent(message = "Error: Link to Site is null or empty"))
        }
    }


    private val _textSearch = mutableStateOf("740.1003010-20")
    val textSearch = _textSearch

    fun changeTextSearch(text: String) {
        _textSearch.value = text
    }

    fun clearSearchText() {
        _textSearch.value = ""
    }
}