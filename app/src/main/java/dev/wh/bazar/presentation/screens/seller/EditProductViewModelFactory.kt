package dev.wh.bazar.presentation.screens.seller

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EditProductViewModelFactory(
    private val application: Application,
    private val productId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditProductViewModel(application, productId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
