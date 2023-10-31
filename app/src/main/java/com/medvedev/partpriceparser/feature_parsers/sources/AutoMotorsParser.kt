package com.medvedev.partpriceparser.feature_parsers.sources

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.html2text
import com.medvedev.partpriceparser.feature_parsers.ProductParser
import com.medvedev.partpriceparser.presentation.models.ProductCart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.util.Locale


class AutoMotorsParser : ProductParser() {

    override val linkToSite: String = "https://auto-motors.ru"
    override val siteName: String = "АВТОМОТОРС"
    override val partOfLinkToCatalog: (String) -> String
        get() = { article ->
            "/catalog/?q=$article"
        }

    val Any.printAM
        get() = Timber.tag("developerAM").d(toString())

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>
        get() = { articleToSearch ->
            flow {

                val authLink = "https://auto-motors.ru/AM_autorize_AUT/"

                val authCookies: Connection.Response =
                    Jsoup.connect(authLink)
                        .data(
                            "USER_LOGIN", "info@dvizh-dvizh.ru",
                            "USER_PASSWORD", "info@dvizh-dvizh.ru",
                            "USER_REMEMBER", "Y",
                            "AUTH_ACTION", "Войти"
                        )
                        .method(Connection.Method.POST)
                        .execute()

                val cookies = authCookies.cookies()

                val document: Document =
                    Jsoup.connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                        .timeout(10 * 1000)
                        .cookies(cookies)
                        .post()

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

                    productList.add(
                        ProductCart(
                            fullLinkToProduct = linkToSite + halfLinkToProduct,
                            fullImageUrl = linkToSite + imgSrc,
                            price = price,
                            name = name,
                            alternativeName = alternativeName,
                            article = article,
                            additionalArticles = dopArticle,
                            brand = brand,
                            quantity = "",
                            existence = existenceText
                        )
                    )
                }

                emit(Resource.Success(data = productList))

                /*
                val priceElements = document.select("div.price")
                "selectDiv: ${document.select("div.price")}".printD
                priceElements.forEach {it.children().printD}
                */

                // textPrice.attr("₽").printD

                /*
                textPrice.forEachIndexed { index, element ->
                    "foreach element: $index $element".printD
                }*/

                /*"getEL1: ${document.getElementsByAttributeValueContaining("price", "₽")}".printD
                "getEL2: ${document.getElementsContainingOwnText("₽")}".printD
                "getEL3: ${document.getElementsMatchingOwnText("₽")}".printD*/
                /*
                val link = document.select("a")
                val linkHref = link.attr("href")
                "link: ${document.select("a")}".printD
                "linkHref: ${link.attr("href")}".printD
                */


            }
        }

}