package com.medvedev.partpriceparser.feature_parsers.sources

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.html2text
import com.medvedev.partpriceparser.core.util.safeTakeFirst
import com.medvedev.partpriceparser.feature_parsers.ProductParser
import com.medvedev.partpriceparser.presentation.models.ProductCart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

class KamaCenterParser : ProductParser() {
    override val linkToSite: String
        get() = "https://kamacenter.ru"
    override val siteName: String
        get() = "КамаЦентр"

    override val partOfLinkToCatalog: (String) -> String = { article ->
        "/search/?searchword=$article"
    }

    val Any.printKC
        get() = Timber.tag("developerKC").d(toString())

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>
        get() = { articleToSearch ->
            flow {
                val fullLink = linkToSite + partOfLinkToCatalog(articleToSearch)

                "fullLink: $fullLink".printKC

                val document: Document =
                    Jsoup.connect(fullLink) // 740.1003010-20 пример
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                        .timeout(30 * 1000)
                        .post()

                val productElements = document
                    .select("div.content")
                    .select("div.products-list")
                    .select("div.products-list__item")

                productElements.forEach { element ->

                    val imageUrl = element
                        .select("div.products-image")
                        .select("img")
                        .attr("src")
                        .apply { "imageUrl: $this".printKC }

                    val partLinkToProduct = element
                        .select("a.products-name__name")
                        .attr("href")
                        .apply { "linkToProduct: $this".printKC }

                    val name = element
                        .select("a.products-name__name")
                        .textNodes().safeTakeFirst
                        .apply { "name: $this".printKC }

                    val price = element
                        .select("span.products-priceinfo__price")
                        .textNodes().safeTakeFirst
                        .let {
                            if (it.isNotEmpty()) "$it ₽" else ""
                        }
                        .apply { "price: $this".printKC }


                    val articleHtml = element
                        .select("table.products-table")
                        .select("td")

                    var textArticle = ""

                    if (articleHtml[0].text().html2text == "Артикул") {
                        textArticle = articleHtml[1].text().html2text
                        "textArticle: $textArticle".printKC
                    }

                    element.select("a.products__getmore")
                        .textNodes().safeTakeFirst.apply { "existenceOriginal: $this".printKC }


                    val existence = element
                        .select("a.products__getmore")
                        .textNodes().safeTakeFirst
                        .let {
                            if (it == "Уведомить о наличии") "нет в наличии" else it
                        }
                        .apply { "existence: $this".printKC }

                    val innerDocument = Jsoup
                        .connect(linkToSite + partLinkToProduct)
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                        .timeout(20 * 1000)
                        .post()

                    val productInfo = innerDocument.select("div.good-stocks__first")
                        .select("div.good-priceinfo-stocks")

                    val quantity = productInfo
                        .select("span.good-priceinfo-stocks__item")
                        .select("b")
                        .textNodes().safeTakeFirst
                        .apply { "quantity: $this".printKC }


                    productList.add(
                        ProductCart(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = name,
                            article = "Артикул: $textArticle",
                            additionalArticles = "",
                            brand = "",
                            quantity = quantity,
                            existence = existence,
                        )
                    )
                }
                emit(Resource.Success(data = productList))
            }
        }
}