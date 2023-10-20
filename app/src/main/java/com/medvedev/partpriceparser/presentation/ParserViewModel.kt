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
import com.medvedev.partpriceparser.domain.use_cases.GetProductsUseCase
import com.medvedev.partpriceparser.presentation.models.ParserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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


    private val Any.printTFK
        get() = Timber.tag("developer TFK").d(toString())

    fun temporaryParseProducts(articleToSearch: String) {

        viewModelScope.launch(Dispatchers.IO) {

            val linkToSite: String = "https://skladtfk.ru"
            val localPartOfLinkToCatalog: String = "https://skladtfk.ru/catalog/search/?text="

            val document: Document =
                Jsoup.connect("$localPartOfLinkToCatalog$articleToSearch&s=") // 740.1003010-20 пример
                    .timeout(10 * 1000).get()

            val productElements = document.select("td.table-body-image")
            // div.tovarlist catalog__search_virt_action_list
            // "productElements: $productElements".printTFK

            productElements.forEachIndexed { index, element ->
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
                /*productInfo[0].getElementsMatchingOwnText("Артикул:").apply { "getE1: $this".printTFK }
                productInfo[0].getElementsByAttributeStarting("Артикул:").apply { "getE2: $this".printTFK }
                productInfo[0].getElementsContainingOwnText("Артикул:").apply { "getE3: $this".printTFK }
                productInfo[0].getElementsByAttributeValueContaining("span","Артикул:").apply { "getE4: $this".printTFK }
                productInfo[0].getElementsByAttribute("Артикул:").apply { "getE5: $this".printTFK }*/

                /*productInfo[0].allElements.forEach {
                    "productInfoForEach: $it".printTFK
                }*/

                /* val article1 = productInfo.attr("div.tovarcard-top-props span span").apply { "article1: $this".printTFK }
                 val article3 = productInfo.attr("div.tovarcard-top-props span span").apply { "article3: $this".printTFK }
                 // val article2 = productInfo.attr("Артикул:").apply { "article2: $this".printTFK }
                 val brand = productInfo.attr("Производитель::").apply { "brand: $this".printTFK }*/


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


            /*
            val price: String,
            val alternativeName: String,
            val article: String,
            val additionalArticles: String,
            val brand: String,
            val quantity: Int,
            val existence: String*/

        }
    }

    fun parseProducts(article: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _foundedProductList.clear()
            getProductsUseCase.execute(article).collect { data ->
                _foundedProductList.forEachIndexed { index, parserData ->
                    if (parserData.siteName == data.siteName) {
                        _foundedProductList.removeAt(index)
                    }
                }
                _foundedProductList.add(data)
            }
        }
    }

    fun openBrowser(context: Context, linkToSite: String) {
        if (linkToSite.isNotEmpty()) {
            "start activity: $linkToSite".printTFK
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