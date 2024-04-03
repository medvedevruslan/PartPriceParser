package com.medvedev.partpriceparser.feature_parsers.presentation.models.filter

import com.medvedev.partpriceparser.feature_parsers.presentation.models.ProductBrand

data class BrandFilter(
    var brandState: Boolean,
    var brandProduct: ProductBrand
)
