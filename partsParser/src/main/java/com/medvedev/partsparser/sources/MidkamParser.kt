package com.medvedev.partsparser.sources

import com.medvedev.partsparser.models.ProductBrand
import com.medvedev.partsparser.models.ReceivedProductData
import com.medvedev.partsparser.models.getCleanPrice
import com.medvedev.partsparser.models.getExistence
import com.medvedev.partsparser.utils.Resource
import com.medvedev.partsparser.utils.safeTakeFirst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

class MidkamParser : ProductParser() {

    val kamazMrfName = "ПАО КамАЗ"
    val repairText = "ремонт с гарантией"

    override val linkToSite: String
        get() = "https://midkam.ru"
    override val siteName: String
        get() = "Мидкам"

    override val partOfLinkToCatalog: (String) -> String = { article ->
        "/search/?nc_ctpl=2052&find=$article"
    }
    val Any.printMK
        get() = Timber.tag("developerMK").d(toString())

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<Resource<Set<ReceivedProductData>>>
        get() = { articleToSearch ->

            val productSet: MutableSet<ReceivedProductData> = mutableSetOf()

            flow {

                val fullLink = linkToSite + partOfLinkToCatalog(articleToSearch)

                "fullLink: $fullLink".printMK

                val document: Document =
                    Jsoup.connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                        .timeout(30 * 1000)
                        .get()


                val productElements = document
                    .select("div.blk_items_spisok")
                        .select("div.product-item")

                productElements.forEach { element ->

                    val name = element
                        .select("div.blk_text")
                        .select("div.blk_bordertext")
                        .select("div.blk_name")
                        .select("a")
                        .select("span")
                        .textNodes().safeTakeFirst
                        .apply { "name: $this".printMK }


                    val imageUrl = element
                        .select("span.image_h")
                        .select("img")
                        .attr("src")
                        .apply { "imageUrl: $this".printMK }


                    val partLinkToProduct = element
                        .select("div.blk_text")
                        .select("div.blk_bordertext")
                        .select("div.blk_name")
                        .select("a")
                        .attr("href")
                        .apply { "halfLinkToProduct: $this".printMK }

                    val price = element
                        .select("div.blk_buyinfo")
                        .select("span.cen")
                        .textNodes().safeTakeFirst
                        .getCleanPrice
                        .apply { "price: $this".printMK }

                    val existence = element
                        .select("span.c_nalich")
                        .textNodes().safeTakeFirst
                        .apply { "existence: $this".printMK }

                    val fullLinkToProduct = linkToSite + partLinkToProduct
                        .apply { "fullLinkToProduct: $this".printMK }

                    val innerDocument: Document =
                        Jsoup.connect(fullLinkToProduct)
                            .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                            .timeout(20 * 1000)
                            .get()

                    val article = innerDocument
                        .select("div.c_article")
                        .select("span.art_num")
                        .textNodes().safeTakeFirst
                        .apply { "article: $this".printMK }

                    val brand = if (name.contains(kamazMrfName)) {
                        ProductBrand.Kamaz()
                    } else if (name.contains(repairText)) {
                        ProductBrand.Repair()
                    } else ProductBrand.Unknown()


                    productSet.add(
                        ReceivedProductData(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = name,
                            article = article,
                            additionalArticles = "",
                            brand = brand,
                            quantity = "",
                            existence = existence.getExistence
                        )
                    )
                }
                emit(Resource.Success(data = productSet))
            }
        }
}