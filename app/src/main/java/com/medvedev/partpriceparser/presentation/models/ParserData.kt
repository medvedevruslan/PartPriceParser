package com.medvedev.partpriceparser.presentation.models

import com.medvedev.partpriceparser.core.util.Resource

data class ParserData(
    val halfLinkSearchCatalog:String,
    val linkToSite: String,
    val siteName: String,
    val productList: Resource<List<ProductCart>>
)
