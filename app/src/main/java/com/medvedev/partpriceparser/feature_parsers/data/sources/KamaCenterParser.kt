package com.medvedev.partpriceparser.feature_parsers.data.sources

import com.medvedev.partpriceparser.brands.getBrand
import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.html2text
import com.medvedev.partpriceparser.core.util.safeTakeFirst
import com.medvedev.partpriceparser.feature_parsers.data.ProductParser
import com.medvedev.partpriceparser.feature_parsers.presentation.models.ProductCart
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
    override inline val workWithServer: (String) -> Flow<Resource<Set<ProductCart>>>
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
                        .toFloatOrNull()
                        .apply { "price: $this".printKC }

                    var article = ""
                    var brand = ""


                    element
                        .select("table.products-table")
                        .select("tr")
                        .select("td")
                        .apply {
                            for (i in 0..this.size - 2) {
                                if (this[i].text().html2text.contains("Артикул")) {
                                    article = this[i + 1].text().html2text
                                    "article: $article".printKC
                                } else if (this[i].text().html2text.contains("Производитель")) {
                                    brand = this[i + 1].text().html2text
                                    "brand: $brand".printKC
                                }
                            }
                        }

                    val existence = element
                        .select("a.products__getmore")
                        .textNodes().safeTakeFirst
                        .let {
                            if (it == "Уведомить о наличии") "нет в наличии" else "В наличии"
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

                    productSet.add(
                        ProductCart(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = name,
                            article = article,
                            additionalArticles = "",
                            brand = brand.getBrand,
                            quantity = quantity,
                            existence = existence
                        )
                    )
                }
                emit(Resource.Success(data = productSet))
            }
        }
}