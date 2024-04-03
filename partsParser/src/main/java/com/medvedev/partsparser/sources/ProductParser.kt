package com.medvedev.partsparser.sources


import com.medvedev.partsparser.models.ProductCartDTO
import com.medvedev.partsparser.utils.ResourceDTO
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

abstract class ProductParser {

    abstract val linkToSite: String
    abstract val siteName: String
    abstract val partOfLinkToCatalog: (String) -> String


    suspend fun getProduct(articleToSearch: String): Flow<ResourceDTO<Set<ProductCartDTO>>> = flow {
        try {
            emit(ResourceDTO.Loading())
            workWithServer(articleToSearch).collect {
                emit(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Sentry.captureException(e)
            emit(ResourceDTO.Error(e.toString()))
        }
    }
    protected abstract val workWithServer: (String) -> Flow<ResourceDTO<Set<ProductCartDTO>>>
}