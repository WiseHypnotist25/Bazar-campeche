package dev.wh.bazar.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dev.wh.bazar.data.remote.ImgbbApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ImageUploadRepository(
    private val imgbbApiService: ImgbbApiService,
    private val context: Context
) {
    // API Key gratuita de imgbb (puedes crear tu propia cuenta en https://api.imgbb.com/)
    // Esta es una key de ejemplo, deberías obtener la tuya propia
    private val API_KEY = "4b7b9e596fd8a55401a0d13c104c3c60"

    /**
     * Sube una imagen desde un URI y retorna la URL pública
     */
    suspend fun uploadImage(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Leer y comprimir la imagen
            val imageBytes = compressImage(imageUri)

            // Crear el request body
            val requestBody = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                "image",
                "image_${System.currentTimeMillis()}.jpg",
                requestBody
            )

            // Hacer el upload
            val response = imgbbApiService.uploadImage(API_KEY, multipartBody)

            if (response.success) {
                Result.success(response.data.displayUrl)
            } else {
                Result.failure(Exception("Error al subir la imagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sube múltiples imágenes y retorna una lista de URLs
     */
    suspend fun uploadMultipleImages(imageUris: List<Uri>): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val urls = mutableListOf<String>()
            for (uri in imageUris) {
                val result = uploadImage(uri)
                if (result.isSuccess) {
                    urls.add(result.getOrThrow())
                } else {
                    return@withContext Result.failure(result.exceptionOrNull() ?: Exception("Error al subir imagen"))
                }
            }
            Result.success(urls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Comprime la imagen a un tamaño razonable (máximo 1MB)
     */
    private fun compressImage(uri: Uri): ByteArray {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Redimensionar si es muy grande
        val maxDimension = 1920
        val resizedBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        // Comprimir a JPEG
        val outputStream = ByteArrayOutputStream()
        var quality = 90
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        // Si es muy grande, reducir calidad
        while (outputStream.size() > 1_000_000 && quality > 50) {
            outputStream.reset()
            quality -= 10
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        return outputStream.toByteArray()
    }
}
