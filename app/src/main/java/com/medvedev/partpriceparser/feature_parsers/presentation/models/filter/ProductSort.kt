package com.medvedev.partpriceparser.feature_parsers.presentation.models.filter



sealed interface ProductSort {
    val filterDescription: String

    object ByShopAlphabet : ProductSort {
        override val filterDescription: String = "Сортировка по алфавиту"
    }

    object CheapFirst : ProductSort {
        override val filterDescription: String = "Сначала недорогие"
    }

    object ExpensiveFirst : ProductSort {
        override val filterDescription: String = "Сначала дорогие"
    }
}