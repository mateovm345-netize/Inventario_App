package com.example.inventarioapp.feature.product.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventarioapp.feature.product.domain.repository.ProductRepository
import com.example.inventarioapp.feature.product.presentation.state.ProductFilter
import com.example.inventarioapp.feature.product.presentation.state.ProductListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProductListUiState())
    val ui: StateFlow<ProductListUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialProductsIfNeeded()
            repository.observeProducts()
                .catch { error ->
                    if (error is CancellationException) throw error
                    _ui.update {
                        it.copy(
                            message = error.message ?: "Error al observar inventario local",
                            isErrorMessage = true
                        )
                    }
                }
                .collect { products ->
                    _ui.update { it.copy(products = products) }
                }
        }
        sync()
    }

    fun updateSearchQuery(query: String) {
        _ui.update { it.copy(searchQuery = query) }
    }

    fun updateFilter(filter: ProductFilter) {
        _ui.update { it.copy(filter = filter) }
    }

    fun clearMessage() {
        _ui.update { it.copy(message = null, isErrorMessage = false) }
    }

    fun sync() = viewModelScope.launch {
        _ui.update { it.copy(isSyncing = true, message = null, isErrorMessage = false) }
        try {
            val report = repository.syncNow()
            _ui.update {
                it.copy(
                    message = "Sincronizado: ${report.uploaded} enviados, ${report.downloaded} en la nube",
                    isErrorMessage = false
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _ui.update {
                it.copy(
                    message = "Sin conexión o sesión expirada: datos locales disponibles",
                    isErrorMessage = true
                )
            }
        } finally {
            _ui.update { it.copy(isSyncing = false) }
        }
    }

    fun delete(id: String) = viewModelScope.launch {
        repository.delete(id)
        sync()
    }
}
