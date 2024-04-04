package com.medvedev.partsparser.sources

import com.medvedev.partsparser.models.ProductCartParse
import com.medvedev.partsparser.models.getBrand
import com.medvedev.partsparser.models.getCleanPrice
import com.medvedev.partsparser.models.getExistence
import com.medvedev.partsparser.utils.ResourceParse
import com.medvedev.partsparser.utils.html2text
import com.medvedev.partsparser.utils.safeTakeFirst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

internal class AvtoKamaParser : ProductParser() {
    override val linkToSite: String
        get() = "https://avtokama.ru"
    override val siteName: String
        get() = "АВТОКАМА"

    override val partOfLinkToCatalog: (String) -> String = { article ->
        "/search?searched=$article"
    }

    val Any.printAK
        get() = Timber.tag("developerAK").d(toString())

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<ResourceParse<Set<ProductCartParse>>>
        get() = { articleToSearch ->
            val productSet: MutableSet<ProductCartParse> = mutableSetOf()
            flow {


                val fullLink = linkToSite + partOfLinkToCatalog(articleToSearch)

                "fullLink: $fullLink".printAK

                val document: Document =
                    Jsoup.connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                        .timeout(10 * 1000)
                        .get()


                val productElements = document
                    .select("div.teaser")

                productElements.forEach { element ->

                    val article = element
                        .select("div.info")
                        .select("div.codes")
                        .select("div.small-text")
                        .select("b")
                        .first()?.text()?.html2text
                        .apply { "article: $this".printAK }


                    val existence = element
                        .select("div.items-center")
                        .select("div.flex")
                        .select("div.font-bold")
                        .textNodes().safeTakeFirst
                        .apply { "existence: $this".printAK }


                    val imageUrl = element
                        .select("a")
                        .select("img")
                        .attr("src")
                        .apply { "imageUrl: $this".printAK }


                    val name = element
                        .select("div.info")
                        .select("a")
                        .textNodes().safeTakeFirst
                        .apply { "name: $this".printAK }


                    val partLinkToProduct = element
                        .select("div.info")
                        .select("a")
                        .attr("href")
                        .apply { "halfLinkToProduct: $this".printAK }


                    val brand = element
                        .select("div.codes")
                        .select("div.small-text")
                        .select("a")
                        .select("b")
                        .text().html2text
                        .apply { "brand: $this".printAK }


                    var price: Float? = null

                    element
                        .select("div.justify-between")
                        .select("div.price-line")
                        .select("div.price")
                        .textNodes().forEach {
                            val text = it.text()
                            if (!text.isNullOrEmpty() && text != " ") {
                                price = text.getCleanPrice
                            }
                        }
                        .apply { "price: $price".printAK }


                    productSet.add(
                        ProductCartParse(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = name,
                            article = article ?: "articleError",
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