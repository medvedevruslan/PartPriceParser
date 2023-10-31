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


    private val Any.printRT
        get() = Timber.tag("developerRT").d(toString())

    fun temporaryParseProducts(articleToSearch: String) {

        viewModelScope.launch(Dispatchers.IO) {

            val linkToSite = "https://tdriat.ru"
            val siteName = "ООО «ТД «РИАТ-Запчасть»"

            val localPartOfLinkToCatalog: (String) -> String = { article ->
                "/poisk/$article"
            }

            val cookieResponse: Connection.Response =
                Jsoup.connect("$linkToSite${localPartOfLinkToCatalog(articleToSearch)}")
                    .method(Connection.Method.GET)
                    .execute()

            val cookies = cookieResponse.cookies()

            val document: Document =
                Jsoup.connect("$linkToSite${localPartOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                    .timeout(10 * 1000)
                    .cookies(cookies)
                    .post()

            val productElements = document.select("tr.cart_table_item")

            productElements.forEach { element ->

                "productElements: $element".printRT

                val partLinkToProduct = element.select("a").attr("href")
                    .apply { "partLinkToProduct: $this".printRT } //

                val imageUrl: String? = element.select("img.img-responsive").attr("src")
                    .apply { "imageUrl: $this".printRT }//

                val name: String =
                    element.select("td.product-name")
                        .select("a")
                        .textNodes()
                        .first()
                        .text().html2text
                        .apply { "name: $this".printRT } //

                val article = element
                    .select("td.product-name")
                    .after("a")
                    .textNodes()
                    .first()
                    .text().html2text.trim()
                    .apply { "article: $this".printRT }

                var price =
                    element.select("td.product-price").select("span.amount").text().html2text
                        .apply { "price: $this".printRT }

                val existence: String =
                    element.select("span.amount").select("small").text().html2text
                        .apply { "existence: $this".printRT }

                if (existence.isNotBlank()) {
                    if (price.contains(existence)) {
                        price = price.removeSuffix(existence).trim()
                        "price is cleaned: $price".printRT
                    }
                }

                val innerDocument = Jsoup.connect("$linkToSite$partLinkToProduct")
                    .timeout(30 * 1000).get()


                /*val productInfoA = innerDocument.select("div.summary")
                    .apply { "productInfoA: $this".printRT }


                val priceA = productInfoA.select("span.amount")
                    .apply { "priceA: $this".printRT }*/

                val productInfo = innerDocument.select("p.taller")
                    .apply { "productInfo: $this".printRT }

                var brand = ""

                productInfo[0].select("b").mapIndexed { productIndex, productElement ->
                    productElement.text().html2text.removeSuffix(":").also { textElement ->
                        if (textElement.contains("Производитель")) {

                            brand = productInfo[0].getElementsMatchingText("Производитель")
                                .textNodes()[1].toString().trim()
                                .apply { "brand: $this".printRT }

                            "data contains brand: $productIndex".printRT
                        }
                    }
                }

                /*
                                element.getElementsMatchingOwnText("href").apply { "getE1: $this".printRT }
                                element.getElementsByAttributeStarting("href").apply { "getE2: $this".printRT }
                                element.getElementsContainingOwnText("href").apply { "getE3: $this".printRT }
                                element.getElementsByAttributeValueContaining("href", "product-name:")
                                    .apply { "getE4: $this".printRT }
                                element.getElementsByAttribute("href").apply { "getE5: $this".printRT }*/


                /*
                val partLinkToProduct =
                    element.select("a").attr("href")
                .apply { "partLinkToProduct: $this".printRT } //

                var name = element.select("img").attr("alt").html2text
                    .apply { "name: $this".printRT }//


                val imageUrl =
                    element.select("img.img-responsive").attr("src")
                .apply { "imageUrl: $this".printRT }//
                */


                /*val article =
                    element.allElements[0].allElements[2]
                        .apply { "article: $this".printRT }*/

                //https://tdriat.ru


                /*element.allElements[0].allElements.forEachIndexed { index, element ->
                    "foreach`s$index: $element".printRT
                    element.allElements.forEachIndexed { innerIndex, innerElement ->
                        "inner foreach`s: $index - $innerIndex: $innerElement".printRT
                        innerElement.allElements.forEachIndexed { inner2Index, inner2Element ->
                            "inner 2 foreach`s: $index - $innerIndex - $inner2Index: $inner2Element".printRT
                        }
                    }
                }*/


                /*element.allElements.forEach {
                    "productElementsForEach: ${it.allElements}".printRT
                }*/

                "\n".printRT
            }


            /*
            val fullLinkToProduct: String,
            val fullImageUrl: String,
            val price: String?,
            val name: String,
            val alternativeName: String?,
            val article: String,
            val additionalArticles: String?,
            val brand: String,
            val quantity: String?,
            val existence: String?
             */


            /*productElements.forEachIndexed { index, element ->
                val linkToProduct =
                    element.select("a").attr("href").apply { "linkToProduct: $this".printTFK }
                val name = element.select("img").attr("alt").apply { "name: $this".printTFK }
                val imageUrl =
                    element.select("img").attr("src").apply { "imageUrl: $this".printTFK }
                val alternativeName: String = ""

                val innerDocument: Document =
                    Jsoup.connect("$linkToSite$linkToProduct").timeout(10 * 1000).get()

                val productInfo = innerDocument.select("span.tovarcard-top-prop")

                val article =
                    productInfo[0].child(1).text().html2text.apply { "article: $this".printTFK }
                val brand =
                    productInfo[1].child(1).text().html2text.apply { "brand: $this".printTFK }


                // val spanArticle = productInfo.select("span.tovarcard-top-prop").apply { "spanArticle: $this".printTFK }
                // val article = productInfo.text().html2text.apply { "article: $this".printTFK }
                *//*productInfo[0].getElementsMatchingOwnText("Артикул:").apply { "getE1: $this".printTFK }
                productInfo[0].getElementsByAttributeStarting("Артикул:").apply { "getE2: $this".printTFK }
                productInfo[0].getElementsContainingOwnText("Артикул:").apply { "getE3: $this".printTFK }
                productInfo[0].getElementsByAttributeValueContaining("span","Артикул:").apply { "getE4: $this".printTFK }
                productInfo[0].getElementsByAttribute("Артикул:").apply { "getE5: $this".printTFK }*//*

                *//*productInfo[0].allElements.forEach {
                    "productInfoForEach: $it".printTFK
                }*//*

                *//* val article1 = productInfo.attr("div.tovarcard-top-props span span").apply { "article1: $this".printTFK }
                 val article3 = productInfo.attr("div.tovarcard-top-props span span").apply { "article3: $this".printTFK }
                 // val article2 = productInfo.attr("Артикул:").apply { "article2: $this".printTFK }
                 val brand = productInfo.attr("Производитель::").apply { "brand: $this".printTFK }*//*


                val innerProductElements = innerDocument.select("div.tbody")
                // .apply { "innerProductElements: $this".printTFK }


                innerProductElements.forEachIndexed { innerIndex, innerElement ->

                    val price = innerElement.select("div.td_cena").first()?.text()?.html2text
                        .apply { "price: $this".printTFK }


                    val existenceText =
                        innerElement.select("div.td_quantity").first()?.text()?.html2text
                            .apply { "existenceText: $this".printTFK }
                }
                "\n".printTFK
            }
*/
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
                        "size: ${_foundedProductList.size}".printD

                        val iterator: MutableIterator<ParserData> = _foundedProductList.iterator()

                        while (iterator.hasNext()) {
                            val value = iterator.next()
                            if (value.siteName == data.siteName) {
                                iterator.remove()
                            }
                        }


                        /*for (i in 0 until _foundedProductList.size) {
                            "Index: $i".printD
                            if (_foundedProductList[i].siteName == data.siteName) {
                                _foundedProductList.removeAt(i)
                            }
                        }*/


                        /*_foundedProductList.forEachIndexed { index, parserData ->
                            if (parserData.siteName == data.siteName) {
                                val valueToDelete = _foundedProductList[index]

                                val list: MutableList<ParserData> =
                                    mutableStateListOf<ParserData>().apply {
                                        add(_foundedProductList[index])
                                    }

                                _foundedProductList.removeAll(list)
                            }
                        }*/
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