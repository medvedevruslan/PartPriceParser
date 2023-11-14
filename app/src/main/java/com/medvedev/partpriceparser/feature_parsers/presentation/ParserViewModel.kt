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
import com.medvedev.partpriceparser.brands.ProductBrand
import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.UIEvents
import com.medvedev.partpriceparser.core.util.printD
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

@HiltViewModel
class ParserViewModel : ViewModel() {

    private val getProductsUseCase = GetProductsUseCase()

    private val _uiEvents = MutableSharedFlow<UIEvents>()
    val uiEvents: SharedFlow<UIEvents> = _uiEvents.asSharedFlow()

    private val _foundedProductList: SnapshotStateList<ParserData> = mutableStateListOf()
    val foundedProductList = _foundedProductList

    private val _loadingInProgressFlag: MutableState<Boolean> = mutableStateOf(false)
    val loadingInProgressFlag = _loadingInProgressFlag

    private fun addUIEvent(event: UIEvents) {
        viewModelScope.launch {
            _uiEvents.emit(event)
        }
    }

    private val _filterDialogState = mutableStateOf(true) // todo должен быть false
    val filterDialogState: State<Boolean> = _filterDialogState

    fun changeDialogState() {
        _filterDialogState.value = !_filterDialogState.value
    }


    private val _filterState = mutableStateOf(
        ProductFilter(
            showMissingItems = true,
            selectedSort = ProductSort.ByShopAlphabet
        )
    )
    val filterState: State<ProductFilter> = _filterState

    private var _brandListFilter = mutableStateListOf<BrandFilter>(
        BrandFilter(true, ProductBrand.Kamaz),
        BrandFilter(true, ProductBrand.Kmz),
        BrandFilter(true, ProductBrand.Repair),
        BrandFilter(true, ProductBrand.Unknown())
    )
    val brandListFilter = _brandListFilter

    fun changeFilterShowMissingItems(changeToBoolean: Boolean) {
        _filterState.value = _filterState.value.copy(showMissingItems = changeToBoolean)
    }

    fun changeListBrand(brandState: Boolean, brand: ProductBrand) {
        _brandListFilter.replaceAll {
            if (it.brandProduct == brand) {
                it.brandState = brandState
                it
            } else it
        }
    }

    fun changeSortState(brandSort: ProductSort) {
        _filterState.value = _filterState.value.copy(selectedSort = brandSort)
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


    private val _textSearch = mutableStateOf("6520-2405024")// 740.1003010-20
    val textSearch = _textSearch

    fun changeTextSearch(text: String) {
        _textSearch.value = text
    }

    fun clearSearchText() {
        _textSearch.value = ""
    }
}