package com.medvedev.partpriceparser.feature_parsers.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medvedev.partpriceparser.brands.ProductBrand
import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.UIEvents
import com.medvedev.partpriceparser.feature_parsers.presentation.models.ParserData
import com.medvedev.partpriceparser.feature_parsers.presentation.models.ProductCart
import com.medvedev.partpriceparser.feature_parsers.presentation.models.toPriceWithSpace
import com.medvedev.partpriceparser.feature_parsers.presentation.screen_content.CustomFilterDialog
import com.medvedev.partpriceparser.feature_parsers.presentation.screen_content.FilterItemButton
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ParseScreen(viewModel: ParserViewModel = hiltViewModel()) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UIEvents.SnackbarEvent -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }

                is UIEvents.NavigateEvent -> {/* todo расписать события навигации*/
                }
            }
        }
    }

    CustomScaffold(
        snackbarHostState = snackbarHostState,
        loadingFlag = viewModel.loadingInProgressFlag.value,
        onChangeDialogState = { viewModel.changeDialogState() }
    ) {
        ParseScreenContent(
            keyboardController = keyboardController,
            modifier = Modifier.padding(it),
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ParseScreenContent(
    keyboardController: SoftwareKeyboardController?,
    modifier: Modifier = Modifier,
    viewModel: ParserViewModel
) {

    if (viewModel.filterDialogState.value) {
        CustomFilterDialog(
            changeDialogState = { viewModel.changeDialogState() },
            productFilter = viewModel.filterProductState.value,
            brandListFilter = viewModel.brandListFilter,
            onCheckedChangeBrandState = { state, brand ->
                when (brand) {
                    ProductBrand.Kamaz -> viewModel.updateFilterShowKamazBrand(state)
                    ProductBrand.Kmz -> viewModel.updateFilterShowKmzBrand(state)
                    ProductBrand.Repair -> viewModel.updateFilterShowRepairBrand(state)
                    is ProductBrand.Unknown -> viewModel.updateFilterShowUnknownBrand(state)
                }
            },
            onCheckedChangeShowMiss = {
                viewModel.updateFilterShowMissingProduct(it)
            },
            sortList = viewModel.sortListByBrands,
            onClickOnSortItem = { productSort ->
                viewModel.updateSortFilter(productSort)
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBarArticle(
            keyboardController = keyboardController,
            viewModel = viewModel
        )

        Surface(
            modifier = Modifier
                .padding(top = 5.dp)
                .padding(horizontal = 5.dp)
        ) {
            LazyColumn(

                modifier = Modifier
                    .fillMaxHeight()
                    .padding(bottom = 10.dp)
            ) {
                items(items = viewModel.foundedProductList) { parserData ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp)
                            .padding(bottom = 3.dp)
                    ) {

                        val localContext = LocalContext.current

                        ItemColumn(
                            parserData = parserData,
                            actionGoToBrowser = { link ->
                                viewModel.openBrowser(
                                    context = localContext,
                                    linkToSite = link
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemColumn(
    parserData: ParserData,
    actionGoToBrowser: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .border(
                border = BorderStroke(2.dp, color = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(13.dp)
    ) {

        Column(modifier = Modifier.wrapContentHeight()) {
            OutlinedButton(contentPadding = PaddingValues(4.dp),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    actionGoToBrowser.invoke(parserData.linkToSearchCatalog)
                }) {
                Text(
                    modifier = Modifier.padding(2.dp),
                    text = parserData.linkToSearchCatalog,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Divider(
                modifier = Modifier.padding(horizontal = 7.dp),
                color = MaterialTheme.colorScheme.secondary,
                thickness = 1.dp
            )
            when (parserData.productParserData) {
                is Resource.Loading -> {
                    ProgressBar(modifier = Modifier.fillMaxSize())
                }

                is Resource.Error -> {
                    Text(text = parserData.productParserData.message.toString())
                }

                is Resource.Success -> {
                    parserData.productParserData.data?.let { listData ->
                        val iterator = listData.iterator()
                        while (iterator.hasNext()) {
                            val product = iterator.next()
                            ProductCardItem(
                                productCart = product,
                                actionGoToBrowser = actionGoToBrowser
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}


/*@Composable
fun TextLink(text: String) {
    Text(
        modifier = Modifier.padding(2.dp),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun GoToLinkButton(
    modifier: Modifier = Modifier,
    actionGoToBrowser: (String) -> Unit,
    parserData: ParserData,
    content: @Composable () -> Unit
) {
    OutlinedButton(contentPadding = PaddingValues(4.dp),
        modifier = modifier.padding(0.dp),
        shape = RoundedCornerShape(4.dp),
        onClick = {
            actionGoToBrowser.invoke(parserData.linkToSite)
        }) {
        content()
    }
}*/


@Composable
fun ProductCardItem(
    productCart: ProductCart,
    actionGoToBrowser: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(top = 3.dp)
            .padding(horizontal = 3.dp)
            .border(
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable {
                actionGoToBrowser.invoke(productCart.fullLinkToProduct)
            },
    ) {
        Surface {
            Row(modifier = Modifier.fillMaxWidth()) {
                /*AsyncImage(
                    model = ImageRequest.Builder(context = LocalContext.current)
                        .data(productCart.imageUrl).crossfade(true).build(),
                    // model = productCart.imageUrl,
                    contentDescription = "part_image: ${productCart.name}",
                    modifier = Modifier
                        .padding(0.dp)
                        .width(itemsHeight)
                        .height(itemsHeight)
                        .weight(1.5f)
                )*/
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(7f)
                        .padding(start = 5.dp)
                        .padding(top = 5.dp)
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = productCart.name,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = productCart.brand ?: "",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Divider(
                        modifier = Modifier.padding(horizontal = 7.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        thickness = 0.3.dp
                    )
                    Text(
                        text = "Артикул: " + productCart.article,
                        style = MaterialTheme.typography.labelSmall + MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = productCart.additionalArticles ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(3f)
                ) {
                    Text(
                        text = if (productCart.price != null) productCart.price.toPriceWithSpace + " ₽" else "",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                    Text(
                        text = productCart.existence ?: "",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = productCart.quantity ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressBar(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .size(40.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 5.dp
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBarArticle(
    keyboardController: SoftwareKeyboardController?,
    viewModel: ParserViewModel
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .padding(horizontal = 10.dp)
            .height(50.dp)
    ) {
        OutlinedTextField(textStyle = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            trailingIcon = {
                Icon(imageVector = Icons.Default.Clear,
                    contentDescription = "clear",
                    modifier = Modifier.clickable { viewModel.clearSearchText() })
            },
            placeholder = {
                Text(
                    textAlign = TextAlign.Center,
                    text = "введите необходимый артикул",
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
                viewModel.parseProducts(viewModel.textSearch.value)
            },
            keyboardOptions = KeyboardOptions(
                autoCorrect = true, keyboardType = KeyboardType.Text, imeAction = ImeAction.Search
            ),
            modifier = Modifier
                .weight(0.8f)
                .padding(end = 1.dp),
            value = viewModel.textSearch.value,
            onValueChange = {
                viewModel.changeTextSearch(it)
            })
        OutlinedIconButton(
            modifier = Modifier
                .size(45.dp)
                .padding(start = 3.dp),
            shape = RoundedCornerShape(8.dp),
            onClick = {
                viewModel.parseProducts(viewModel.textSearch.value)
                keyboardController?.hide()
            },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "search")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomScaffold(
    snackbarHostState: SnackbarHostState,
    loadingFlag: Boolean,
    onChangeDialogState: () -> Unit,
    content: @Composable (paddingValues: PaddingValues) -> Unit
) {

    // todo добавить общий прогрессБар для отслеживания всех загрузок

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.height(50.dp),
                title = {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Парсер артикула",
                            modifier = Modifier
                                .align(Alignment.CenterStart),
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Start,
                        )
                        if (loadingFlag) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onTertiary,
                                trackColor = MaterialTheme.colorScheme.tertiary,
                                strokeCap = StrokeCap.Butt
                            )
                        }
                        FilterItemButton(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(35.dp),
                            onChangeDialogState = onChangeDialogState
                        )
                    }
                }, colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
                )
            )
        }) { paddingValues ->
        content(paddingValues)
    }
}