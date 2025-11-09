package dev.wh.bazar.presentation.screens.seller

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.wh.bazar.data.model.Product
import dev.wh.bazar.data.repository.ImageUploadRepository
import dev.wh.bazar.data.repository.ProductRepository
import dev.wh.bazar.di.RepositoryModule
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProductUiState(
    val product: Product? = null,
    val selectedImages: List<Uri> = emptyList(),
    val existingImageUrls: List<String> = emptyList(),
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val stock: String = "",
    val category: String = "",
    val nameError: String? = null,
    val priceError: String? = null,
    val stockError: String? = null,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val errorMessage: String? = null,
    val isProductUpdated: Boolean = false
)

class EditProductViewModel(
    application: Application,
    private val productId: String
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditProductUiState())
    val uiState: StateFlow<EditProductUiState> = _uiState.asStateFlow()

    private val productRepository = ProductRepository()
    private val imageUploadRepository = RepositoryModule.provideImageUploadRepository(application)

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = productRepository.getProductById(productId)) {
                is Resource.Success -> {
                    result.data?.let { product ->
                        _uiState.update {
                            it.copy(
                                product = product,
                                existingImageUrls = product.imageUrls,
                                name = product.name,
                                description = product.description,
                                price = product.price.toString(),
                                stock = product.stock.toString(),
                                category = product.category,
                                isLoading = false
                            )
                        }
                    } ?: run {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Producto no encontrado"
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Error al cargar el producto"
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun addImages(uris: List<Uri>) {
        _uiState.update {
            val currentImages = it.selectedImages.toMutableList()
            val availableSlots = 5 - (it.existingImageUrls.size + currentImages.size)
            val imagesToAdd = uris.take(availableSlots)
            it.copy(selectedImages = currentImages + imagesToAdd)
        }
    }

    fun removeNewImage(uri: Uri) {
        _uiState.update {
            it.copy(selectedImages = it.selectedImages.filter { img -> img != uri })
        }
    }

    fun removeExistingImage(url: String) {
        _uiState.update {
            it.copy(existingImageUrls = it.existingImageUrls.filter { img -> img != url })
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updatePrice(price: String) {
        _uiState.update { it.copy(price = price, priceError = null) }
    }

    fun updateStock(stock: String) {
        _uiState.update { it.copy(stock = stock, stockError = null) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateProduct() {
        viewModelScope.launch {
            // Validaciones
            var hasError = false

            if (_uiState.value.name.isBlank()) {
                _uiState.update { it.copy(nameError = "El nombre es requerido") }
                hasError = true
            }

            val priceValue = _uiState.value.price.toDoubleOrNull()
            if (priceValue == null || priceValue <= 0) {
                _uiState.update { it.copy(priceError = "Ingresa un precio válido") }
                hasError = true
            }

            val stockValue = _uiState.value.stock.toIntOrNull()
            if (stockValue == null || stockValue < 0) {
                _uiState.update { it.copy(stockError = "Ingresa un stock válido") }
                hasError = true
            }

            val totalImages = _uiState.value.existingImageUrls.size + _uiState.value.selectedImages.size
            if (totalImages == 0) {
                _uiState.update { it.copy(errorMessage = "Debes tener al menos una imagen") }
                hasError = true
            }

            if (hasError) return@launch

            _uiState.update { it.copy(isUploading = true, errorMessage = null) }

            try {
                val product = _uiState.value.product ?: return@launch

                // Subir nuevas imágenes
                val uploadedUrls = mutableListOf<String>()
                _uiState.value.selectedImages.forEachIndexed { index, uri ->
                    val progress = ((index + 1) * 100) / _uiState.value.selectedImages.size
                    _uiState.update { it.copy(uploadProgress = progress) }

                    val result = imageUploadRepository.uploadImage(uri)
                    if (result.isSuccess) {
                        uploadedUrls.add(result.getOrThrow())
                    } else {
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = "Error al subir imagen: ${result.exceptionOrNull()?.message}"
                            )
                        }
                        return@launch
                    }
                }

                // Combinar URLs existentes con las nuevas
                val allImageUrls = _uiState.value.existingImageUrls + uploadedUrls

                // Actualizar producto
                val updatedProduct = product.copy(
                    name = _uiState.value.name,
                    description = _uiState.value.description,
                    price = priceValue!!,
                    stock = stockValue!!,
                    category = _uiState.value.category,
                    imageUrls = allImageUrls
                )

                when (productRepository.updateProduct(productId, updatedProduct)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                isProductUpdated = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = "Error al actualizar el producto"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (productRepository.deleteProduct(productId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isProductUpdated = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al eliminar el producto"
                        )
                    }
                }
                else -> {}
            }
        }
    }
}
