package dev.wh.bazar.data.model

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discountPrice: Double? = null,
    val category: String = "",
    val imageUrls: List<String> = emptyList(),
    val stock: Int = 0,
    @get:PropertyName("available")
    @set:PropertyName("available")
    var isAvailable: Boolean = true,
    val rating: Float = 0f,
    val totalRatings: Int = 0,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val finalPrice: Double
        get() = discountPrice ?: price

    val hasDiscount: Boolean
        get() = discountPrice != null && discountPrice < price
}
