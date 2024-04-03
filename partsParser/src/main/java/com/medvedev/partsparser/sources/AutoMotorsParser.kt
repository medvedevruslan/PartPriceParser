package com.medvedev.partsparser.sources

import com.medvedev.partsparser.models.ProductCartDTO
import com.medvedev.partsparser.models.PartExistenceDTO
import com.medvedev.partsparser.models.getBrand
import com.medvedev.partsparser.models.getCleanPrice
import com.medvedev.partsparser.utils.ResourceDTO
import com.medvedev.partsparser.utils.html2text
import com.medvedev.partsparser.utils.safeTakeFirst
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
    override inline val workWithServer: (String) -> Flow<ResourceDTO<Set<ProductCartDTO>>>
        get() = { articleToSearch ->
            flow {
                val productSet: MutableSet<ProductCartDTO> = mutableSetOf()

                val fullLink = linkToSite + partOfLinkToCatalog(articleToSearch)

                "fullLink: $fullLink".printAU

                if (!::autoMotorsCookies.isInitialized) {
                    val authLink = "https://auto-motors.ru/auth/"
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

                val document: Document = Jsoup
                    .connect("$linkToSite${partOfLinkToCatalog(articleToSearch)}") // 740.1003010-20 пример
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
                        .removePrefix("Артикул: ")
                        .apply { "article: $this".printAU }

                    val dopArticle = element
                        .select("p.hidden")
                        .textNodes().safeTakeFirst
                        .apply { "dopArticle: $this".printAU }

                    val infoProductElements = element.getElementsByAttribute("alt")

                    val imgSrc = infoProductElements
                        .attr("src")
                        .apply { "imgSrc: $this".printAU }

                    val name = infoProductElements
                        .attr("title")
                        .apply { "name: $this".printAU }

                    val halfLinkToProduct = element
                        .select("a.link-fast-view")
                        .attr("data-url").html2text
                        .apply { "halfLink: $this".printAU }

                    val brand = element
                        .select("p.brand_name")
                        .text().html2text
                        .apply { "brand: $this".printAU }

                    val price: Float? = element
                        .select("div.price")
                        .select("div")
                        .first()?.text()
                        ?.removeSuffix("₽")
                        ?.getCleanPrice
                        .apply { "price: ${this.toString()}".printAU }


                    var count: Pair<PartExistenceDTO, String>

                    element
                        .select("div.m_right20")
                        .select("p.m_none")
                        .select("b")
                        .text().html2text
                        .also { quantityDescription ->
                            count = when (quantityDescription) {
                                "ПОД" -> {
                                    PartExistenceDTO.FalseExistenceDTO("под заказ") to ""
                                }

                                "50" -> {
                                    PartExistenceDTO.TrueExistence("болеe 50 шт") to ">50"
                                }

                                else -> {
                                    PartExistenceDTO.TrueExistence() to quantityDescription
                                }
                            }
                        }
                        .apply { "quantity: $this".printAU }


                    productSet.add(
                        ProductCartDTO(
                            fullLinkToProduct = linkToSite + halfLinkToProduct,
                            fullImageUrl = linkToSite + imgSrc,
                            price = price,
                            name = name,
                            article = article,
                            additionalArticles = dopArticle,
                            brand = brand.getBrand,
                            quantity = count.second,
                            existence = count.first
                        )
                    )
                }
                emit(ResourceDTO.Success(data = productSet))
            }
        }
}