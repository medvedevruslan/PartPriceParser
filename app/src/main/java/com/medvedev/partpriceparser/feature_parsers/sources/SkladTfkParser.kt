package com.medvedev.partpriceparser.feature_parsers.sources

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.html2text
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
    override val siteName: String = "STFK KAMAZ"
    override val partOfLinkToCatalog: (String) -> String
        get() = { article ->
            "/catalog/search/?text=$article&s="
        }

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>
        get() = { articleToSearch ->
            flow {

                val nameSeparator = "(см."

                val actualDocument = documentCatalogAddressLink(articleToSearch)

                val productElements = actualDocument.select("td.table-body-image")

                productElements.forEach { element ->
                    val partLinkToProduct =
                        element.select("a").attr("href").apply { "linkToProduct: $this".printTFK }
                    val name: String =
                        element.select("img").attr("alt").apply { "name: $this".printTFK }


                    var additionalArticles = "-"

                    val readyName = if (name.contains("(см.")) {

                        val nameList = ArrayList<String>()
                        nameList.addAll(name.split(nameSeparator))
                        additionalArticles = "Доп.артикул: ${nameList[1].trim().removeSuffix(")")}"

                        nameList.first()
                    } else {
                        name
                    }


                    val imageUrl =
                        element.select("img").attr("src").apply { "imageUrl: $this".printTFK }

                    val innerDocument: Document =
                        Jsoup.connect("$linkToSite$partLinkToProduct").timeout(10 * 1000).get()

                    val productInfo = innerDocument.select("span.tovarcard-top-prop")

                    val article =
                        productInfo[0].child(1).text().html2text.apply { "article: $this".printTFK }
                    val brand =
                        productInfo[1].child(1).text().html2text.apply { "brand: $this".printTFK }


                    // val spanArticle = productInfo.select("span.tovarcard-top-prop").apply { "spanArticle: $this".printTFK }
                    // val article = productInfo.text().html2text.apply { "article: $this".printTFK }
                    /*productInfo[0].getElementsMatchingOwnText("Артикул:").apply { "getE1: $this".printTFK }
                    productInfo[0].getElementsByAttributeStarting("Артикул:").apply { "getE2: $this".printTFK }
                    productInfo[0].getElementsContainingOwnText("Артикул:").apply { "getE3: $this".printTFK }
                    productInfo[0].getElementsByAttributeValueContaining("span","Артикул:").apply { "getE4: $this".printTFK }
                    productInfo[0].getElementsByAttribute("Артикул:").apply { "getE5: $this".printTFK }*/

                    /*productInfo[0].allElements.forEach {
                        "productInfoForEach: $it".printTFK
                    }*/

                    /* val article1 = productInfo.attr("div.tovarcard-top-props span span").apply { "article1: $this".printTFK }
                     val article3 = productInfo.attr("div.tovarcard-top-props span span").apply { "article3: $this".printTFK }
                     // val article2 = productInfo.attr("Артикул:").apply { "article2: $this".printTFK }
                     val brand = productInfo.attr("Производитель::").apply { "brand: $this".printTFK }*/


                    val innerProductElements = innerDocument.select("div.tbody")

                    var price: String? = "0"
                    var existence: String? = ""

                    innerProductElements.forEach { innerElement ->

                        price = innerElement.select("div.td_cena").first()?.text()?.html2text
                            .apply { "price: $this".printTFK }

                        existence =
                            innerElement.select("div.td_quantity").first()?.text()?.html2text
                                .apply { "existenceText: $this".printTFK }
                    }

                    productList.add(
                        ProductCart(
                            fullLinkToProduct = linkToSite + partLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = readyName,
                            alternativeName = "",
                            article = "Артикул: $article",
                            additionalArticles = additionalArticles,
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