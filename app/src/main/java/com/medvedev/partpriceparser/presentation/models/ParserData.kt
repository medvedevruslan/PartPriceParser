package com.medvedev.partpriceparser.presentation.models

import com.medvedev.partpriceparser.core.util.Resource
import java.net.URL

data class ParserData(
    val link: URL,
    val siteName: String,
    val productList: Resource<List<ProductCart>>
)
