package com.medvedev.partsparser.models

import com.medvedev.partsparser.utils.Resource


data class ParserData(
    val linkToSearchCatalog: String = "",
    val linkToSite: String = "",
    val siteName: String = "",
    val productParserData: Resource<Set<ReceivedProductData>> = Resource.Success(data = setOf())
)
