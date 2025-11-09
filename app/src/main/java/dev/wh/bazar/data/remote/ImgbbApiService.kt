package dev.wh.bazar.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImgbbApiService {
    @Multipart
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part
    ): ImgbbResponse
}

@Serializable
data class ImgbbResponse(
    @SerialName("data")
    val data: ImgbbData,
    @SerialName("success")
    val success: Boolean,
    @SerialName("status")
    val status: Int
)

@Serializable
data class ImgbbData(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("url_viewer")
    val urlViewer: String,
    @SerialName("url")
    val url: String,
    @SerialName("display_url")
    val displayUrl: String,
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int,
    @SerialName("size")
    val size: Int,
    @SerialName("time")
    val time: Long,
    @SerialName("expiration")
    val expiration: Int,
    @SerialName("image")
    val image: ImgbbImageInfo,
    @SerialName("thumb")
    val thumb: ImgbbImageInfo,
    @SerialName("delete_url")
    val deleteUrl: String
)

@Serializable
data class ImgbbImageInfo(
    @SerialName("filename")
    val filename: String,
    @SerialName("name")
    val name: String,
    @SerialName("mime")
    val mime: String,
    @SerialName("extension")
    val extension: String,
    @SerialName("url")
    val url: String
)
