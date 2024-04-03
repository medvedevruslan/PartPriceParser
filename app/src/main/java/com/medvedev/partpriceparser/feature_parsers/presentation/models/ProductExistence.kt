package com.medvedev.partpriceparser.feature_parsers.presentation.models

sealed class ProductExistence(val description: String) {

    class Positive(description: String) : ProductExistence(description)

    class Negative(description: String) : ProductExistence(description)

    class Unknown(description: String) : ProductExistence(description)

}