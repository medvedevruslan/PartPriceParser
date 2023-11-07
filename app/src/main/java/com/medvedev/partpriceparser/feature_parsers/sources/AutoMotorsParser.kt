package com.medvedev.partpriceparser.feature_parsers.sources

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.html2text
import com.medvedev.partpriceparser.core.util.safeTakeFirst
import com.medvedev.partpriceparser.feature_parsers.ProductParser
import com.medvedev.partpriceparser.presentation.models.ProductCart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber


class AutoMotorsParser : ProductParser() {

    override val linkToSite: String = "https://auto-motors.ru"
    override val siteName: String = "АВТОМОТОРС"
    override val partOfLinkToCatalog: (String) -> String
        get() = { article ->
            "/catalog/?q=$article"
        }

    val Any.printAU
        get() = Timber.tag("developerAU").d(toString())

    lateinit var autoMotorsCookies: MutableMap<String, String>

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>
        get() = { articleToSearch ->
            flow {

                if (!::autoMotorsCookies.isInitialized) {
                    val authLink = "https://auto-motors.ru/AM_autorize_AUT/"
                    val authCookies: Connection.Response =
                        Jsoup.connect(authLink)
                            .data(
                                "USER_LOGIN", "info@dvizh-dvizh.ru",
                                "USER_PASSWORD", "info@dvizh-dvizh.ru",
                                "USER_REMEMBER", "Y",
                                "AUTH_ACTION", "Войти"
                            )
                            .method(Connection.Method.POST)
                            .execute()

                    autoMotorsCookies = authCookies.cookies()
                }

                val document: Document =
                    Jsoup.connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                        .timeout(10 * 1000)
                        .cookies(autoMotorsCookies)
                        .post()

                val productElements = document.select("div.product-card-list")

                productElements.forEach { element ->

                    val article = element
                        .select("div.art-list")
                        .select("p.m_none")
                        .textNodes().safeTakeFirst
                        .apply { "article: $this".printAU }

                    val dopArticle = element.select("p.hidden").textNodes().safeTakeFirst
                        .apply { "dopArticle: $this".printAU }

                    val infoProductElements = element.getElementsByAttribute("alt")

                    val imgSrc = infoProductElements.attr("src")
                        .apply { "imgSrc: $this".printAU }

                    val name = infoProductElements.attr("title")
                        .apply { "name: $this".printAU }

                    val halfLinkToProduct = element
                        .select("a.link-fast-view")
                        .attr("data-url").html2text
                        .apply { "halfLink: $this".printAU }

                    val brand = element.select("p.brand_name").text().html2text
                        .apply { "brand: $this".printAU }

                    val price: String? = element.select("div.price").first()?.html()?.html2text
                        .apply { "price: $this".printAU }

                    val quantity = element
                        .select("div.m_right20")
                        .select("p.m_none")
                        .select("b")
                        .text().html2text
                        .let { quantity ->
                            when (quantity) {
                                "ПОД" -> {
                                    "под заказ."
                                }

                                "50" -> {
                                    "болеe 50 шт."
                                }

                                else -> {
                                    "$quantity шт."
                                }
                            }
                        }
                        .apply { "quantity: $this".printAU }



                    productList.add(
                        ProductCart(
                            fullLinkToProduct = linkToSite + halfLinkToProduct,
                            fullImageUrl = linkToSite + imgSrc,
                            price = price,
                            name = name,
                            article = article,
                            additionalArticles = dopArticle,
                            brand = brand,
                            quantity = quantity,
                            existence = ""
                        )
                    )
                }
                emit(Resource.Success(data = productList))
            }
        }
}