package com.example.inventarioapp.feature.product.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query(
        "SELECT * FROM products " +
            "WHERE ownerUid = :ownerUid AND isDeleted = 0 " +
            "ORDER BY updatedAt DESC"
    )
    fun observeVisible(ownerUid: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun find(id: String): ProductEntity?

    @Query("SELECT COUNT(*) FROM products WHERE ownerUid = :ownerUid")
    suspend fun countProducts(ownerUid: String): Int

    @Query("SELECT * FROM products WHERE ownerUid = :ownerUid AND pending = 1")
    suspend fun getPending(ownerUid: String): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity)

    @Query(
        "UPDATE products SET pending = 0 " +
            "WHERE id = :id AND updatedAt = :version"
    )
    suspend fun markSynced(id: String, version: Long)
}
