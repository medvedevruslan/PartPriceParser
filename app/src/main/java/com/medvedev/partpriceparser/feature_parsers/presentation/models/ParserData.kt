package com.medvedev.partpriceparser.feature_parsers.presentation.models

import com.medvedev.partpriceparser.core.util.Resource

data class ParserData(
    val linkToSearchCatalog: String = "",
    val linkToSite: String = "",
    val siteName: String = "",
    val productParserData: Resource<Set<ProductCart>> = Resource.Success(data = setOf())
)
