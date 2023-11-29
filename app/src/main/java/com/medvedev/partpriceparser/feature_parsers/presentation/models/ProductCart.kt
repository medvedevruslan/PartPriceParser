package com.medvedev.partpriceparser.feature_parsers.presentation.models

import com.medvedev.partpriceparser.brands.ProductBrand

data class ProductCart(
    val fullLinkToProduct: String,
    val fullImageUrl: String,
    val price: Float?,
    val name: String,
    val article: String,
    val additionalArticles: String?,
    val brand: ProductBrand,
    val quantity: String?,
    val existence: String?
)

val String.getCleanPrice: Float?
    get() = run {
        if (this.isEmpty()) null
        else {
            this.replace(",",".")
                .replace(" ", "")
                .toFloatOrNull()
        }
    }



val Float.toPriceWithSpace: String
    get() = toString()
        .replace(
            regex = Regex(pattern = """(?!^)(?=(\d{3})+(?=\.|$))"""),
            replacement = " "
        )
