package dev.wh.bazar.presentation.screens.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

data class ProfileUiState(
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userProfileImage: String = "",
    val hasStore: Boolean = false,
    val storeName: String = "",
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isUploadingImage: Boolean = false,
    val isSendingPasswordReset: Boolean = false,
    val passwordResetSent: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository(),
    private val storeRepository: StoreRepository = StoreRepository(),
    private val imageUploadRepository: ImageUploadRepository = RepositoryModule.provideImageUploadRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser

            if (currentUser != null) {
                _uiState.update {
                    it.copy(
                        userName = currentUser.displayName ?: "Usuario",
                        userEmail = currentUser.email ?: "",
                        userPhone = currentUser.phoneNumber ?: ""
                    )
                }

                // Check if user has a store
                when (val storeResult = storeRepository.getStoreByOwnerId(currentUser.uid)) {
                    is Resource.Success -> {
                        val store = storeResult.data
                        _uiState.update {
                            it.copy(
                                hasStore = store != null,
                                storeName = store?.name ?: ""
                            )
                        }
                    }
                    else -> {
                        _uiState.update {
                            it.copy(hasStore = false)
                        }
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }

            authRepository.signOut()

            _uiState.update {
                it.copy(
                    isLoggingOut = false,
                    isLoggedOut = true
                )
            }
        }
    }

    fun updateProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploadingImage = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                val currentUserId = authRepository.currentUser?.uid
                    ?: throw Exception("Usuario no autenticado")

                // Subir imagen a imgbb
                val uploadResult = imageUploadRepository.uploadImage(imageUri)
                if (uploadResult.isFailure) {
                    throw uploadResult.exceptionOrNull() ?: Exception("Error al subir imagen")
                }

                val imageUrl = uploadResult.getOrThrow()

                // Actualizar en Firestore
                when (val result = authRepository.updateUserProfileImage(currentUserId, imageUrl)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                userProfileImage = imageUrl,
                                isUploadingImage = false,
                                successMessage = "Foto de perfil actualizada"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isUploadingImage = false,
                                errorMessage = result.message ?: "Error al actualizar foto"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploadingImage = false,
                        errorMessage = e.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    fun requestPasswordReset() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingPasswordReset = true,
                    errorMessage = null,
                    successMessage = null,
                    passwordResetSent = false
                )
            }

            val email = _uiState.value.userEmail
            if (email.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isSendingPasswordReset = false,
                        errorMessage = "No se encontró el email del usuario"
                    )
                }
                return@launch
            }

            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSendingPasswordReset = false,
                            passwordResetSent = true,
                            successMessage = "Email de recuperación enviado a $email"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSendingPasswordReset = false,
                            errorMessage = result.message ?: "Error al enviar email"
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null,
                passwordResetSent = false
            )
        }
    }
}
