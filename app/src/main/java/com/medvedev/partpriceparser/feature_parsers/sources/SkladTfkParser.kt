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


class SkladTfkParser : ProductParser() {

    // todo магазин имеет несколько складов с разынм наличием и разными ценами, взят самый основной склад,
    //  где больше всех наличие и самая актуально-низкая цена

    val Any.printTFK
        get() = Timber.tag("developerTFK").d(toString())

    override val linkToSite: String = "https://skladtfk.ru"
    override val siteName: String = "СТФК"
    override val partOfLinkToCatalog: (String) -> String
        get() = { article ->
            "/catalog/search/?text=$article&s="
        }

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>
        get() = { articleToSearch ->
            flow {

                val fullLink = linkToSite + partOfLinkToCatalog(articleToSearch)

                "fullLink: $fullLink".printTFK

                val nameSeparator = "(см."

                val actualDocument = Jsoup
                    .connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                    .timeout(10 * 1000)
                    .get()

                val productElements = actualDocument.select("td.table-body-image")
                // .apply { "productElements: $this".printTFK }

                productElements.forEach { element ->

                    val partLinkToProduct: String = element
                        .select("a")
                        .attr("href")
                        .apply { "linkToProduct: $this".printTFK }

                    val name: String = element
                        .select("img")
                        .attr("alt")
                        .apply { "name: $this".printTFK }

                    var additionalArticles: String? = ""

                    val readyName = if (name.contains("(см.")) {
                        val nameList = ArrayList<String>()
                        nameList.addAll(name.split(nameSeparator))
                        additionalArticles = "Доп.артикул: ${nameList[1].trim().removeSuffix(")")}"

                        nameList.first().trim()
                            .apply { "changedName: $this".printTFK }
                    } else {
                        name
                    }

                    val imageUrl = element
                        .select("img")
                        .attr("src")
                        .apply { "imageUrl: $this".printTFK }

                    val innerDocument: Document = Jsoup
                        .connect("$linkToSite$partLinkToProduct")
                        .timeout(10 * 1000).get()

                    var article: String
                    var brand: String

                    innerDocument
                        .select("span.tovarcard-top-prop")
                        .also { articleAndBrand ->
                            article = articleAndBrand[0]
                                .child(1)
                                .text().html2text
                                .apply { "article: $this".printTFK }

                            brand = articleAndBrand[1]
                                .child(1)
                                .text().html2text
                                .apply { "brand: $this".printTFK }
                        }

                    var price: Float?
                    var existence: String?

                    innerDocument
                        .select("div.tbody")
                        .select("div.tr")
                        .first()
                        .also { firstElement ->
                            price = firstElement
                                ?.select("div.td_cena")
                                ?.textNodes()
                                ?.safeTakeFirst
                                ?.removeSuffix("руб.")
                                ?.replace(" ", "")
                                ?.toFloatOrNull()
                                .apply { "price: $this".printTFK }

                            existence = firstElement
                                ?.select("div.td_quantity")
                                ?.select("span.for-order")
                                ?.textNodes()
                                ?.safeTakeFirst
                                .apply { "existenceText: $this".printTFK }
                        }

                    productList.add(
                        ProductCart(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = readyName,
                            article = article,
                            additionalArticles = additionalArticles,
                            brand = brand,
                            quantity = null,
                            existence = existence
                        )
                    )
                }
                emit(Resource.Success(data = productList))
            }
        }
}