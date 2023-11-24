package com.medvedev.partpriceparser.feature_parsers.data

import androidx.datastore.core.DataStore
import com.medvedev.partpriceparser.ProductFilterPreferences
import com.medvedev.partpriceparser.ProductFilterPreferences.SortOrderProducts
import com.medvedev.partpriceparser.core.util.printE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

class ProductFiltersPreferencesRepository(private val filterPreferencesStore: DataStore<ProductFilterPreferences>) {

    val filterPreferencesFlow: Flow<ProductFilterPreferences> = filterPreferencesStore.data
        .catch { exception ->
            if (exception is IOException) {
                "Error reading product filters preferences: $exception".printE
                emit(ProductFilterPreferences.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun updateShowKamazBrand(enable: Boolean) {
        filterPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setShowKamazBrand(enable).build()
        }
    }

    suspend fun updateShowKmzBrand(enable: Boolean) {
        filterPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setShowKmzBrand(enable).build()
        }
    }

    suspend fun updateRepairBrand(enable: Boolean) {
        filterPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setShowRepairBrand(enable).build()
        }
    }

    suspend fun updateShowUnknownBrand(enable: Boolean) {
        filterPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setShowUnknownBrand(enable).build()
        }
    }

    suspend fun updateShowMissingProduct(enable: Boolean) {
        filterPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setShowMissingProduct(enable).build()
        }
    }

    suspend fun updateSortOrder(sortProduct: SortOrderProducts) {
        filterPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setSortOrderValue(sortProduct.ordinal).build()
        }
    }


}