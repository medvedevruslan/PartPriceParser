package com.medvedev.partpriceparser.feature_parsers.presentation.models.filter

import com.medvedev.partpriceparser.ProductFilterPreferences.SortOrderProducts


sealed interface ProductSort {
    val filterDescription: String
    val protoName: String

    data object ByStoreNameAlphabetically : ProductSort {
        override val filterDescription: String = "Сортировка по алфавиту"
        override val protoName: String = SortOrderProducts.BY_NAME.name
    }

    data object CheapFirst : ProductSort {
        override val filterDescription: String = "Сначала недорогие"
        override val protoName: String = SortOrderProducts.BY_FIRST_CHEAP.name
    }

    data object ExpensiveFirst : ProductSort {
        override val filterDescription: String = "Сначала дорогие"
        override val protoName: String = SortOrderProducts.BY_FIRST_EXPENSIVE.name
    }
}