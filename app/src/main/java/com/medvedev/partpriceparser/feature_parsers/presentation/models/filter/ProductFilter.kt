package com.medvedev.partpriceparser.feature_parsers.presentation.models.filter

data class ProductFilter(
    var showMissingItems: Boolean,
    var selectedSort: ProductSort = ProductSort.ByStoreNameAlphabetically
)
