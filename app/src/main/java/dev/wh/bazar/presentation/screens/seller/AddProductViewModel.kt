package dev.wh.bazar.presentation.screens.seller

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.wh.bazar.data.model.Product
import dev.wh.bazar.data.repository.AuthRepository
import dev.wh.bazar.data.repository.ImageUploadRepository
import dev.wh.bazar.data.repository.ProductRepository
import dev.wh.bazar.data.repository.StoreRepository
import dev.wh.bazar.di.RepositoryModule
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddProductUiState(
    val selectedImages: List<Uri> = emptyList(),
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val stock: String = "",
    val category: String = "",
    val nameError: String? = null,
    val priceError: String? = null,
    val stockError: String? = null,
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val errorMessage: String? = null,
    val isProductAdded: Boolean = false
)

class AddProductViewModel(
    application: Application,
    private val imageUploadRepository: ImageUploadRepository = RepositoryModule.provideImageUploadRepository(application),
    private val productRepository: ProductRepository = ProductRepository(),
    private val storeRepository: StoreRepository = StoreRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    fun addImages(uris: List<Uri>) {
        _uiState.update {
            it.copy(selectedImages = it.selectedImages + uris)
        }
    }

    fun removeImage(uri: Uri) {
        _uiState.update {
            it.copy(selectedImages = it.selectedImages.filter { it != uri })
        }
    }

    fun updateName(name: String) {
        _uiState.update {
            it.copy(name = name, nameError = null)
        }
    }

    fun updateDescription(description: String) {
        _uiState.update {
            it.copy(description = description)
        }
    }

    fun updatePrice(price: String) {
        _uiState.update {
            it.copy(price = price, priceError = null)
        }
    }

    fun updateStock(stock: String) {
        _uiState.update {
            it.copy(stock = stock, stockError = null)
        }
    }

    fun updateCategory(category: String) {
        _uiState.update {
            it.copy(category = category)
        }
    }

    fun addProduct() {
        // Validar campos
        if (!validateFields()) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploading = true,
                    errorMessage = null,
                    uploadProgress = 0
                )
            }

            try {
                // Subir imágenes
                val imageUrls = mutableListOf<String>()
                val totalImages = _uiState.value.selectedImages.size

                _uiState.value.selectedImages.forEachIndexed { index, uri ->
                    val result = imageUploadRepository.uploadImage(uri)

                    if (result.isSuccess) {
                        imageUrls.add(result.getOrThrow())
                        val progress = ((index + 1) * 100) / totalImages
                        _uiState.update { it.copy(uploadProgress = progress) }
                    } else {
                        throw result.exceptionOrNull() ?: Exception("Error al subir imagen")
                    }
                }

                // Obtener la tienda del usuario
                val userId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")

                val storeResult = storeRepository.getStoreByOwnerId(userId)
                val store = when (storeResult) {
                    is Resource.Success -> storeResult.data ?: throw Exception("No tienes una tienda creada")
                    is Resource.Error -> throw Exception(storeResult.message ?: "Error al obtener tienda")
                    else -> throw Exception("Error al obtener tienda")
                }

                val product = Product(
                    id = "", // Firestore generará el ID
                    storeId = store.id,
                    storeName = store.name,
                    name = _uiState.value.name,
                    description = _uiState.value.description,
                    price = _uiState.value.price.toDouble(),
                    discountPrice = null,
                    imageUrls = imageUrls,
                    category = _uiState.value.category,
                    stock = _uiState.value.stock.toInt(),
                    rating = 0f,
                    totalRatings = 0,
                    isAvailable = true,
                    tags = emptyList(),
                    createdAt = System.currentTimeMillis()
                )

                // Guardar en Firestore
                when (val result = productRepository.addProduct(product)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                isProductAdded = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = result.message ?: "Error al agregar producto"
                            )
                        }
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = e.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (_uiState.value.name.isBlank()) {
            _uiState.update { it.copy(nameError = "El nombre es requerido") }
            isValid = false
        }

        if (_uiState.value.price.isBlank() || _uiState.value.price.toDoubleOrNull() == null) {
            _uiState.update { it.copy(priceError = "El precio debe ser un número válido") }
            isValid = false
        }

        if (_uiState.value.stock.isBlank() || _uiState.value.stock.toIntOrNull() == null) {
            _uiState.update { it.copy(stockError = "El stock debe ser un número válido") }
            isValid = false
        }

        if (_uiState.value.selectedImages.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Debes agregar al menos una imagen") }
            isValid = false
        }

        return isValid
    }
}
