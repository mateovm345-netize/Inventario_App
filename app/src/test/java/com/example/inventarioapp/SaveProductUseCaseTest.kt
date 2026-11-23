package com.example.inventarioapp

import com.example.inventarioapp.feature.product.domain.model.Product
import com.example.inventarioapp.feature.product.domain.repository.ProductRepository
import com.example.inventarioapp.feature.product.domain.repository.SyncReport
import com.example.inventarioapp.feature.product.domain.usecase.SaveProductUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeProductRepository : ProductRepository {
    private val items = MutableStateFlow<List<Product>>(emptyList())

    override fun observeProducts(): Flow<List<Product>> = items

    override suspend fun findById(id: String): Product? =
        items.value.firstOrNull { it.id == id }

    override suspend fun save(product: Product) {
        val updated = items.value.filterNot { it.id == product.id } + product
        items.value = updated
    }

    override suspend fun delete(id: String) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun syncNow(): SyncReport = SyncReport(uploaded = 0, downloaded = 0)

    override suspend fun seedInitialProductsIfNeeded() {}
}

class SaveProductUseCaseTest {

    @Test
    fun `valid product is saved via repository`() = runTest {
        val repo = FakeProductRepository()
        val useCase = SaveProductUseCase(repo)
        val product = Product(
            id = "p-1",
            name = "Teclado mecánico",
            description = "Switches táctiles",
            priceCop = 220000L,
            quantity = 12
        )

        useCase(product)

        assertEquals(product, repo.findById("p-1"))
    }

    @Test
    fun `blank name throws IllegalArgumentException`() = runTest {
        val useCase = SaveProductUseCase(FakeProductRepository())
        val result = runCatching {
            useCase(
                Product(
                    id = "p-2",
                    name = "   ",
                    description = "Desc",
                    priceCop = 10000L,
                    quantity = 1
                )
            )
        }
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `non positive price throws IllegalArgumentException`() = runTest {
        val useCase = SaveProductUseCase(FakeProductRepository())
        val result = runCatching {
            useCase(
                Product(
                    id = "p-3",
                    name = "Mouse",
                    description = "Inalámbrico",
                    priceCop = 0L,
                    quantity = 5
                )
            )
        }
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `negative quantity throws IllegalArgumentException`() = runTest {
        val useCase = SaveProductUseCase(FakeProductRepository())
        val result = runCatching {
            useCase(
                Product(
                    id = "p-4",
                    name = "Monitor",
                    description = "27 pulgadas",
                    priceCop = 990000L,
                    quantity = -1
                )
            )
        }
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
