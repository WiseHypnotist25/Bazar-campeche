package dev.wh.bazar.data.repository

import dev.wh.bazar.data.local.CartDao
import dev.wh.bazar.data.model.CartItem
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {

    fun getAllCartItems(): Flow<List<CartItem>> {
        return cartDao.getAllCartItems()
    }

    fun getCartItemCount(): Flow<Int> {
        return cartDao.getCartItemCount()
    }

    suspend fun addToCart(cartItem: CartItem) {
        val existingItem = cartDao.getCartItem(cartItem.productId)
        if (existingItem != null) {
            // Validar que la cantidad total no exceda el stock disponible
            val newQuantity = existingItem.quantity + cartItem.quantity
            val finalQuantity = if (newQuantity > cartItem.stock) {
                cartItem.stock
            } else {
                newQuantity
            }
            val updatedItem = existingItem.copy(
                quantity = finalQuantity,
                stock = cartItem.stock // Actualizar stock disponible
            )
            cartDao.updateCartItem(updatedItem)
        } else {
            // Validar que la cantidad inicial no exceda el stock
            val validatedItem = if (cartItem.quantity > cartItem.stock) {
                cartItem.copy(quantity = cartItem.stock)
            } else {
                cartItem
            }
            cartDao.insertCartItem(validatedItem)
        }
    }

    suspend fun updateCartItem(cartItem: CartItem) {
        cartDao.updateCartItem(cartItem)
    }

    suspend fun removeFromCart(cartItem: CartItem) {
        cartDao.deleteCartItem(cartItem)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }
}
