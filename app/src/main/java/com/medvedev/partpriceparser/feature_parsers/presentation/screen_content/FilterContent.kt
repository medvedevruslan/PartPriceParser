package com.medvedev.partpriceparser.feature_parsers.presentation.screen_content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medvedev.partpriceparser.R
import com.medvedev.partpriceparser.brands.ProductBrand
import com.medvedev.partpriceparser.feature_parsers.presentation.models.filter.BrandFilter
import com.medvedev.partpriceparser.feature_parsers.presentation.models.filter.ProductFilter
import com.medvedev.partpriceparser.feature_parsers.presentation.models.filter.ProductSort
import com.medvedev.partpriceparser.ui.theme.PartPriceParserTheme


@Composable
fun FilterItemButton(modifier: Modifier = Modifier, onChangeDialogState: () -> Unit) {
    Row(
        modifier = modifier
            .padding(end = 10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onChangeDialogState) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_tune_24),
                contentDescription = "изменение фильтров"
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFilterDialog(
    changeDialogState: () -> Unit,
    productFilter: ProductFilter,
    brandListFilter: List<BrandFilter>,
    onCheckedChangeShowMiss: (Boolean) -> Unit,
    onCheckedChangeBrandState: (Boolean, ProductBrand) -> Unit
) {
    AlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(5.dp)
            )
            .wrapContentSize(),
        onDismissRequest = changeDialogState
    ) {
        FilterDialogContent(
            filterList = productFilter.sortListByBrands,
            showMissingItems = productFilter.showMissingItems,
            selectedSort = productFilter.selectedSort,
            onCheckedChangeShowMiss = onCheckedChangeShowMiss,
            brandList = brandListFilter,
            onCheckedChangeBrandState = onCheckedChangeBrandState
        )
    }
}

@Composable
fun FilterDialogContent(
    filterList: List<ProductSort>,
    showMissingItems: Boolean,
    selectedSort: ProductSort,
    onCheckedChangeShowMiss: (Boolean) -> Unit,
    brandList: List<BrandFilter>,
    onCheckedChangeBrandState: (Boolean, ProductBrand) -> Unit
) {
    Column(modifier = Modifier.padding(10.dp)) {
        CheckboxFilter(
            text = "Показывать отсутствующие позиции",
            checked = showMissingItems,
            onCheckedChange = onCheckedChangeShowMiss
        )
        BrandListCheckboxes(
            brandList = brandList,
            brandOnCheckedChange = onCheckedChangeBrandState
        )
        // sort list
        LazyColumn(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(5.dp)
                )
        ) {
            items(filterList) { productFilter ->
                SortItem(
                    filterText = productFilter.filterDescription,
                    selected = (selectedSort == productFilter)
                )
            }
        }
    }
}

@Composable
fun BrandListCheckboxes(
    brandList: List<BrandFilter>,
    brandOnCheckedChange: (Boolean, ProductBrand) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .wrapContentHeight()
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(5.dp)
            )
    ) {
        items(brandList) { brandFilter ->
            CheckboxBrandFilter(
                brandFilter = brandFilter,
                brandOnCheckedChange = brandOnCheckedChange
            )
        }
    }
}


@Composable
fun CheckboxBrandFilter(
    brandFilter: BrandFilter,
    brandOnCheckedChange: (Boolean, ProductBrand) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomCheckbox(
            modifier = Modifier
                .size(30.dp)
                .padding(5.dp),
            checked = brandFilter.brandState,
            onCheckedChange = {
                brandOnCheckedChange(it, brandFilter.brandProduct)
            })
        Text(
            modifier = Modifier,
            text = brandFilter.brandProduct.name
        )
    }
}

@Composable
fun CheckboxFilter(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(4.dp)
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(4.dp),
            style = MaterialTheme.typography.bodyMedium,
            text = text
        )
        Spacer(modifier = Modifier.size(8.dp))
        CustomCheckbox(
            modifier = Modifier
                .padding(end = 10.dp)
                .size(14.dp),
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun CustomCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Checkbox(
        modifier = modifier.clip(shape = RoundedCornerShape(15.dp)),
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            uncheckedColor = MaterialTheme.colorScheme.surfaceTint,
            checkmarkColor = MaterialTheme.colorScheme.surfaceTint,
            disabledCheckedColor = MaterialTheme.colorScheme.secondary,
            disabledUncheckedColor = MaterialTheme.colorScheme.primary,
            disabledIndeterminateColor = MaterialTheme.colorScheme.tertiary
        )
    )
}


@Preview
@Composable
fun PreviewCheckboxNotOutlined() {
    PartPriceParserTheme {
        var rememberChecked = remember { true }
        val onCheckedChange: (Boolean) -> Unit = {
            rememberChecked = it
        }
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            CustomCheckbox(checked = rememberChecked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SortItem(filterText: String, selected: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp)
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = filterText,
                modifier = Modifier.padding(end = 5.dp)
            )
            if (selected) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.baseline_check_24),
                    contentDescription = "selected filter",
                    tint = MaterialTheme.colorScheme.surfaceTint
                )
            }
        }
        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

