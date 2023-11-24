package com.medvedev.partpriceparser.feature_parsers.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.medvedev.partpriceparser.ProductFilterPreferences
import com.medvedev.partpriceparser.feature_parsers.data.ProductFilterPreferencesSerializer
import com.medvedev.partpriceparser.feature_parsers.data.ProductFiltersPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val DATA_STORE_FILE_NAME = "product_filter.proto"

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideProductFilterProtoDataStore(@ApplicationContext appContext: Context): DataStore<ProductFilterPreferences> =
        DataStoreFactory.create(
            serializer = ProductFilterPreferencesSerializer,
            produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) },
            corruptionHandler = null,
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )


    @Provides
    @Singleton
    fun provideProductFilterPreferencesRepository(prefStore: DataStore<ProductFilterPreferences>) =
        ProductFiltersPreferencesRepository(filterPreferencesStore = prefStore)


}