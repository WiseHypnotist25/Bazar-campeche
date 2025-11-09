package dev.wh.bazar.presentation.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.wh.bazar.data.model.Order
import dev.wh.bazar.data.repository.AuthRepository
import dev.wh.bazar.data.repository.OrderRepository
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class OrdersViewModel(
    private val orderRepository: OrderRepository = OrderRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = orderRepository.getUserOrders(userId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            orders = result.data ?: emptyList(),
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
