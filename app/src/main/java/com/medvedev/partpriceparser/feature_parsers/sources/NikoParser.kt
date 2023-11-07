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

class NikoParser : ProductParser() {
    override val linkToSite: String
        get() = "https://нико.рф"
    override val siteName: String
        get() = "Нико"

    override val partOfLinkToCatalog: (String) -> String = { article ->
        "/search/?nc_ctpl=2052&find=$article"
    }

    val Any.printNK
        get() = Timber.tag("developeNk").d(toString())

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>
        get() = { articleToSearch ->
            flow {

                val document: Document =
                    Jsoup.connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                        .timeout(10 * 1000)
                        .get()


                val productElements = document
                    .select("div.catalog-items-list")
                    .select("div.catalog-item")
                    .select("div.blklist_main")
                    .apply { "productElements: $this".printNK }

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
                            partLinkToProduct =
                                select("div.blk_name").select("a").attr("href").html2text
                            name = select("div.blk_name").select("span").text().html2text
                            article =
                                select("div.blk_art").select("span.art_value").text().html2text
                        }

                    "partLinkToProduct: $partLinkToProduct".printNK
                    "name: $name".printNK
                    "article: $article".printNK

                    var price: String
                    var existence: String

                    element.select("div.blklist_price")
                        .apply {
                            price = select("div.blk_priceblock ").select("div.normal_price")
                                .select("span.cen").text()
                            existence =
                                select("div.blk_stock").select("span").textNodes().safeTakeFirst
                        }

                    "price: $price".printNK
                    "existence: $existence".printNK


                    productList.add(
                        ProductCart(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = name,
                            article = "Артикул: $article",
                            additionalArticles = "",
                            brand = "",
                            quantity = null,
                            existence = existence,
                        )
                    )
                }
                emit(Resource.Success(data = productList))
            }
        }
}