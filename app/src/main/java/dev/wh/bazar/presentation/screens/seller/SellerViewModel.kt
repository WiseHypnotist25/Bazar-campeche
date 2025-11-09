package dev.wh.bazar.presentation.screens.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.wh.bazar.data.model.Product
import dev.wh.bazar.data.model.Store
import dev.wh.bazar.data.repository.AuthRepository
import dev.wh.bazar.data.repository.ProductRepository
import dev.wh.bazar.data.repository.StoreRepository
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SellerUiState(
    val store: Store? = null,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SellerViewModel(
    private val productRepository: ProductRepository = ProductRepository(),
    private val storeRepository: StoreRepository = StoreRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SellerUiState())
    val uiState: StateFlow<SellerUiState> = _uiState.asStateFlow()

    fun loadStoreAndProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Get current user's store
            val userId = authRepository.currentUser?.uid ?: return@launch

            // Get store by owner ID
            when (val storeResult = storeRepository.getStoreByOwnerId(userId)) {
                is Resource.Success -> {
                    val store = storeResult.data
                    if (store == null) {
                        _uiState.update {
                            it.copy(
                                store = null,
                                isLoading = false,
                                errorMessage = "No tienes una tienda creada"
                            )
                        }
                        return@launch
                    }

                    _uiState.update { it.copy(store = store) }

                    // Load products for this store
                    when (val productsResult = productRepository.getProductsByStore(store.id)) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    products = productsResult.data ?: emptyList(),
                                    isLoading = false
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = productsResult.message
                                )
                            }
                        }
                        else -> {}
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = storeResult.message
                        )
                    }
                }
                else -> {}
            }
        }
    }
}
