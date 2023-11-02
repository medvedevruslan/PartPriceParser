package com.medvedev.partpriceparser.domain.use_cases

import com.medvedev.partpriceparser.feature_parsers.sources.AutoMotorsParser
import com.medvedev.partpriceparser.feature_parsers.sources.AvtoKamaParser
import com.medvedev.partpriceparser.feature_parsers.sources.KamaCenterParser
import com.medvedev.partpriceparser.feature_parsers.sources.MarkParser
import com.medvedev.partpriceparser.feature_parsers.sources.RiatParser
import com.medvedev.partpriceparser.feature_parsers.sources.SkladTfkParser
import com.medvedev.partpriceparser.presentation.models.ParserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch


class GetProductsUseCase {

    private val autoMotorsParser = AutoMotorsParser()

    private val skladTfkParser = SkladTfkParser()

    private val riatParser = RiatParser()

    private val markParser = MarkParser()

    private val kamaCenterParser = KamaCenterParser()

    private val avtoKamaParser = AvtoKamaParser()

    suspend fun execute(article: String): Flow<ParserData> {
        return channelFlow {

            launch(Dispatchers.IO) {
                autoMotorsParser.getProduct(article).collect { result ->
                    val parserData = ParserData(
                        linkToSearchCatalog = autoMotorsParser.linkToSite +
                                autoMotorsParser.partOfLinkToCatalog(article),
                        linkToSite = autoMotorsParser.linkToSite,
                        siteName = autoMotorsParser.siteName,
                        productList = result
                    )
                    send(parserData)
                }
            }

            launch(Dispatchers.IO) {
                skladTfkParser.getProduct(article).collect { result ->
                    val parserData = ParserData(
                        linkToSearchCatalog = skladTfkParser.linkToSite +
                                skladTfkParser.partOfLinkToCatalog(article),
                        linkToSite = skladTfkParser.linkToSite,
                        siteName = skladTfkParser.siteName,
                        productList = result
                    )
                    send(parserData)
                }
            }

            launch(Dispatchers.IO) {
                riatParser.getProduct(article).collect { result ->
                    val parserData = ParserData(
                        linkToSearchCatalog = riatParser.linkToSite +
                                riatParser.partOfLinkToCatalog(article),
                        linkToSite = riatParser.linkToSite,
                        siteName = riatParser.siteName,
                        productList = result
                    )
                    send(parserData)
                }
            }

            launch(Dispatchers.IO) {
                markParser.getProduct(article).collect { result ->
                    val parserData = ParserData(
                        linkToSearchCatalog = markParser.linkToSite +
                                markParser.partOfLinkToCatalog(article),
                        linkToSite = markParser.linkToSite,
                        siteName = markParser.siteName,
                        productList = result
                    )
                    send(parserData)
                }
            }

            launch(Dispatchers.IO) {
                kamaCenterParser.getProduct(article).collect { result ->
                    val parserData = ParserData(
                        linkToSearchCatalog = kamaCenterParser.linkToSite +
                                kamaCenterParser.partOfLinkToCatalog(article),
                        linkToSite = kamaCenterParser.linkToSite,
                        siteName = kamaCenterParser.siteName,
                        productList = result
                    )
                    send(parserData)
                }
            }

            launch(Dispatchers.IO) {
                avtoKamaParser.getProduct(article).collect { result ->
                    val parserData = ParserData(
                        linkToSearchCatalog = avtoKamaParser.linkToSite +
                                avtoKamaParser.partOfLinkToCatalog(article),
                        linkToSite = avtoKamaParser.linkToSite,
                        siteName = avtoKamaParser.siteName,
                        productList = result
                    )
                    send(parserData)
                }
            }
        }
    }
}