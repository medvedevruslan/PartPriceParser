package com.medvedev.partpriceparser.feature_parsers.presentation.models.filter

import com.medvedev.partpriceparser.brands.ProductBrand

data class BrandFilter(
    var brandState: Boolean,
    var brandProduct: ProductBrand
)
