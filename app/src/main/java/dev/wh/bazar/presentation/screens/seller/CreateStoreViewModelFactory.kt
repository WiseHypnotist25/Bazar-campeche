package dev.wh.bazar.presentation.screens.seller

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CreateStoreViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateStoreViewModel::class.java)) {
            return CreateStoreViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
