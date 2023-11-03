package com.medvedev.partpriceparser.feature_parsers.sources

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.safeTakeFirst
import com.medvedev.partpriceparser.feature_parsers.ProductParser
import com.medvedev.partpriceparser.presentation.models.ProductCart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

class MidkamParser : ProductParser() {
    override val linkToSite: String
        get() = "https://midkam.ru"
    override val siteName: String
        get() = "Мидкам"

    override val partOfLinkToCatalog: (String) -> String = { article ->
        "/search/?nc_ctpl=2052&find=$article"
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
                    .select("div.blk_items_spisok")
                    .select("div.product-item")
                    .apply { "productElements: $this".printAM }

                productElements.forEach { element ->

                    val name = element
                        .select("div.blk_text")
                        .select("div.blk_bordertext")
                        .select("div.blk_name")
                        .select("a")
                        .select("span")
                        .textNodes().safeTakeFirst
                        .apply { "name: $this".printAM }


                    val imageUrl = element
                        .select("span.image_h")
                        .select("img")
                        .attr("src")
                        .apply { "imageUrl: $this".printAM }


                    val partLinkToProduct = element
                        .select("div.blk_text")
                        .select("div.blk_bordertext")
                        .select("div.blk_name")
                        .select("a")
                        .attr("href")
                        .apply { "halfLinkToProduct: $this".printAM }

                    val price = element
                        .select("div.blk_buyinfo")
                        .select("span.cen")
                        .textNodes().safeTakeFirst
                        .let {
                            if (it.isNotEmpty()) "$it ₽" else it
                        }
                        .apply { "price: $this".printAM }

                    val existence = element
                        .select("span.c_nalich")
                        .textNodes().safeTakeFirst
                        .apply { "existence: $this".printAM }

                    val fullLinkToProduct = linkToSite + partLinkToProduct
                        .apply { "fullLinkToProduct: $this".printAM }

                    val innerDocument: Document =
                        Jsoup.connect(fullLinkToProduct)
                            .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                            .timeout(10 * 1000)
                            .get()

                    val article = innerDocument
                        .select("div.c_article").let {
                            (it.select("span.c_art_1").textNodes().safeTakeFirst) + " " +
                                    (it.select("span.art_num").textNodes().safeTakeFirst)
                        }
                        .apply { "article: $this".printAM }


                    productList.add(
                        ProductCart(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = name,
                            alternativeName = "",
                            article = article,
                            additionalArticles = "",
                            brand = "",
                            quantity = "",
                            existence = existence,
                        )
                    )
                }
                emit(Resource.Success(data = productList))
            }
        }
}