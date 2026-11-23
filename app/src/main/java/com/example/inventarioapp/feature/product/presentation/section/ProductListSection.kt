package com.example.inventarioapp.feature.product.presentation.section

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.inventarioapp.feature.product.domain.model.Product
import com.example.inventarioapp.feature.product.presentation.composables.ProductCard
import com.example.inventarioapp.feature.product.presentation.state.ProductFilter

/** Bloque funcional de lista: no conoce Room, Firestore ni ViewModel. */
@Composable
fun ProductListSection(
    products: List<Product>,
    allCount: Int,
    totalUnits: Int,
    pendingCount: Int,
    searchQuery: String,
    activeFilter: ProductFilter,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (ProductFilter) -> Unit,
    onEdit: (String) -> Unit,
    onAskDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
        ) {
            InventorySummaryBar(
                totalProducts = allCount,
                totalUnits = totalUnits,
                pendingCount = pendingCount,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(stringResource(R.string.buscar_productos)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = stringResource(R.string.limpiar_busqueda)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("search_products_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeFilter == ProductFilter.ALL,
                    onClick = { onFilterChange(ProductFilter.ALL) },
                    label = { Text(stringResource(R.string.filtro_todos)) },
                    modifier = Modifier.testTag("filter_chip_all")
                )
                FilterChip(
                    selected = activeFilter == ProductFilter.PENDING,
                    onClick = { onFilterChange(ProductFilter.PENDING) },
                    label = { Text(stringResource(R.string.filtro_pendientes)) },
                    modifier = Modifier.testTag("filter_chip_pending")
                )
                FilterChip(
                    selected = activeFilter == ProductFilter.LOW_STOCK,
                    onClick = { onFilterChange(ProductFilter.LOW_STOCK) },
                    label = { Text(stringResource(R.string.filtro_bajo_stock)) },
                    modifier = Modifier.testTag("filter_chip_low_stock")
                )
            }

            if (products.isEmpty()) {
                EmptyInventoryState(
                    hasProductsInInventory = allCount > 0,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onEdit = { onEdit(product.id) },
                            onAskDelete = { onAskDelete(product.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InventorySummaryBar(
    totalProducts: Int,
    totalUnits: Int,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryMetric(
                label = stringResource(R.string.resumen_total_productos),
                value = totalProducts.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                label = stringResource(R.string.resumen_unidades),
                value = totalUnits.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                label = stringResource(R.string.resumen_pendientes),
                value = pendingCount.toString(),
                highlight = pendingCount > 0,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (highlight) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyInventoryState(
    hasProductsInInventory: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(
                imageVector = Icons.Rounded.Inventory2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(20.dp)
                    .size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasProductsInInventory) {
                stringResource(R.string.lista_vacia_filtro)
            } else {
                stringResource(R.string.lista_vacia)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
