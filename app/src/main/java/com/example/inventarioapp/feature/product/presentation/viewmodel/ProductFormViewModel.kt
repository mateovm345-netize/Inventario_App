package com.example.inventarioapp.feature.product.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventarioapp.feature.product.domain.model.Product
import com.example.inventarioapp.feature.product.domain.repository.ProductRepository
import com.example.inventarioapp.feature.product.domain.usecase.SaveProductUseCase
import com.example.inventarioapp.feature.product.presentation.state.ProductFormUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProductFormViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val saveProduct: SaveProductUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(ProductFormUiState())
    val ui: StateFlow<ProductFormUiState> = _ui.asStateFlow()

    private var editingId: String? = null
    private var loaded = false

    fun load(id: String?) {
        if (loaded) return
        loaded = true
        editingId = id
        if (id == null) return
        viewModelScope.launch {
            val product = repository.findById(id) ?: return@launch
            _ui.value = ProductFormUiState(
                name = product.name,
                description = product.description,
                price = product.priceCop.toString(),
                quantity = product.quantity.toString()
            )
        }
    }

    fun updateName(value: String) {
        _ui.update { it.copy(name = value, error = null) }
    }

    fun updateDescription(value: String) {
        _ui.update { it.copy(description = value, error = null) }
    }

    fun updatePrice(value: String) {
        _ui.update { it.copy(price = value, error = null) }
    }

    fun updateQuantity(value: String) {
        _ui.update { it.copy(quantity = value, error = null) }
    }

    fun save() = viewModelScope.launch {
        val current = _ui.value
        val price = current.price.trim().toLongOrNull()
        val quantity = current.quantity.trim().toIntOrNull()
        if (price == null || quantity == null) {
            _ui.update { it.copy(error = "Precio y cantidad deben ser numéricos") }
            return@launch
        }
        _ui.update { it.copy(saving = true, error = null) }
        try {
            saveProduct(
                Product(
                    id = editingId ?: UUID.randomUUID().toString(),
                    name = current.name.trim(),
                    description = current.description.trim(),
                    priceCop = price,
                    quantity = quantity
                )
            )
            // Guardado local exitoso en Room incluso sin conexión a Internet.
            _ui.update { it.copy(saved = true, saving = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _ui.update {
                it.copy(
                    saving = false,
                    error = e.message ?: "Error al guardar el producto"
                )
            }
        }
    }
}
