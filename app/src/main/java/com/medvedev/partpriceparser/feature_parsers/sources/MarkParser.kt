package com.medvedev.partpriceparser.feature_parsers.sources

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.safeTakeFirst
import com.medvedev.partpriceparser.feature_parsers.ProductParser
import com.medvedev.partpriceparser.presentation.models.ProductCart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

class MarkParser : ProductParser() {
    override val linkToSite: String
        get() = "https://klassauto.ru"
    override val siteName: String
        get() = "Марк"

    override val partOfLinkToCatalog: (String) -> String = { article ->
        "/search/?search=$article"
    }

    val Any.printMR
        get() = Timber.tag("developerMR").d(toString())

    lateinit var markCookies: MutableMap<String, String>

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>
        get() = { articleToSearch ->
            flow {
                if (!::markCookies.isInitialized) {

                    val markLogin = "a9173959992@gmail.com"
                    val markPassword = "уке987гр"
                    val authLink = "https://klassauto.ru/cabinet/"

                    val authCookies: Connection.Response = Jsoup.connect(authLink)
                        .data(
                            "AuthPhase", "1",
                            "REQUESTED_FROM", "/",
                            "REQUESTED_BY", "GET",
                            "catalogue", "1",
                            "sub", "7",
                            "cc", "74",
                            "AUTH_USER", markLogin,
                            "AUTH_PW", markPassword,
                            "submit", "Авторизоваться"
                        )
                        .method(Connection.Method.POST)
                        .execute()

                    markCookies = authCookies.cookies()
                }

                val fullLinkToSearch = linkToSite + partOfLinkToCatalog(articleToSearch)

                val document: Document = Jsoup.connect(fullLinkToSearch)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                    .timeout(20 * 1000)
                    .cookies(markCookies)
                    .post()

                val productElements = document
                    .select("div.is-multiline")
                    .select("div.catalog-item__tile-item-content")

                productElements.forEach { element ->

                    val imageUrl = element.select("figure.image").select("img").attr("src")
                        .apply { "imageUrl: $this".printMR }

                    val price = element.select("span.price").textNodes().safeTakeFirst
                        .apply { "price: $this".printMR }

                    val brand =
                        element.select("a.param-vendor-value").textNodes().safeTakeFirst
                            .apply { "brand: $this".printMR }

                    val article =
                        element.select("span.param-article-value").textNodes().safeTakeFirst
                            .apply { "article: $this".printMR }

                    val name =
                        element.select("a.catalog-item__tile-item-title").textNodes().safeTakeFirst
                            .apply { "name: $this".printMR }

                    val halfLinkToProduct =
                        element.select("a.catalog-item__tile-item-title").attr("href")
                            .apply { "linkToProduct: $this".printMR }

                    val quantity =
                        element.select("span.stock-value").textNodes().safeTakeFirst
                            .apply { "quantity: $this".printMR }


                    val existence = element.select("span.stock-title").textNodes().safeTakeFirst
                        .apply { "existence: $this".printMR }


                    productList.add(
                        ProductCart(
                            fullLinkToProduct = linkToSite + halfLinkToProduct,
                            fullImageUrl = linkToSite + imageUrl,
                            price = price,
                            name = name,
                            article = "Артикул: $article",
                            additionalArticles = "",
                            brand = brand,
                            quantity = quantity,
                            existence = existence
                        )
                    )
                }
                emit(Resource.Success(data = productList))
            }
        }
}