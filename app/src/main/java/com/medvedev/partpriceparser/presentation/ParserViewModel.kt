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


    private val Any.printNK
        get() = Timber.tag("developeNk").d(toString())

    private fun temporaryParseProducts(articleToSearch: String) {

        viewModelScope.launch(Dispatchers.IO) {

            val linkToSite: String = "https://нико.рф"

            val partOfLinkToCatalog: (String) -> String = { article ->
                "/search/?nc_ctpl=2052&find=$article"
            }

            val fullLink = linkToSite + partOfLinkToCatalog(articleToSearch)

            "fullLink: $fullLink".printNK

            val document: Document =
                Jsoup.connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                    .timeout(10 * 1000)
                    .get()


            val productElements = document
                .select("div.catalog-items-list")
                .select("div.catalog-item")
                .select("div.blklist_main")
                .apply { "productElements: $this".printNK }

            productElements.forEach { element ->

                val imageUrl = element
                    .select("div.blklist_photo")
                    .select("div.image-default")
                    .select("img")
                    .attr("data-src")
                    .apply { "imageUrl: $this".printNK }

                val dopArticle = ""
                var name = ""
                var article = ""
                var partLinkToProduct = ""

                element
                    .select("div.blklist_info")
                    .select("div.blk_listfirst")
                    .apply {
                        partLinkToProduct =
                            select("div.blk_name").select("a").attr("href").html2text
                        name = select("div.blk_name").select("span").text().html2text
                        article = select("div.blk_art").select("span.art_value").text().html2text
                    }

                "partLinkToProduct: $partLinkToProduct".printNK
                "name: $name".printNK
                "article: $article".printNK

                var price = ""
                var existence = ""

                element.select("div.blklist_price")
                    .apply {
                        price = select("div.blk_priceblock ").select("div.normal_price")
                            .select("span.cen").text()
                        existence = select("div.blk_stock").select("span").textNodes().safeTakeFirst
                    }

                "price: $price".printNK
                "existence: $existence".printNK

                /*


                                val partLinkToProduct1 = element
                                    .select("div.blk_text")
                                    .select("div.blk_bordertext")
                                    .select("div.blk_name")
                                    .select("a")
                                    .attr("href")
                                    .apply { "halfLinkToProduct: $this".printNK }


                                val brand = ""*//*element
                    .select("div.codes")
                    .select("div.small-text")
                    .select("a")
                    .select("b")
                    .text().html2text
                    .apply { "brand: $this".printAM }*//*

                val price1 = element
                    .select("div.blk_buyinfo")
                    .select("span.cen")
                    .textNodes().safeTakeFirst
                    .let {
                        if (it.isNotEmpty()) "$it ₽" else it
                    }
                    .apply { "price: $this".printNK }

                val quantity = ""

                val existence1 = element
                    .select("span.c_nalich")
                    .textNodes().safeTakeFirst
                    .apply { "existence: $this".printNK }*/


                "\n".printNK
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
                            _foundedProductList.sortBy { it.siteName }
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