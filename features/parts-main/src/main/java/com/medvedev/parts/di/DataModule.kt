package com.medvedev.parts.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.medvedev.parts.main.ProductFilterPreferences
import com.medvedev.parts.main.filter.ProductFilterPreferencesSerializer
import com.medvedev.parts.main.filter.ProductFiltersPreferencesRepository
import com.medvedev.parts_domain.PartsRepository
import com.medvedev.parts_domain.usecases.GetPartsDataUseCase
import com.medvedev.partsparser.PartsParser
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
    fun provideGetPartsDataUseCase(): GetPartsDataUseCase {
        return GetPartsDataUseCase(providePartsRepository())
    }

    @Provides
    fun providePartsRepository(): PartsRepository {
        return PartsRepository(providePartsParser())
    }

    @Provides
    @Singleton
    fun providePartsParser(): PartsParser {
        return PartsParser()
    }

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