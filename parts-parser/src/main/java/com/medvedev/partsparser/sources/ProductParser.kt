package com.medvedev.partsparser.sources


import com.medvedev.partsparser.models.ProductCartParse
import com.medvedev.partsparser.utils.ResourceParse
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal abstract class ProductParser {

    abstract val linkToSite: String
    abstract val siteName: String
    abstract val partOfLinkToCatalog: (String) -> String


    suspend fun getProduct(articleToSearch: String): Flow<ResourceParse<Set<ProductCartParse>>> = flow {
        try {
            emit(ResourceParse.Loading())
            workWithServer(articleToSearch).collect {
                emit(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Sentry.captureException(e)
            emit(ResourceParse.Error(e.toString()))
        }
    }
    protected abstract val workWithServer: (String) -> Flow<ResourceParse<Set<ProductCartParse>>>
}