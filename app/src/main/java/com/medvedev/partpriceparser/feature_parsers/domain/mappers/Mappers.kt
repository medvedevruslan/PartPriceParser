package com.medvedev.partpriceparser.feature_parsers.domain.mappers

import com.medvedev.partpriceparser.feature_parsers.presentation.models.PartParserData
import com.medvedev.partpriceparser.feature_parsers.presentation.models.ProductCart
import com.medvedev.partpriceparser.feature_parsers.presentation.models.ProductExistence
import com.medvedev.partsparser.models.ParserData
import com.medvedev.partsparser.models.PartExistence
import com.medvedev.partsparser.models.ReceivedProductData
import com.medvedev.partsparser.utils.Resource

fun ParserData.toPartParserData(): PartParserData {
    return PartParserData(
        linkToSearchCatalog = linkToSearchCatalog,
        linkToSite = linkToSite,
        siteName = siteName,
        productParserData = productParserData.toResourceProductCart()
    )
}


fun Resource<Set<ReceivedProductData>>.toResourceProductCart(): Resource<Set<ProductCart>> {
    val newResult: Resource<Set<ProductCart>> = when (this) {
        is Resource.Loading -> Resource.Loading()
        is Resource.Error -> Resource.Error(message = message ?: "")
        is Resource.Success -> Resource.Success(
            data = data?.map { product ->
                ProductCart(
                    fullLinkToProduct = product.fullLinkToProduct,
                    fullImageUrl = product.fullImageUrl,
                    price = product.price,
                    name = product.name,
                    article = product.article,
                    additionalArticles = product.additionalArticles,
                    brand = product.brand,
                    quantity = product.quantity,
                    existence = product.existence.toProductExistence(),
                )
            }?.toSet()
        )
    }
    return newResult
}

fun PartExistence.toProductExistence(): ProductExistence {
    return when (this) {
        is PartExistence.TrueExistence -> ProductExistence.Positive(description = description)
        is PartExistence.FalseExistence -> ProductExistence.Negative(description = description)
        else -> ProductExistence.Unknown(description = description)
    }
}