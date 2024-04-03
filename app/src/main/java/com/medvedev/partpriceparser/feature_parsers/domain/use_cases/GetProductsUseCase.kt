package com.medvedev.partpriceparser.feature_parsers.domain.use_cases

import com.medvedev.partpriceparser.feature_parsers.domain.mappers.toResult
import com.medvedev.partpriceparser.feature_parsers.presentation.models.ParserData
import com.medvedev.partsparser.sources.AutoMotorsParser
import com.medvedev.partsparser.sources.AvtoKamaParser
import com.medvedev.partsparser.sources.KamaCenterParser
import com.medvedev.partsparser.sources.MarkParser
import com.medvedev.partsparser.sources.MidkamParser
import com.medvedev.partsparser.sources.NikoParser
import com.medvedev.partsparser.sources.ProductParser
import com.medvedev.partsparser.sources.RiatParser
import com.medvedev.partsparser.sources.SkladTfkParser
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
                    source.getProduct(article).collect { resultDTO ->
                        val parserData = ParserData(
                            linkToSearchCatalog = source.linkToSite +
                                    source.partOfLinkToCatalog(article),
                            linkToSite = source.linkToSite,
                            siteName = source.siteName,
                            productParserData = resultDTO.toResult()
                        )
                        send(parserData)
                    }
                }
            }
        }
    }
}