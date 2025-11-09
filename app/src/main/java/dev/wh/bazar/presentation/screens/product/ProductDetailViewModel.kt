package dev.wh.bazar.presentation.screens.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import dev.wh.bazar.data.local.BazarDatabase
import dev.wh.bazar.data.model.CartItem
import dev.wh.bazar.data.model.Product
import dev.wh.bazar.data.repository.CartRepository
import dev.wh.bazar.data.repository.ProductRepository
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAddedToCartMessage: Boolean = false
)

class ProductDetailViewModel(
    application: Application,
    private val productRepository: ProductRepository = ProductRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val database = BazarDatabase.getDatabase(application)
    private val cartRepository = CartRepository(database.cartDao())

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                when (val result = productRepository.getProductById(productId)) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _uiState.update {
                                it.copy(
                                    product = result.data,
                                    isLoading = false
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "El producto no existe o fue eliminado"
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message ?: "Error desconocido al cargar el producto"
                            )
                        }
                    }
                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error inesperado"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun addToCart(quantity: Int) {
        viewModelScope.launch {
            try {
                val product = _uiState.value.product ?: return@launch

                // Validar que el producto tenga stock disponible
                if (product.stock <= 0) {
                    _uiState.update {
                        it.copy(errorMessage = "Producto sin stock disponible")
                    }
                    return@launch
                }

                // Validar que la cantidad solicitada no exceda el stock
                val validQuantity = if (quantity > product.stock) {
                    product.stock
                } else {
                    quantity
                }

                val cartItem = CartItem(
                    productId = product.id,
                    storeId = product.storeId,
                    storeName = product.storeName,
                    productName = product.name,
                    productImage = product.imageUrls.firstOrNull() ?: "",
                    price = product.finalPrice,
                    quantity = validQuantity,
                    stock = product.stock
                )

                cartRepository.addToCart(cartItem)
                _uiState.update { it.copy(showAddedToCartMessage = true) }

                delay(2000)
                _uiState.update { it.copy(showAddedToCartMessage = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al agregar al carrito: ${e.message}")
                }
            }
        }
    }

}
