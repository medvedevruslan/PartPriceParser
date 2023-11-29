package com.medvedev.partpriceparser.feature_parsers.data

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.feature_parsers.presentation.models.ProductCart
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

abstract class ProductParser {

    abstract val linkToSite: String
    abstract val siteName: String
    abstract val partOfLinkToCatalog: (String) -> String

    @Volatile
    protected var productSet: MutableSet<ProductCart> = mutableSetOf()

    suspend fun getProduct(articleToSearch: String): Flow<Resource<Set<ProductCart>>> = flow {
        try {
            if (productSet.size > 0) productSet.clear()
            emit(Resource.Loading())
            workWithServer(articleToSearch).collect {
                emit(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Sentry.captureException(e)
            emit(Resource.Error(e.toString()))
        }
    }
    protected abstract val workWithServer: (String) -> Flow<Resource<Set<ProductCart>>>
}