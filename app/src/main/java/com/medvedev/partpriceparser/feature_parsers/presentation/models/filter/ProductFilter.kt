package com.medvedev.partpriceparser.feature_parsers.presentation.models.filter

data class ProductFilter(
    var showMissingItems: Boolean,
    var selectedSort: ProductSort = ProductSort.ByShopAlphabet,
    val sortListByBrands: ArrayList<ProductSort> = arrayListOf(
        ProductSort.ByShopAlphabet,
        ProductSort.CheapFirst,
        ProductSort.ExpensiveFirst
    )
)
