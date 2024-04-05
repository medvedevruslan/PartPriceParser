package com.medvedev.partsparser.models


data class ProductCartParse(
    val fullLinkToProduct: String,
    val fullImageUrl: String,
    val price: Float?,
    val name: String,
    val article: String,
    val additionalArticles: String?,
    val brand: ProductBrandParse,
    val quantity: String?,
    val existence: PartExistenceParse
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
