package com.medvedev.partpriceparser.brands

sealed interface ProductBrand {
    val name: String
    val possibleNames: ArrayList<String>

    object Kamaz : ProductBrand {
        override val name: String = "ПАО \"КАМАЗ\""

        override val possibleNames: ArrayList<String> = arrayListOf("пао", "камаз")
    }

    object Kmz : ProductBrand {
        override val name: String = "КМЗ"
        override val possibleNames: ArrayList<String> = arrayListOf("кмз")
    }

    object Repair : ProductBrand {
        override val name: String = "Ремонтный"
        override val possibleNames: ArrayList<String> = arrayListOf("ремонт", "восcтановленный")
    }

    class Unknown(description: String = "Неопределен") : ProductBrand {
        override val name: String = description
        override val possibleNames: ArrayList<String> = arrayListOf()
    }


}

