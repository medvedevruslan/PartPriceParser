package com.medvedev.partpriceparser.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.core.util.UIEvents
import com.medvedev.partpriceparser.presentation.models.ParserData
import com.medvedev.partpriceparser.presentation.models.ProductCart
import kotlinx.coroutines.flow.collectLatest

const val ITEM_HEIGHT_IN_LAZY_COLUMN: Int = 130


@Composable
fun ParseScreen(viewModel: ParserViewModel) {
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

    CustomScaffold(snackbarHostState) {
        ParseScreenContent(
            modifier = Modifier.padding(it),
            viewModel = viewModel
        )
    }
}

@Composable
fun ParseScreenContent(
    modifier: Modifier = Modifier,
    viewModel: ParserViewModel
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBarArticle(viewModel = viewModel)

        Surface(
            modifier = Modifier
                .padding(top = 5.dp)
                .padding(horizontal = 5.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(items = viewModel.foundedProductList) { parserData ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.background)
                            .padding(horizontal = 5.dp)
                    ) {

                        val localContext = LocalContext.current

                        ItemLazyColumn(
                            parserData = parserData, actionGoToBrowser = { link ->
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
fun ItemLazyColumn(
    parserData: ParserData, actionGoToBrowser: (String) -> Unit
) {

    val listSize = parserData.productList.data?.size ?: 1

    Surface(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .border(
                border = BorderStroke(2.dp, color = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(8.dp)
            ), shape = RoundedCornerShape(13.dp)
    ) {
        LazyColumn(modifier = Modifier.height((ITEM_HEIGHT_IN_LAZY_COLUMN * listSize + 65).dp)) {
            item {
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


                /*Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GoToLinkButton(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 3.dp),
                        actionGoToBrowser = { actionGoToBrowser.invoke(parserData.linkToSite) },
                        parserData = parserData
                    ) {
                        TextLink(parserData.linkToSite)
                    }

                    GoToLinkButton(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 3.dp),
                        actionGoToBrowser = { actionGoToBrowser.invoke(parserData.halfLinkSearchCatalog + searchText) },
                        parserData = parserData
                    ) {
                        TextLink(text = parserData.halfLinkSearchCatalog + searchText)
                    }
                }*/

                Divider(
                    modifier = Modifier.padding(horizontal = 7.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    thickness = 1.dp
                )
            }
            when (parserData.productList) {
                is Resource.Loading -> {
                    item { ProgressBar(modifier = Modifier.fillMaxSize()) }
                }

                is Resource.Error -> {
                    item { Text(text = parserData.productList.message.toString()) }
                }

                is Resource.Success -> {
                    parserData.productList.data?.let {
                        items(items = it) { productCart ->
                            ProductCardItem(
                                productCart = productCart,
                                itemsHeight = ITEM_HEIGHT_IN_LAZY_COLUMN.dp,
                                actionGoToBrowser = actionGoToBrowser
                            )
                        }
                    }
                }
            }
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
    productCart: ProductCart, itemsHeight: Dp, actionGoToBrowser: (String) -> Unit
) {
    Card(modifier = Modifier
        .padding(top = 3.dp)
        .padding(horizontal = 3.dp)
        .border(
            border = BorderStroke(
                1.dp, color = MaterialTheme.colorScheme.secondaryContainer
            ), shape = RoundedCornerShape(4.dp)
        )
        .clickable {
            actionGoToBrowser.invoke(productCart.linkToProduct)
        }) {
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
                        .height(itemsHeight)
                        .weight(7f)
                        .padding(start = 5.dp)
                ) {
                    Row(modifier = Modifier.padding(top = 5.dp)) {
                        Text(
                            text = productCart.name,
                            modifier = Modifier.weight(5f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = productCart.brand,
                            modifier = Modifier.weight(5f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Text(
                        text = productCart.article,
                        style = MaterialTheme.typography.labelSmall + MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = productCart.additionalArticles ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
                Column(
                    modifier = Modifier
                        .height(itemsHeight)
                        .weight(3f)
                ) {
                    Text(
                        text = productCart.price ?: "-",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                    Text(
                        text = productCart.existence ?: " - ",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = productCart.quantity ?: "-",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarArticle(viewModel: ParserViewModel) {
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
            keyboardActions = KeyboardActions { viewModel.parseProducts(viewModel.textSearch.value) },
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
            onClick = { viewModel.parseProducts(viewModel.textSearch.value) },
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
    content: @Composable (paddingValues: PaddingValues) -> Unit
) {
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(
            modifier = Modifier.height(50.dp), title = {
                Surface(tonalElevation = 5.dp) {
                    Text(
                        text = "Парсер артикула:",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Start,
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