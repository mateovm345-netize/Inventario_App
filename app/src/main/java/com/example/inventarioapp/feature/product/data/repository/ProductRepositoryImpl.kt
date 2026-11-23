package com.example.inventarioapp.feature.product.data.repository

import com.example.inventarioapp.feature.product.data.local.ProductDao
import com.example.inventarioapp.feature.product.data.mapper.toDomain
import com.example.inventarioapp.feature.product.data.mapper.toDto
import com.example.inventarioapp.feature.product.data.mapper.toEntity
import com.example.inventarioapp.feature.product.data.remote.FirestoreProductDataSource
import com.example.inventarioapp.feature.product.domain.model.Product
import com.example.inventarioapp.feature.product.domain.repository.ProductRepository
import com.example.inventarioapp.feature.product.domain.repository.SyncReport
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dao: ProductDao,
    private val remote: FirestoreProductDataSource,
    private val auth: FirebaseAuth
) : ProductRepository {

    private val syncMutex = Mutex()

    private fun currentUserId(): String =
        auth.currentUser?.uid ?: "offline_guest_user"

    override fun observeProducts(): Flow<List<Product>> {
        val uid = currentUserId()
        return dao.observeVisible(uid).map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun findById(id: String): Product? {
        val uid = currentUserId()
        return dao.find(id)
            ?.takeIf { it.ownerUid == uid && !it.isDeleted }
            ?.toDomain()
    }

    override suspend fun save(product: Product) {
        val uid = currentUserId()
        dao.upsert(
            product.copy(updatedAt = System.currentTimeMillis()).toEntity(uid)
        )
    }

    override suspend fun delete(id: String) {
        val uid = currentUserId()
        val current = dao.find(id)?.takeIf { it.ownerUid == uid } ?: return
        dao.upsert(
            current.copy(
                isDeleted = true,
                pending = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun syncNow(): SyncReport = syncMutex.withLock {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("Inicia sesión con Google para sincronizar tus productos en la nube.")
        val uid = currentUser.uid

        var uploaded = 0
        for (local in dao.getPending(uid)) {
            remote.push(uid, local.toDto())
            dao.markSynced(local.id, local.updatedAt)
            uploaded++
        }

        val cloudItems = remote.fetchFromServer(uid)
        for (dto in cloudItems) {
            val cloud = dto.toEntity(uid)
            val local = dao.find(cloud.id)
            if (local?.pending == true) continue
            if (local == null || cloud.updatedAt > local.updatedAt) {
                dao.upsert(cloud)
            }
        }
        SyncReport(uploaded = uploaded, downloaded = cloudItems.size)
    }

    override suspend fun seedInitialProductsIfNeeded() {
        val uid = currentUserId()
        if (dao.countProducts(uid) == 0) {
            val now = System.currentTimeMillis()
            val initial = listOf(
                com.example.inventarioapp.feature.product.data.local.ProductEntity(
                    id = "$uid-prod-1",
                    ownerUid = uid,
                    name = "Laptop HP ProBook",
                    description = "Core i5 11va Gen, 16GB RAM, 512GB SSD",
                    priceCop = 2850000L,
                    quantity = 8,
                    updatedAt = now,
                    pending = true,
                    isDeleted = false
                ),
                com.example.inventarioapp.feature.product.data.local.ProductEntity(
                    id = "$uid-prod-2",
                    ownerUid = uid,
                    name = "Teclado Mecánico RGB",
                    description = "Switches Red, formato TKL en español",
                    priceCop = 240000L,
                    quantity = 15,
                    updatedAt = now,
                    pending = true,
                    isDeleted = false
                ),
                com.example.inventarioapp.feature.product.data.local.ProductEntity(
                    id = "$uid-prod-3",
                    ownerUid = uid,
                    name = "Monitor LG 27 pulgadas",
                    description = "Resolución Full HD IPS, 75Hz",
                    priceCop = 780000L,
                    quantity = 5,
                    updatedAt = now,
                    pending = true,
                    isDeleted = false
                ),
                com.example.inventarioapp.feature.product.data.local.ProductEntity(
                    id = "$uid-prod-4",
                    ownerUid = uid,
                    name = "Mouse Inalámbrico Ergonómico",
                    description = "Sensor óptico de alta precisión con USB",
                    priceCop = 95000L,
                    quantity = 25,
                    updatedAt = now,
                    pending = true,
                    isDeleted = false
                ),
                com.example.inventarioapp.feature.product.data.local.ProductEntity(
                    id = "$uid-prod-5",
                    ownerUid = uid,
                    name = "Silla Ergonómica de Oficina",
                    description = "Soporte lumbar ajustable y reclinable",
                    priceCop = 550000L,
                    quantity = 4,
                    updatedAt = now,
                    pending = true,
                    isDeleted = false
                )
            )
            initial.forEach { dao.upsert(it) }
        }
    }
}
