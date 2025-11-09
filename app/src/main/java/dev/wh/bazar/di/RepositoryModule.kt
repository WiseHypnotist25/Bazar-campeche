package dev.wh.bazar.di

import android.content.Context
import dev.wh.bazar.data.repository.ImageUploadRepository

object RepositoryModule {

    private var imageUploadRepository: ImageUploadRepository? = null

    fun provideImageUploadRepository(context: Context): ImageUploadRepository {
        return imageUploadRepository ?: synchronized(this) {
            imageUploadRepository ?: ImageUploadRepository(
                NetworkModule.imgbbApiService,
                context.applicationContext
            ).also {
                imageUploadRepository = it
            }
        }
    }
}
