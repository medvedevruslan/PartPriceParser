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
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // temporaryParseProducts("740.1003010-20")
            // temporaryParseProducts("740-1003010")
            // temporaryParseProducts("агрегат")
        }
    }


    private val Any.printAM
        get() = Timber.tag("developerAM").d(toString())

    private fun temporaryParseProducts(articleToSearch: String) {

        viewModelScope.launch(Dispatchers.IO) {

            val linkToSite: String = "https://avtokama.ru"

            val partOfLinkToCatalog: (String) -> String = { article ->
                "/search?searched=$article"
            }

            val fullLink = linkToSite + partOfLinkToCatalog(articleToSearch)

            "fullLink: $fullLink".printAM

            val document: Document =
                Jsoup.connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                    .timeout(10 * 1000)
                    .get()


            val productElements = document
                .select("div.teaser")

            productElements.forEach { element ->

                val article = element
                    .select("div.info")
                    .select("div.codes")
                    .select("div.small-text")
                    .let {
                        val articleName = it.select("b").first()?.text()?.html2text
                        val articleText = it.textNodes().safeTakeFirst
                        "$articleText $articleName"
                    }
                    .apply { "article: $this".printAM }



                val dopArticle = ""


                val existence = element
                    .select("div.items-center")
                    .select("div.flex")
                    .select("div.font-bold")
                    .textNodes().safeTakeFirst
                    .apply { "existence: $this".printAM }


                val imageUrl = element
                    .select("a")
                    .select("img")
                    .attr("src")
                    .apply { "imageUrl: $this".printAM }


                val name = element
                    .select("div.info")
                    .select("a")
                    .textNodes().safeTakeFirst
                    .apply { "name: $this".printAM }


                val partLinkToProduct = element
                    .select("div.info")
                    .select("a")
                    .attr("href")
                    .apply { "halfLinkToProduct: $this".printAM }


                val brand = element
                    .select("div.codes")
                    .select("div.small-text")
                    .select("a")
                    .select("b")
                    .text().html2text
                    .apply { "brand: $this".printAM }

                val price = element
                    .select("div.justify-between")
                    .select("div.price-line")
                    .select("div.price")
                    .textNodes().safeTakeFirst
                    .let {
                        if (it.isNotEmpty()) "$it ₽" else it
                    }
                    .apply { "price: $this".printAM }

                val quantity = ""

                "\n".printAM
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    fun parseProducts(articleToSearch: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _foundedProductList.clear()
            if (articleToSearch.isEmpty()) {
                addUIEvent(UIEvents.SnackbarEvent(message = "Введите артикул"))
            } else {
                getProductsUseCase.execute(articleToSearch)
                    .buffer(10)
                    .collect { data ->
                        synchronized(Object()) {
                            val iterator: MutableIterator<ParserData> =
                                _foundedProductList.iterator()

                            while (iterator.hasNext()) {
                                val value = iterator.next()
                                if (value.siteName == data.siteName) {
                                    iterator.remove()
                                }
                            }
                            _foundedProductList.add(data)
                            _foundedProductList.sortBy {
                                it.siteName
                            }
                        }
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