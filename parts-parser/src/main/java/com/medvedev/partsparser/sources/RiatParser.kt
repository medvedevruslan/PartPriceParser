package com.medvedev.partsparser.sources

import com.medvedev.partsparser.models.ProductCartParse
import com.medvedev.partsparser.models.getBrand
import com.medvedev.partsparser.models.getExistence
import com.medvedev.partsparser.utils.ResourceParse
import com.medvedev.partsparser.utils.html2text
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.TextNode
import timber.log.Timber

internal class RiatParser : ProductParser() {

    override val linkToSite: String = "https://tdriat.ru"
    override val siteName: String = "РИАТ"
    override val partOfLinkToCatalog: (String) -> String = { article ->
        "/poisk/$article"
    }

    val Any.printRT
        get() = Timber.tag("developerRT").d(toString())

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<ResourceParse<Set<ProductCartParse>>>
        get() = { articleToSearch ->
            val productSet: MutableSet<ProductCartParse> = mutableSetOf()
            flow {

                val fullLink = linkToSite + partOfLinkToCatalog(articleToSearch)
                "fullLink: $fullLink".printRT

                // cookies are required to load the current price
                val cookieResponse: Connection.Response =
                    Jsoup.connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}")
                        .data("username", "myUsername", "password", "myPassword")
                        .method(Connection.Method.GET)
                        .timeout(40 * 1000)
                        .execute()

                val cookies = cookieResponse.cookies()

                val document: Document =
                    Jsoup.connect(fullLink) // 740.1003010-20 пример
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                        .timeout(30 * 1000)
                        .cookies(cookies)
                        .post()

                val productElements = document
                    .select("table.table-catalog")
                    .select("tr.cart_table_item")

                productElements.forEach { element ->

                    val partLinkToProduct = element.select("a").attr("href")
                        .apply { "partLinkToProduct: $this".printRT }

                    val imageUrl: String? = element.select("img.img-responsive").attr("src")
                        .apply { "imageUrl: $this".printRT }

                    val name: String = element
                        .select("td.product-name")
                        .select("a")
                        .textNodes()
                        .first()
                        .text().html2text
                        .apply { "name: $this".printRT }

                    val unnecessary = element
                        .select("div.spoiler_links3")

                    var article = ""
                    var brand = ""


                    element
                        .select("td.product-name")
                        .select("div")
                        .let {
                            val elements = it
                            if (unnecessary.size > 0) {
                                unnecessary.forEach { elementToRemove ->
                                    elements.remove(elementToRemove)
                                }
                            }
                            elements
                        }
                        .textNodes()
                        .let {
                            val elements = it
                            val iterator: MutableIterator<TextNode> = elements.iterator()
                            while (iterator.hasNext()) {
                                val value = iterator.next()
                                if (value.isBlank || value.text() == ", ") {
                                    iterator.remove()
                                }
                            }
                            elements
                        }.also { foundElements ->
                            if (foundElements.size > 0) {
                                article = foundElements[0].text().apply { "article: $this".printRT }
                            }
                            if (foundElements.size > 1) {
                                brand = foundElements[1].text().apply { "brand: $this".printRT }
                            }
                        }

                    val price = element
                        .select("td.product-price")
                        .select("span.amount")
                        .select("b")
                        .text().html2text
                        .replace(" ", "")
                        .replace(" ", "")
                        .toFloatOrNull()
                        .apply { "price: $this".printRT }

                    val existence: String = element
                        .select("td.product-price")
                        .select("span.amount")
                        .select("small")
                        .text().html2text
                        .apply { "existence: $this".printRT }

                    /*val innerDocument = Jsoup
                        .connect("$linkToSite$partLinkToProduct")
                        .timeout(20 * 1000)
                        .get()


                    val productInfo = innerDocument.select("p.taller")

                    var brand = ""

                    productInfo[0].select("b").map { productElement ->
                        productElement.text().html2text.removeSuffix(":").also { textElement ->
                            if (textElement.contains("Производитель")) {

                                brand = productInfo[0].getElementsMatchingText("Производитель")
                                    .textNodes()[1].toString().trim()
                                    .apply { "data contains brand: $this".printRT }
                            }
                        }
                    }*/

                    productSet.add(
                        ProductCartParse(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = name,
                            article = article,
                            additionalArticles = "",
                            brand = brand.getBrand,
                            quantity = null,
                            existence = existence.getExistence
                        )
                    )
                }
                emit(ResourceParse.Success(data = productSet))
            }
        }
}