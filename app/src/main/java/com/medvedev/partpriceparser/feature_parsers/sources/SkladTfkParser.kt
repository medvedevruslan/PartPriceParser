package com.medvedev.partpriceparser.feature_parsers.sources

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.feature_parsers.ProductParser
import com.medvedev.partpriceparser.presentation.models.ProductCart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber


class SkladTfkParser : ProductParser() {

    val Any.printTFK
        get() = Timber.tag("developerTFK").d(toString())

    override val linkToSite: String = "https://skladtfk.ru/"
    override val siteName: String = "STFK KAMAZ"
    override val partOfLinkToCatalog: String = "https://skladtfk.ru/catalog/search/?text="

    @Suppress("OVERRIDE_BY_INLINE")
    override inline val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>
        get() = { articleToSearch ->
            flow {
                val actualDocument = documentCatalogAddressLink(articleToSearch)
                val productElements = actualDocument.select("div.tovarlist catalog__search_virt_action_list")
                productElements.printTFK

            }
        }


}