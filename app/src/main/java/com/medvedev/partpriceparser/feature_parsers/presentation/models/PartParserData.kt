package com.medvedev.partpriceparser.feature_parsers.presentation.models

import com.medvedev.partsparser.utils.Resource


data class PartParserData(
    val linkToSearchCatalog: String = "",
    val linkToSite: String = "",
    val siteName: String = "",
    val productParserData: Resource<Set<ProductCart>> = Resource.Success(data = setOf())
)
