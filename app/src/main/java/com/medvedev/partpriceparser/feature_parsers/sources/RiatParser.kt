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

class RiatParser : ProductParser() {


    override val linkToSite: String = "https://tdriat.ru"
    override val siteName: String = "ООО «ТД «РИАТ-Запчасть»"
    override val partOfLinkToCatalog: (String) -> String = { article ->
        "/poisk/$article"
    }

    val Any.printRT
        get() = Timber.tag("developer TFK").d(toString())

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>
        get() = { articleToSearch ->
            flow {

                val cookieResponse: Connection.Response =
                    Jsoup.connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}")
                        .data("username", "myUsername", "password", "myPassword")
                        .method(Connection.Method.GET)
                        .execute()

                val cookies = cookieResponse.cookies()

                val document: Document =
                    Jsoup.connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                        .referrer("$linkToSite${partOfLinkToCatalog(articleToSearch)}")
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

                    productInfo[0].select("b").map { productElement ->
                        productElement.text().html2text.removeSuffix(":").also { textElement ->
                            if (textElement.contains("Производитель")) {

                                brand = productInfo[0].getElementsMatchingText("Производитель")
                                    .textNodes()[1].toString().trim()
                                    .apply { "data contains brand: $this".printRT }
                            }
                        }
                    }

                    productList.add(
                        ProductCart(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = name,
                            alternativeName = "",
                            article = "Артикул: $article",
                            additionalArticles = "",
                            brand = brand,
                            quantity = null,
                            existence = existence,
                        )
                    )
                    emit(Resource.Success(data = productList))
                }
            }
        }
}