package dev.wh.bazar.presentation.screens.seller

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.wh.bazar.data.model.Store
import dev.wh.bazar.data.repository.AuthRepository
import dev.wh.bazar.data.repository.ImageUploadRepository
import dev.wh.bazar.data.repository.StoreRepository
import dev.wh.bazar.di.RepositoryModule
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateStoreUiState(
    val logoUri: Uri? = null,
    val bannerUri: Uri? = null,
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val nameError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
    val isStoreCreated: Boolean = false
)

class CreateStoreViewModel(
    application: Application,
    private val imageUploadRepository: ImageUploadRepository = RepositoryModule.provideImageUploadRepository(application),
    private val storeRepository: StoreRepository = StoreRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CreateStoreUiState())
    val uiState: StateFlow<CreateStoreUiState> = _uiState.asStateFlow()

    fun setLogoUri(uri: Uri) {
        _uiState.update { it.copy(logoUri = uri) }
    }

    fun setBannerUri(uri: Uri) {
        _uiState.update { it.copy(bannerUri = uri) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description, descriptionError = null) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category, categoryError = null) }
    }

    fun createStore() {
        // Validar campos
        if (!validateFields()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }

            try {
                // Verificar si el usuario ya tiene una tienda
                val userId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")

                when (val existingStoreResult = storeRepository.getStoreByOwnerId(userId)) {
                    is Resource.Success -> {
                        if (existingStoreResult.data != null) {
                            _uiState.update {
                                it.copy(
                                    isUploading = false,
                                    errorMessage = "Ya tienes una tienda creada"
                                )
                            }
                            return@launch
                        }
                    }
                    is Resource.Error -> {
                        // Continuar si no se encontró tienda
                    }
                    else -> {}
                }

                // Subir logo
                var logoUrl = ""
                if (_uiState.value.logoUri != null) {
                    val logoResult = imageUploadRepository.uploadImage(_uiState.value.logoUri!!)
                    if (logoResult.isSuccess) {
                        logoUrl = logoResult.getOrThrow()
                    } else {
                        throw logoResult.exceptionOrNull() ?: Exception("Error al subir el logo")
                    }
                }

                // Subir banner (opcional)
                var bannerUrl = ""
                if (_uiState.value.bannerUri != null) {
                    val bannerResult = imageUploadRepository.uploadImage(_uiState.value.bannerUri!!)
                    if (bannerResult.isSuccess) {
                        bannerUrl = bannerResult.getOrThrow()
                    }
                    // No lanzamos error si falla el banner ya que es opcional
                }

                // Crear tienda
                val store = Store(
                    id = "", // Firestore generará el ID
                    ownerId = userId,
                    name = _uiState.value.name,
                    description = _uiState.value.description,
                    category = _uiState.value.category,
                    logoUrl = logoUrl,
                    bannerUrl = bannerUrl,
                    rating = 0f,
                    totalRatings = 0,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )

                // Guardar en Firestore
                when (val result = storeRepository.createStore(store)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                isStoreCreated = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = result.message ?: "Error al crear la tienda"
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

        if (_uiState.value.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "La descripción es requerida") }
            isValid = false
        }

        if (_uiState.value.category.isBlank()) {
            _uiState.update { it.copy(categoryError = "La categoría es requerida") }
            isValid = false
        }

        if (_uiState.value.logoUri == null) {
            _uiState.update { it.copy(errorMessage = "Debes agregar un logo para tu tienda") }
            isValid = false
        }

        return isValid
    }
}
