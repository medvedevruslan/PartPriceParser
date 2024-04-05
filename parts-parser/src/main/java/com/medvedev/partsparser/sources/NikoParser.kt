package com.medvedev.partsparser.sources

import com.medvedev.partsparser.models.ProductBrandParse
import com.medvedev.partsparser.models.ProductCartParse
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

internal class NikoParser : ProductParser() {
    override val linkToSite: String
        get() = "https://нико.рф"
    override val siteName: String
        get() = "Нико"

    override val partOfLinkToCatalog: (String) -> String = { article ->
        "/search/?nc_ctpl=2052&find=$article"
    }

    val Any.printNK
        get() = Timber.tag("developerNk").d(toString())

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<ResourceParse<Set<ProductCartParse>>>
        get() = { articleToSearch ->
            val productSet: MutableSet<ProductCartParse> = mutableSetOf()
            flow {

                val fullLink = linkToSite + partOfLinkToCatalog(articleToSearch)
                "fullLink: $fullLink".printNK

                val document: Document = Jsoup.connect(fullLink)
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                        .timeout(30 * 1000)
                        .get()


                val productElements = document
                    .select("div.catalog-items-list")
                    .select("div.catalog-item")
                    .select("div.blklist_main")

                productElements.forEach { element ->

                    val imageUrl = element
                        .select("div.blklist_photo")
                        .select("div.image-default")
                        .select("img")
                        .attr("data-src")
                        .apply { "imageUrl: $this".printNK }

                    var name: String
                    var article: String
                    var partLinkToProduct: String

                    element
                        .select("div.blklist_info")
                        .select("div.blk_listfirst")
                        .apply {
                            partLinkToProduct = select("div.blk_name")
                                .select("a")
                                .attr("href").html2text

                            name = select("div.blk_name")
                                .select("span")
                                .text().html2text

                            article = select("div.blk_art")
                                .select("span.art_value")
                                .text().html2text
                                .removeSuffix("..")
                                .removeSuffix(".")
                        }

                    "partLinkToProduct: $partLinkToProduct".printNK
                    "name: $name".printNK
                    "article: $article".printNK

                    var price: Float?
                    var existence: String

                    element.select("div.blklist_price")
                        .apply {
                            price = select("div.blk_priceblock")
                                .select("div.normal_price")
                                .select("span.cen").text()
                                .getCleanPrice
                                .let {
                                    if (it == 0f) null else it
                                }

                            existence = select("div.blk_stock")
                                .select("span")
                                .textNodes().safeTakeFirst
                        }

                    "price: $price".printNK
                    "existence: $existence".printNK


                    productSet.add(
                        ProductCartParse(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = name,
                            article = article,
                            additionalArticles = "",
                            brand = ProductBrandParse.Unknown(),
                            quantity = null,
                            existence = existence.getExistence
                        )
                    )
                }
                emit(ResourceParse.Success(data = productSet))
            }
        }
}