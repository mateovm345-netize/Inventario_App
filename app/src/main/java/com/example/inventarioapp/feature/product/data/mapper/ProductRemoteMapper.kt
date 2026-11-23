package com.example.inventarioapp.feature.product.data.mapper

import com.example.inventarioapp.feature.product.data.local.ProductEntity
import com.example.inventarioapp.feature.product.data.remote.ProductDto

fun ProductEntity.toDto() = ProductDto(
    id = id,
    name = name,
    description = description,
    priceCop = priceCop,
    quantity = quantity,
    updatedAt = updatedAt,
    isDeleted = isDeleted
)

fun ProductDto.toEntity(ownerUid: String) = ProductEntity(
    id = id,
    ownerUid = ownerUid,
    name = name,
    description = description,
    priceCop = priceCop,
    quantity = quantity,
    updatedAt = updatedAt,
    pending = false,
    isDeleted = isDeleted
)
