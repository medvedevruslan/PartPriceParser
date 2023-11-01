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
import com.medvedev.partpriceparser.core.util.html2text
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


    private val Any.printKC
        get() = Timber.tag("developerKC").d(toString())

    fun temporaryParseProducts(articleToSearch: String) {

        viewModelScope.launch(Dispatchers.IO) {

            val linkToSite: String = "https://kamacenter.ru"

            val siteName = "КамаЦентр"

            val localPartOfLinkToCatalog: (String) -> String = { article ->
                "/search/?searchword=$article"
            }

            val fullLink = linkToSite + localPartOfLinkToCatalog(articleToSearch)

            "fullLink: $fullLink".printKC

            val document: Document =
                Jsoup.connect(fullLink) // 740.1003010-20 пример
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                    .timeout(10 * 1000)
                    .post()

            val productElements = document
                .select("div.products-list")
                .select("div.products-list__item")

            productElements.forEach { element ->

                val imageUrl = element.select("div.products-image").select("img").attr("src")
                    .apply { "imageUrl: $this".printKC }

                val partLinkToProduct = element.select("a.products-name__name").attr("href")
                    .apply { "linkToProduct: $this".printKC }

                val name = element.select("a.products-name__name").textNodes().safeTakeFirst
                    .apply { "name: $this".printKC }

                val price =
                    element.select("span.products-priceinfo__price").textNodes().safeTakeFirst
                        .apply { "price: $this".printKC }

                val brand = ""

                val article = element.select("table.products-table").select("td")
                    // .apply { "article: $this".printKC }

                var textArticle = ""

                if (article[0].toString() == "Артикул") {
                    textArticle = article[1].text().html2text
                    "textArticle: $textArticle".printKC
                }

                val existence = element.select("a.products__getmore").textNodes().safeTakeFirst
                    .apply { "existence: $this".printKC }

                val innerDocument = Jsoup.connect(linkToSite + partLinkToProduct)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                    .timeout(10 * 1000)
                    .post()

                val productInfo = innerDocument.select("div.good-stocks__first")
                    .select("div.good-priceinfo-stocks")

                val quantity =
                    productInfo.select("span.good-priceinfo-stocks__item").select("b")
                        .textNodes().safeTakeFirst
                        .apply { "quantity: $this".printKC }


                "\n".printKC
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    fun parseProducts(articleToSearch: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _foundedProductList.clear()
            getProductsUseCase.execute(articleToSearch)
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