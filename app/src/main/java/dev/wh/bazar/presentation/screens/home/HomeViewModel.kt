package dev.wh.bazar.presentation.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import dev.wh.bazar.data.local.BazarDatabase
import dev.wh.bazar.data.model.Product
import dev.wh.bazar.data.model.UserRole
import dev.wh.bazar.data.repository.AuthRepository
import dev.wh.bazar.data.repository.CartRepository
import dev.wh.bazar.data.repository.ProductRepository
import dev.wh.bazar.data.repository.StoreRepository
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val products: List<Product> = emptyList(),
    val myProducts: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cartItemCount: Int = 0,
    val canSell: Boolean = false,
    val userStoreId: String? = null
)

class HomeViewModel(
    application: Application,
    private val productRepository: ProductRepository = ProductRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val storeRepository: StoreRepository = StoreRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val database = BazarDatabase.getDatabase(application)
    private val cartRepository = CartRepository(database.cartDao())

    init {
        checkUserRole()
        observeCartCount()
    }

    private fun observeCartCount() {
        viewModelScope.launch {
            cartRepository.getCartItemCount().collect { count ->
                _uiState.update { it.copy(cartItemCount = count) }
            }
        }
    }

    private fun checkUserRole() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUserData()) {
                is Resource.Success -> {
                    val user = result.data
                    val canSell = user?.role == UserRole.SELLER ||
                                  user?.role == UserRole.BOTH

                    // Si el usuario puede vender, obtener su tienda
                    var userStoreId: String? = null
                    if (canSell && user != null) {
                        when (val storeResult = storeRepository.getStoreByOwnerId(user.id)) {
                            is Resource.Success -> {
                                userStoreId = storeResult.data?.id
                            }
                            else -> {}
                        }
                    }

                    _uiState.update { it.copy(canSell = canSell, userStoreId = userStoreId) }
                }
                else -> {}
            }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Obtener información del usuario y su tienda
            var myProducts = emptyList<Product>()
            when (val userResult = authRepository.getCurrentUserData()) {
                is Resource.Success -> {
                    val user = userResult.data
                    if (user != null) {
                        val isSeller = user.role == UserRole.SELLER || user.role == UserRole.BOTH
                        if (isSeller) {
                            // Obtener la tienda del vendedor
                            when (val storeResult = storeRepository.getStoreByOwnerId(user.id)) {
                                is Resource.Success -> {
                                    val storeId = storeResult.data?.id
                                    if (storeId != null) {
                                        // Cargar productos del vendedor
                                        when (val myProductsResult = productRepository.getProductsByStore(storeId)) {
                                            is Resource.Success -> {
                                                myProducts = myProductsResult.data ?: emptyList()
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
                else -> {}
            }

            // Cargar todos los productos recomendados
            when (val result = productRepository.getRecommendedProducts(limit = 50)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            products = result.data ?: emptyList(),
                            myProducts = myProducts,
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }

}
