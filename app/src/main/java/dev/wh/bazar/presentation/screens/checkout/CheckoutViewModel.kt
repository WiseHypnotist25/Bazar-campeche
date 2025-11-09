package dev.wh.bazar.presentation.screens.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dev.wh.bazar.data.local.BazarDatabase
import dev.wh.bazar.data.model.Order
import dev.wh.bazar.data.model.OrderItem
import dev.wh.bazar.data.model.OrderStatus
import dev.wh.bazar.data.repository.CartRepository
import dev.wh.bazar.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val shippingAddress: String = "",
    val paymentMethod: String = "Efectivo",
    val availablePaymentMethods: List<String> = listOf("Efectivo", "Tarjeta", "Transferencia"),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val orderCreated: Boolean = false
)

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val database = BazarDatabase.getDatabase(application)
    private val cartRepository = CartRepository(database.cartDao())
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadCartTotal()
    }

    private fun loadCartTotal() {
        viewModelScope.launch {
            try {
                val cartItems = cartRepository.getAllCartItems().first()
                val total = cartItems.sumOf { it.totalPrice }
                _uiState.update { it.copy(totalAmount = total) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al cargar el total: ${e.message}") }
            }
        }
    }

    fun updateShippingAddress(address: String) {
        _uiState.update { it.copy(shippingAddress = address) }
    }

    fun updatePaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun createOrder() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Usuario no autenticado"
                        )
                    }
                    return@launch
                }

                // Validar dirección
                if (_uiState.value.shippingAddress.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Por favor ingresa una dirección de envío"
                        )
                    }
                    return@launch
                }

                // Obtener items del carrito
                val cartItems = cartRepository.getAllCartItems().first()
                if (cartItems.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "El carrito está vacío"
                        )
                    }
                    return@launch
                }

                // Agrupar por tienda
                val itemsByStore = cartItems.groupBy { it.storeId }

                // Crear una orden por cada tienda
                itemsByStore.forEach { (storeId, items) ->
                    val orderItems = items.map { cartItem ->
                        OrderItem(
                            productId = cartItem.productId,
                            productName = cartItem.productName,
                            productImage = cartItem.productImage,
                            price = cartItem.price,
                            quantity = cartItem.quantity
                        )
                    }

                    val totalAmount = orderItems.sumOf { it.price * it.quantity }

                    val order = Order(
                        id = "", // Firestore generará el ID
                        userId = userId,
                        storeId = storeId,
                        storeName = items.first().storeName,
                        items = orderItems,
                        totalAmount = totalAmount,
                        status = OrderStatus.PENDING,
                        shippingAddress = _uiState.value.shippingAddress,
                        paymentMethod = _uiState.value.paymentMethod,
                        paymentId = "",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    orderRepository.createOrder(order)
                }

                // Limpiar carrito después de crear las órdenes
                cartRepository.clearCart()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        orderCreated = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al crear la orden: ${e.message}"
                    )
                }
            }
        }
    }
}
