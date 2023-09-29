package com.medvedev.partpriceparser.presentation.models

import java.net.URL

data class ProductCart(
    val linkToProduct:URL,
    val imageUrl: String,
    val price: String,
    val name: String,
    val alternativeName: String,
    val article: String,
    val additionalArticles: String,
    val brand: String,
    val quantity: Int,
    val existence: String
)
