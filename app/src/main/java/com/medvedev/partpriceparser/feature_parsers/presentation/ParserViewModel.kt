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
import com.medvedev.partpriceparser.core.util.printE
import com.medvedev.partpriceparser.feature_parsers.data.ProductFiltersPreferencesRepository
import com.medvedev.partpriceparser.feature_parsers.domain.use_cases.GetProductsUseCase
import com.medvedev.partpriceparser.feature_parsers.presentation.models.ParserData
import com.medvedev.partpriceparser.feature_parsers.presentation.models.filter.BrandFilter
import com.medvedev.partpriceparser.feature_parsers.presentation.models.filter.ProductFilter
import com.medvedev.partpriceparser.feature_parsers.presentation.models.filter.ProductSort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
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

    private var _foundedProductList: SnapshotStateList<ParserData> = mutableStateListOf() // todo решить проблему с дублирующимися данными
    var foundedProductList: SnapshotStateList<ParserData> = _foundedProductList


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
        BrandFilter(true, ProductBrand.Kamaz()),
        BrandFilter(true, ProductBrand.Repair()),
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
                        is ProductBrand.Kamaz -> it.copy(brandState = productFilterPreferences.showKamazBrand)
                        is ProductBrand.Repair -> it.copy(brandState = productFilterPreferences.showRepairBrand)
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

    private val _filterDialogState = mutableStateOf(false) // todo должен быть false на момент релиза
    val filterDialogState: State<Boolean> = _filterDialogState

    fun changeDialogState() {
        _filterDialogState.value = !_filterDialogState.value
    }

    private lateinit var job: Job

    // todo нужен только для выведения логов
    val listWithExistences: MutableSet<String> = mutableSetOf()

    private var parseJob: (String) -> Unit = { articleToSearch ->
        if (articleToSearch.isEmpty()) {
            addUIEvent(UIEvents.SnackbarEvent(message = "Введите артикул"))
        } else {

            /*if (!::job.isInitialized) {
                "parseJob is not init. before".printD
            } else {
                "parseJob status. before: $job".printD
            }*/

            if (!::job.isInitialized || job.isCancelled || job.isCompleted) {
                job = viewModelScope.launch(context = Dispatchers.IO) {
                    try {
                        _foundedProductList.clear()
                        getProductsUseCase.execute(articleToSearch)
                            .buffer(30)
                            .collect { data ->
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

                                data.productParserData.data?.forEach {
                                    // todo нужно только для выведения логов
                                    it.existence.also { existence ->
                                        listWithExistences.add(existence.description)
                                    }
                                }

                                changeStateOfCommonLoading()
                            }
                    } catch (e: Exception) {
                        e.printE
                    }
                }
            } else { // todo просто защита от дурака, лишнняя, можно удалить
                viewModelScope.launch(Dispatchers.IO) {
                    cancelParsing()
                }
            }
            // "parseJob status. after: $job".printD
        }
    }

    fun cancelParsing() {
        viewModelScope.launch {
            job.cancel()
            _foundedProductList.replaceAll { parserData ->
                when (parserData.productParserData) {
                    is Resource.Loading -> parserData.copy(productParserData = Resource.Error("Parser is stopped"))
                    else -> parserData
                }
            }
            _loadingInProgressFlag.value = false
        }
    }

    fun parseProducts(articleToSearch: String) {
        parseJob(articleToSearch)
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

            if (!loadingWork) {
                // todo нужен только для выведения логов
                "allExistence: $listWithExistences".printD
            }

            _loadingInProgressFlag.value = loadingWork
            // "globalLoading: ${_loadingInProgressFlag.value}".printD
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


    private val _textSearch: MutableState<String> =
        mutableStateOf("6520-2405024")// 740.1003010-20 или 6520-2405024 todo изменить на пусто
    val textSearch: State<String> = _textSearch

    fun changeTextSearch(text: String) {
        _textSearch.value = text
    }

    fun clearSearchText() {
        _textSearch.value = ""
    }
}