package com.example.inventarioapp.feature.product.data.remote

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/** Representación del documento Firestore; sin anotaciones Room. */
@Keep
@IgnoreExtraProperties
data class ProductDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val priceCop: Long = 0L,
    val quantity: Int = 0,
    val updatedAt: Long = 0L,
    @get:PropertyName("isDeleted")
    @PropertyName("isDeleted")
    val isDeleted: Boolean = false
)
