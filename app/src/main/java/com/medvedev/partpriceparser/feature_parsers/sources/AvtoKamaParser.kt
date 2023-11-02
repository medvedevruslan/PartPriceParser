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

class AvtoKamaParser : ProductParser() {
    override val linkToSite: String
        get() = "https://avtokama.ru"
    override val siteName: String
        get() = "АВТОКАМА"

    override val partOfLinkToCatalog: (String) -> String = { article ->
        "/search?searched=$article"
    }


    private val Any.printAM
        get() = Timber.tag("developerAM").d(toString())

    override val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>
        get() = { articleToSearch ->
            flow {


                val fullLink = linkToSite + partOfLinkToCatalog(articleToSearch)

                "fullLink: $fullLink".printAM

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
                        .let {
                            val articleName = it.select("b").first()?.text()?.html2text
                            val articleText = it.textNodes().safeTakeFirst
                            "$articleText $articleName"
                        }
                        .apply { "article: $this".printAM }


                    val existence = element
                        .select("div.items-center")
                        .select("div.flex")
                        .select("div.font-bold")
                        .textNodes().safeTakeFirst
                        .apply { "existence: $this".printAM }


                    val imageUrl = element
                        .select("a")
                        .select("img")
                        .attr("src")
                        .apply { "imageUrl: $this".printAM }


                    val name = element
                        .select("div.info")
                        .select("a")
                        .textNodes().safeTakeFirst
                        .apply { "name: $this".printAM }


                    val partLinkToProduct = element
                        .select("div.info")
                        .select("a")
                        .attr("href")
                        .apply { "halfLinkToProduct: $this".printAM }


                    val brand = element
                        .select("div.codes")
                        .select("div.small-text")
                        .select("a")
                        .select("b")
                        .text().html2text
                        .apply { "brand: $this".printAM }

                    val price = element
                        .select("div.justify-between")
                        .select("div.price-line")
                        .select("div.price")
                        .textNodes().safeTakeFirst
                        .let {
                            if (it.isNotEmpty()) "$it ₽" else it
                        }
                        .apply { "price: $this".printAM }


                    productList.add(
                        ProductCart(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = name,
                            alternativeName = "",
                            article = article,
                            additionalArticles = "",
                            brand = brand,
                            quantity = null,
                            existence = existence,
                        )
                    )
                }
                emit(Resource.Success(data = productList))
            }
        }
}