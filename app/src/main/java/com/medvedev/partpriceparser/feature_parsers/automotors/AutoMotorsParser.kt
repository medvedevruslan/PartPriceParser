package com.medvedev.partpriceparser.feature_parsers.automotors

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.html2text
import com.medvedev.partpriceparser.core.util.printD
import com.medvedev.partpriceparser.feature_parsers.ProductParser
import com.medvedev.partpriceparser.presentation.models.ProductCart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.Locale

class AutoMotorsParser() : ProductParser {

    override val linkToSite: String = "https://auto-motors.ru"
    override val siteName: String = "АВТОМОТОРС"
    override val partOfLinkToCatalog: String = "/catalog/?q="

    override suspend fun getProduct(articleToSearch: String): Flow<Resource<List<ProductCart>>> {

        return flow {
            try {
                emit(Resource.Loading())

                val document: Document =
                    Jsoup.connect("$linkToSite$partOfLinkToCatalog$articleToSearch") // 740.1003010-20 пример
                        .timeout(10 * 1000).get()

                // parseText = document.text()
                /*"tagName: ${document.tagName()}".printD
                "head: ${document.head()}".printD
                "tag: ${document.tag()}".printD
                "tagNameDiv: ${document.tagName("div")}".printD
                "body: ${document.body()}".printD*/

                val productList: MutableList<ProductCart> = mutableListOf()
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
                    val article = articleInfo[0].text().html2text.apply { "article: $this".printD }

                    // val dopArticle = articleInfo[1].text().html2text.apply { "dopArticle: $this".printD }
                    val dopArticle =
                        if (articleInfo.size > 3) articleInfo[1].text().html2text else ""
                    "dopArticle: $dopArticle".printD

                    val existenceText = if (articleInfo.size > 3) {
                        "article info size ${articleInfo.size}".printD
                        (articleInfo[2].text().html2text.lowercase(Locale.getDefault()) + " " + articleInfo[3].text().html2text).apply { "existenceText1: $this".printD }
                    } else {
                        (articleInfo[1].text().html2text.lowercase(Locale.getDefault()) + " " + articleInfo[2].text().html2text).apply { "existenceText2: $this".printD }
                    }


                    val infoProductElements = element.getElementsByAttribute("alt")
                    val alternativeName: String =
                        infoProductElements.attr("alt").apply { "alternativeName: $this".printD }
                    val imgSrc = infoProductElements.attr("src").apply { "imgSrc: $this".printD }
                    val name = infoProductElements.attr("title").apply { "name: $this".printD }

                    val halfLinkToProduct =
                        element.select("a.link-fast-view")
                            .attr("data-url").html2text.apply { "halfLink: $this".printD }
                    val brand = element.select("p.brand_name")
                        .text().html2text.apply { "brand: $this".printD }

                    val price: String? =
                        element.select("div.price").first()
                            ?.html()?.html2text.apply { "price: $this".printD }

                    productList.add(
                        ProductCart(
                            linkToProduct = linkToSite + halfLinkToProduct,
                            imageUrl = linkToSite + imgSrc,
                            price = if (!price.isNullOrEmpty()) price else "Неизвестно",
                            name = name,
                            alternativeName = alternativeName,
                            article = article,
                            additionalArticles = dopArticle,
                            brand = brand,
                            quantity = 0,
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

            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(e.toString()))
            }
        }
    }
}