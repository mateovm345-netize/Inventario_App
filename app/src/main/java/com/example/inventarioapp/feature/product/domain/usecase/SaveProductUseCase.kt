package com.example.inventarioapp.feature.product.domain.usecase

import com.example.inventarioapp.feature.product.domain.model.Product
import com.example.inventarioapp.feature.product.domain.repository.ProductRepository
import javax.inject.Inject

class SaveProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product) {
        require(product.name.isNotBlank()) { "El nombre es obligatorio" }
        require(product.name.length <= 80) { "El nombre admite máximo 80 caracteres" }
        require(product.description.length <= 500) { "La descripción admite máximo 500 caracteres" }
        require(product.priceCop > 0) { "El precio en COP debe ser mayor a 0" }
        require(product.quantity >= 0) { "La cantidad no puede ser negativa" }
        repository.save(product)
    }
}
