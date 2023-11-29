package com.medvedev.partpriceparser.brands

sealed interface ProductBrand {
    val name: String
    val possibleNames: ArrayList<String>

    object Kamaz : ProductBrand {
        override val name: String = "ПАО \"КАМАЗ\""

        override val possibleNames: ArrayList<String> = arrayListOf("пао", "камаз", "ПАО \"КАМАЗ\"")
    }

    object Repair : ProductBrand {
        override val name: String = "Ремонтный"
        override val possibleNames: ArrayList<String> = arrayListOf(
            "ремонтный",
            "ремонтированный",
            "восcтановленный",
            "ремонт под новый с гарантией",
            "ремонт с гарантией"
        )
    }

    class Unknown(override val name: String = "Неопределен") : ProductBrand {
        override val possibleNames: ArrayList<String> = arrayListOf("Прочие")
    }
}


// todo результат поиска брендов по названию: "головка". какие и стоит ли делать так много сортировок по производителям(брендам)?
// БелЗАН \ SORL \ CUMMINS \ РААЗ АМО ЗиЛ \ Литиз г. Набережные Челны \ БелЗАН г. Белебей \ РААЗ АМО ЗиЛ \ БелОМО \
// НЕФАЗ г. Нефтекамск \ КМД \ г. Ярославль \ Mercedes Benz \ Россия \ Китай \ OE Germany \ ПК АЙК \ МАРК \ FG (Китай) \
// MADARA \ АО "Строймаш" \ ОАО "Балаковорезинотехника" \ "SORL Ruili Group China" \ ГК Ростар \ ООО "ЕНА-холдинг" \
// TECNARI \ ПАО "НЕФАЗ" \   "ПК АЙК" \ "ZF", Германия \ ОАО "Шадринский автоагрегатный завод" \ ВЧ РУС \
// ООО "Рославльские тормозные системы" РААЗ \ ООО НПО "УРАЛ" \


val String.getBrand: ProductBrand
    get() = when {
        ProductBrand.Kamaz.possibleNames.contains(this) -> ProductBrand.Kamaz
        ProductBrand.Repair.possibleNames.contains(this) -> ProductBrand.Repair
        else -> ProductBrand.Unknown(name = this)
    }

