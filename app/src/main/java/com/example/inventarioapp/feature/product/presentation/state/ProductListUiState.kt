package com.example.inventarioapp.feature.product.presentation.state

import com.example.inventarioapp.feature.product.domain.model.Product

enum class ProductFilter {
    ALL,
    PENDING,
    LOW_STOCK
}

/** Datos que ProductListScreen necesita renderizar. */
data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val filter: ProductFilter = ProductFilter.ALL,
    val isSyncing: Boolean = false,
    val message: String? = null,
    val isErrorMessage: Boolean = false
) {
    val filteredProducts: List<Product>
        get() {
            val query = searchQuery.trim().lowercase()
            return products.filter { product ->
                val matchesQuery = query.isEmpty() ||
                    product.name.lowercase().contains(query) ||
                    product.description.lowercase().contains(query)
                val matchesFilter = when (filter) {
                    ProductFilter.ALL -> true
                    ProductFilter.PENDING -> product.pendingSync
                    ProductFilter.LOW_STOCK -> product.quantity <= 5
                }
                matchesQuery && matchesFilter
            }
        }

    val totalUnits: Int
        get() = products.sumOf { it.quantity }

    val pendingCount: Int
        get() = products.count { it.pendingSync }
}
