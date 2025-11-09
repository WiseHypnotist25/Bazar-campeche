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
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cartItemCount: Int = 0,
    val canSell: Boolean = false
)

class HomeViewModel(
    application: Application,
    private val productRepository: ProductRepository = ProductRepository(),
    private val authRepository: AuthRepository = AuthRepository()
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
                    val canSell = result.data?.role == UserRole.SELLER ||
                                  result.data?.role == UserRole.BOTH
                    _uiState.update { it.copy(canSell = canSell) }
                }
                else -> {}
            }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = productRepository.getRecommendedProducts()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            products = result.data ?: emptyList(),
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
