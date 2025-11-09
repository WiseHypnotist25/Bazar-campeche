package dev.wh.bazar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val role: UserRole = UserRole.BUYER,
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    BUYER,
    SELLER,
    BOTH
}
