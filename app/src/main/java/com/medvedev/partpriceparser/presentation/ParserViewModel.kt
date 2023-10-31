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
import java.util.Locale

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


    private val Any.printAM
        get() = Timber.tag("developerRT").d(toString())

    fun temporaryParseProducts(articleToSearch: String) {

        viewModelScope.launch(Dispatchers.IO) {

            val linkToSite: String = "https://auto-motors.ru"

            val localPartOfLinkToCatalog: (String) -> String = { article ->
                "/catalog/?q=$article"
            }

            val cookieResponse: Connection.Response =
                Jsoup.connect("https://auto-motors.ru/AM_autorize_AUT/")
                    .data(
                        "USER_LOGIN", "info@dvizh-dvizh.ru",
                        "USER_PASSWORD", "info@dvizh-dvizh.ru",
                        "USER_REMEMBER", "Y",
                        "AUTH_ACTION", "Войти"
                    )
                    .method(Connection.Method.POST)
                    .execute()

            val cookies = cookieResponse.cookies()

            val document: Document =
                Jsoup.connect("$linkToSite${localPartOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                    .timeout(10 * 1000)
                    .cookies(cookies)
                    .post()

            /*val document: Document =
                                Jsoup.connect("$linkToSite$partOfLinkToCatalog$articleToSearch") // 740.1003010-20 пример
                                    .timeout(10 * 1000).get()*/

            // parseText = document.text()
            /*"tagName: ${document.tagName()}".printD
            "head: ${document.head()}".printD
            "tag: ${document.tag()}".printD
            "tagNameDiv: ${document.tagName("div")}".printD
            "body: ${document.body()}".printD*/

            // val productList: MutableList<ProductCart> = mutableListOf()


            val productElements = document.select("div.product-card-list")

            productElements.forEach { element ->

                /*val price1 = element.getElementsMatchingOwnText("₽").first()?.html()?.let { Jsoup.parse(it).text() }.apply { printD }*/


                //val linkToPicture = element.select("img").apply { printD }


                /* element.getElementsMatchingOwnText("/brands/brand/?brand_name").printD
                 element.getElementsByAttributeStarting("/brands/brand/?brand_name").printD
                 element.getElementsContainingOwnText("/brands/brand/?brand_name").printD
                 element.getElementsByAttributeValueContaining("a","href").printD*/

                // element.select("a").attr("href").printD

                // val alternativeArticle = element.select("p.m_none hidden").apply { printD }

                /*val article = element.select("p.m_none").apply { printD }
                article.forEachIndexed { index, element ->
                    "$index $element".printD
                }*/


                // element.select("p.m_none").printD

                val articleInfo = element.select("p.m_none")
                val article = articleInfo[0].text().html2text.apply { "article: $this".printAM }

                // val dopArticle = articleInfo[1].text().html2text.apply { "dopArticle: $this".printD }
                val dopArticle =
                    if (articleInfo.size > 3) articleInfo[1].text().html2text else ""
                "dopArticle: $dopArticle".printAM

                val existenceText = if (articleInfo.size > 3) {
                    "article info size ${articleInfo.size}".printAM
                    (articleInfo[2].text().html2text.lowercase(Locale.getDefault()) + " " + articleInfo[3].text().html2text).apply { "existenceText1: $this".printAM }
                } else {
                    (articleInfo[1].text().html2text.lowercase(Locale.getDefault()) + " " + articleInfo[2].text().html2text).apply { "existenceText2: $this".printAM }
                }


                val infoProductElements = element.getElementsByAttribute("alt")
                val alternativeName: String =
                    infoProductElements.attr("alt").apply { "alternativeName: $this".printAM }
                val imgSrc = infoProductElements.attr("src").apply { "imgSrc: $this".printAM }
                val name = infoProductElements.attr("title").apply { "name: $this".printAM }

                val halfLinkToProduct =
                    element.select("a.link-fast-view")
                        .attr("data-url").html2text.apply { "halfLink: $this".printAM }
                val brand = element.select("p.brand_name")
                    .text().html2text.apply { "brand: $this".printAM }

                val price: String? =
                    element.select("div.price").first()
                        ?.html()?.html2text.apply { "price: $this".printAM }










                "\n".printAM
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