package com.medvedev.parts.main.presentation.models.filter

data class ProductFilter(
    var showMissingItems: Boolean,
    var selectedSort: ProductSort = ProductSort.ByStoreNameAlphabetically
)
