package dev.wh.bazar.presentation.screens.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.wh.bazar.data.local.BazarDatabase
import dev.wh.bazar.data.model.CartItem
import dev.wh.bazar.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val errorMessage: String? = null
)

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val database = BazarDatabase.getDatabase(application)
    private val cartRepository = CartRepository(database.cartDao())

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            try {
                cartRepository.getAllCartItems().collect { items ->
                    _uiState.update {
                        it.copy(
                            cartItems = items,
                            totalPrice = items.sumOf { item -> item.totalPrice },
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al cargar el carrito: ${e.message}")
                }
            }
        }
    }

    fun incrementQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            try {
                if (cartItem.quantity < cartItem.stock) {
                    val updatedItem = cartItem.copy(quantity = cartItem.quantity + 1)
                    cartRepository.updateCartItem(updatedItem)
                } else {
                    _uiState.update {
                        it.copy(errorMessage = "No hay más stock disponible")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al actualizar cantidad: ${e.message}")
                }
            }
        }
    }

    fun decrementQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            try {
                if (cartItem.quantity > 1) {
                    val updatedItem = cartItem.copy(quantity = cartItem.quantity - 1)
                    cartRepository.updateCartItem(updatedItem)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al actualizar cantidad: ${e.message}")
                }
            }
        }
    }

    fun removeItem(cartItem: CartItem) {
        viewModelScope.launch {
            try {
                cartRepository.removeFromCart(cartItem)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al eliminar producto: ${e.message}")
                }
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                cartRepository.clearCart()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al limpiar el carrito: ${e.message}")
                }
            }
        }
    }
}
