package com.medvedev.parts.main.presentation.models.filter

import com.medvedev.parts.main.presentation.models.ProductBrand

data class BrandFilter(
    var brandState: Boolean,
    var brandProduct: ProductBrand
)
