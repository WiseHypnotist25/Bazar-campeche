package dev.wh.bazar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val logoUrl: String = "",
    val bannerUrl: String = "",
    val rating: Float = 0f,
    val totalRatings: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
