package dev.wh.bazar.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dev.wh.bazar.data.model.Order
import dev.wh.bazar.data.model.OrderStatus
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun createOrder(order: Order): Resource<String> {
        return try {
            val docRef = firestore.collection("orders")
                .add(order)
                .await()

            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create order")
        }
    }

    suspend fun getUserOrders(userId: String): Resource<List<Order>> {
        return try {
            val orders = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Order::class.java)

            Resource.Success(orders)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch orders")
        }
    }

    suspend fun getStoreOrders(storeId: String): Resource<List<Order>> {
        return try {
            val orders = firestore.collection("orders")
                .whereEqualTo("storeId", storeId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Order::class.java)

            Resource.Success(orders)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch orders")
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Resource<Unit> {
        return try {
            firestore.collection("orders")
                .document(orderId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update order")
        }
    }
}
