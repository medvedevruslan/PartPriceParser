package com.medvedev.partpriceparser.feature_parsers.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medvedev.partpriceparser.ProductFilterPreferences.SortOrderProducts
import com.medvedev.partpriceparser.brands.ProductBrand
import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.UIEvents
import com.medvedev.partpriceparser.core.util.printD
import com.medvedev.partpriceparser.feature_parsers.data.ProductFiltersPreferencesRepository
import com.medvedev.partpriceparser.feature_parsers.domain.use_cases.GetProductsUseCase
import com.medvedev.partpriceparser.feature_parsers.presentation.models.ParserData
import com.medvedev.partpriceparser.feature_parsers.presentation.models.filter.BrandFilter
import com.medvedev.partpriceparser.feature_parsers.presentation.models.filter.ProductFilter
import com.medvedev.partpriceparser.feature_parsers.presentation.models.filter.ProductSort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParserViewModel @Inject constructor(private val productFiltersPreferencesRepository: ProductFiltersPreferencesRepository) :
    ViewModel() {

    private val getProductsUseCase = GetProductsUseCase()

    val sortListByBrands: ArrayList<ProductSort> = arrayListOf(
        ProductSort.ByStoreNameAlphabetically,
        ProductSort.CheapFirst,
        ProductSort.ExpensiveFirst
    )

    private val _uiEvents = MutableSharedFlow<UIEvents>()
    val uiEvents: SharedFlow<UIEvents> = _uiEvents.asSharedFlow()

    private val _foundedProductList: SnapshotStateList<ParserData> = mutableStateListOf()
    val foundedProductList = _foundedProductList


    private fun addUIEvent(event: UIEvents) {
        viewModelScope.launch {
            _uiEvents.emit(event)
        }
    }


    private val _filterProductState = mutableStateOf(
        ProductFilter(
            showMissingItems = true,
            selectedSort = ProductSort.ByStoreNameAlphabetically
        )
    )
    val filterProductState: State<ProductFilter> = _filterProductState


    private var _brandListFilter: SnapshotStateList<BrandFilter> = mutableStateListOf(
        BrandFilter(true, ProductBrand.Kamaz),
        BrandFilter(true, ProductBrand.Repair),
        BrandFilter(true, ProductBrand.Unknown())
    )
    val brandListFilter: List<BrandFilter> = _brandListFilter

    init {
        initFilterPreferences()
    }

    private fun initFilterPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            productFiltersPreferencesRepository.filterPreferencesFlow.collect { productFilterPreferences ->

                _filterProductState.value = _filterProductState.value.copy(
                    showMissingItems = productFilterPreferences.showMissingProduct,
                    selectedSort = when (productFilterPreferences.sortOrder) {
                        SortOrderProducts.BY_FIRST_CHEAP -> ProductSort.CheapFirst
                        SortOrderProducts.BY_FIRST_EXPENSIVE -> ProductSort.ExpensiveFirst
                        else -> ProductSort.ByStoreNameAlphabetically
                    }
                )

                _brandListFilter.replaceAll {
                    when (it.brandProduct) {
                        ProductBrand.Kamaz -> it.copy(brandState = productFilterPreferences.showKamazBrand)
                        ProductBrand.Repair -> it.copy(brandState = productFilterPreferences.showRepairBrand)
                        is ProductBrand.Unknown -> it.copy(brandState = productFilterPreferences.showUnknownBrand)
                    }
                }
            }
        }
    }

   /* private fun applyingFilters(){ todo
        if (_filterProductState.value.showMissingItems){
            foundedProductList.filter {
                it.
            }
        }
    }*/


    fun updateFilterShowMissingProduct(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            productFiltersPreferencesRepository.updateShowMissingProduct(enable)
        }
    }

    fun updateFilterShowKamazBrand(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            productFiltersPreferencesRepository.updateShowKamazBrand(enable)
        }
    }

    fun updateFilterShowRepairBrand(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            productFiltersPreferencesRepository.updateRepairBrand(enable)
        }
    }

    fun updateFilterShowUnknownBrand(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            productFiltersPreferencesRepository.updateShowUnknownBrand(enable)
        }
    }
    fun updateSortFilter(productSort: ProductSort) {
        viewModelScope.launch(Dispatchers.IO) {
            when (productSort.protoName) {

                SortOrderProducts.BY_NAME.name ->
                    productFiltersPreferencesRepository.updateSortOrder(SortOrderProducts.BY_NAME)

                SortOrderProducts.BY_FIRST_CHEAP.name ->
                    productFiltersPreferencesRepository.updateSortOrder(SortOrderProducts.BY_FIRST_CHEAP)

                SortOrderProducts.BY_FIRST_EXPENSIVE.name ->
                    productFiltersPreferencesRepository.updateSortOrder(SortOrderProducts.BY_FIRST_EXPENSIVE)
                
            }
        }
    }

    private val _filterDialogState = mutableStateOf(true) // todo должен быть false на момент релиза
    val filterDialogState: State<Boolean> = _filterDialogState

    fun changeDialogState() {
        _filterDialogState.value = !_filterDialogState.value
    }


    @OptIn(InternalCoroutinesApi::class)
    fun parseProducts(articleToSearch: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _foundedProductList.clear()
            if (articleToSearch.isEmpty()) {
                addUIEvent(UIEvents.SnackbarEvent(message = "Введите артикул"))
            } else {
                getProductsUseCase.execute(articleToSearch)
                    .buffer(10)
                    .collect { data ->
                        synchronized(Object()) {
                            val iterator: MutableIterator<ParserData> =
                                _foundedProductList.iterator()

                            while (iterator.hasNext()) {
                                val value = iterator.next()
                                if (value.siteName == data.siteName) {
                                    iterator.remove()
                                }
                            }
                            _foundedProductList.add(data)
                            _foundedProductList.sortBy { it.siteName }

                            changeStateOfCommonLoading()
                        }
                    }
            }
        }
    }


    private val _loadingInProgressFlag: MutableState<Boolean> = mutableStateOf(false)
    val loadingInProgressFlag = _loadingInProgressFlag

    private fun changeStateOfCommonLoading() {
        var loadingWork = false
        for (productData in _foundedProductList) {
            if (productData.productParserData is Resource.Loading) {
                loadingWork = true
                break
            }
        }
        if (_loadingInProgressFlag.value != loadingWork) {
            _loadingInProgressFlag.value = loadingWork
            "globalLoading: ${_loadingInProgressFlag.value}".printD
        }
    }

    fun openBrowser(context: Context, linkToSite: String) {
        if (linkToSite.isNotEmpty()) {
            "start activity: $linkToSite".printD
            val openPageIntent = Intent(Intent.ACTION_VIEW, Uri.parse(linkToSite))
            context.startActivity(openPageIntent)
        } else {
            addUIEvent(event = UIEvents.SnackbarEvent(message = "Error: Link to Site is null or empty"))
        }
    }


    private val _textSearch = mutableStateOf("6520-2405024")// 740.1003010-20 todo изменить на пусто
    val textSearch = _textSearch

    fun changeTextSearch(text: String) {
        _textSearch.value = text
    }

    fun clearSearchText() {
        _textSearch.value = ""
    }
}