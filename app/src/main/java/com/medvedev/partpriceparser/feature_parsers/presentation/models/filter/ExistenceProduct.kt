package com.medvedev.partpriceparser.feature_parsers.presentation.models.filter

sealed interface ExistenceProduct {
    val description: String
    val possibleDescription: ArrayList<String>

    object ExistenceTrue : ExistenceProduct {
        override val description: String = "В наличии"
        override val possibleDescription: ArrayList<String> =
            arrayListOf("в наличии", "Мало в наличии")
    }

    object ExistenceFalse : ExistenceProduct {
        override val description: String = "Отсутствует"
        override val possibleDescription: ArrayList<String> = arrayListOf()
    }

    class ExistenceUnknown(override val description: String) : ExistenceProduct {
        override val possibleDescription: ArrayList<String> = arrayListOf("наличие уточняйте")
    }

}