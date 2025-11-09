package dev.wh.bazar.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: String,
    val storeId: String,
    val storeName: String,
    val productName: String,
    val productImage: String,
    val price: Double,
    val quantity: Int,
    val stock: Int
) {
    val totalPrice: Double
        get() = price * quantity
}
