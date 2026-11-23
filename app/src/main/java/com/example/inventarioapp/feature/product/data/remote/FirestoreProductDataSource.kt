package com.example.inventarioapp.feature.product.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Única clase que conoce las rutas de la colección de Firestore. */
class FirestoreProductDataSource @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun products(uid: String) =
        db.collection("users").document(uid).collection("products")

    suspend fun push(uid: String, dto: ProductDto) {
        val path = "users/$uid/products/${dto.id}"
        try {
            val data = mapOf(
                "id" to dto.id,
                "name" to dto.name,
                "description" to dto.description,
                "priceCop" to dto.priceCop,
                "quantity" to dto.quantity,
                "updatedAt" to FieldValue.serverTimestamp(),
                "isDeleted" to dto.isDeleted
            )
            products(uid).document(dto.id).set(data).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun fetchFromServer(uid: String): List<ProductDto> {
        val path = "users/$uid/products"
        return try {
            products(uid).get(Source.SERVER).await().documents.mapNotNull { doc ->
                val id = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
                val serverMillis = doc.getTimestamp(
                    "updatedAt",
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )?.toDate()?.time ?: 0L
                ProductDto(
                    id = id,
                    name = doc.getString("name").orEmpty(),
                    description = doc.getString("description").orEmpty(),
                    priceCop = doc.getLong("priceCop") ?: 0L,
                    quantity = (doc.getLong("quantity") ?: 0L).toInt(),
                    updatedAt = serverMillis,
                    isDeleted = doc.getBoolean("isDeleted") ?: false
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }
}
