package com.medvedev.partpriceparser.domain.use_cases

import com.medvedev.partpriceparser.feature_parsers.ProductParser
import com.medvedev.partpriceparser.feature_parsers.sources.AutoMotorsParser
import com.medvedev.partpriceparser.feature_parsers.sources.AvtoKamaParser
import com.medvedev.partpriceparser.feature_parsers.sources.KamaCenterParser
import com.medvedev.partpriceparser.feature_parsers.sources.MarkParser
import com.medvedev.partpriceparser.feature_parsers.sources.MidkamParser
import com.medvedev.partpriceparser.feature_parsers.sources.NikoParser
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

    private val midkamParser = MidkamParser()

    private val nikoParser = NikoParser()

    private val parserSourcesList: ArrayList<ProductParser> = arrayListOf(
        autoMotorsParser,
        skladTfkParser,
        riatParser,
        markParser,
        kamaCenterParser,
        avtoKamaParser,
        midkamParser,
        nikoParser
    )

    suspend fun execute(article: String): Flow<ParserData> {
        return channelFlow {

            parserSourcesList.forEach { source ->
                launch(Dispatchers.IO) {
                    source.getProduct(article).collect { result ->
                        val parserData = ParserData(
                            linkToSearchCatalog = source.linkToSite +
                                    source.partOfLinkToCatalog(article),
                            linkToSite = source.linkToSite,
                            siteName = source.siteName,
                            productParserData = result
                        )
                        send(parserData)
                    }
                }
            }
        }
    }
}