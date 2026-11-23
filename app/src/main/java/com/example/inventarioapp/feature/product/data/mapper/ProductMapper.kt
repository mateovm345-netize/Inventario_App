package com.example.inventarioapp.feature.product.data.mapper

import com.example.inventarioapp.feature.product.data.local.ProductEntity
import com.example.inventarioapp.feature.product.domain.model.Product

fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
    description = description,
    priceCop = priceCop,
    quantity = quantity,
    updatedAt = updatedAt,
    pendingSync = pending
)

fun Product.toEntity(ownerUid: String) = ProductEntity(
    id = id,
    ownerUid = ownerUid,
    name = name,
    description = description,
    priceCop = priceCop,
    quantity = quantity,
    updatedAt = updatedAt,
    pending = true,
    isDeleted = false
)
