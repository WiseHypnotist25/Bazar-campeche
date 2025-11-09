package dev.wh.bazar.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dev.wh.bazar.data.model.Store
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.tasks.await

class StoreRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getAllStores(): Resource<List<Store>> {
        return try {
            val stores = firestore.collection("stores")
                .whereEqualTo("isActive", true)
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Store::class.java)

            Resource.Success(stores)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch stores")
        }
    }

    suspend fun getStoreById(storeId: String): Resource<Store> {
        return try {
            val store = firestore.collection("stores")
                .document(storeId)
                .get()
                .await()
                .toObject(Store::class.java)
                ?: throw Exception("Store not found")

            Resource.Success(store)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch store")
        }
    }

    suspend fun searchStores(query: String): Resource<List<Store>> {
        return try {
            val stores = firestore.collection("stores")
                .get()
                .await()
                .toObjects(Store::class.java)
                .filter {
                    (it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)) &&
                    it.isActive
                }

            Resource.Success(stores)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Search failed")
        }
    }

    suspend fun getStoreByOwnerId(ownerId: String): Resource<Store?> {
        return try {
            val stores = firestore.collection("stores")
                .whereEqualTo("ownerId", ownerId)
                .limit(1)
                .get()
                .await()
                .toObjects(Store::class.java)

            Resource.Success(stores.firstOrNull())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch store")
        }
    }

    suspend fun createStore(store: Store): Resource<String> {
        return try {
            val docRef = firestore.collection("stores")
                .add(store)
                .await()

            // Actualizar la tienda con su ID real
            val updatedStore = store.copy(id = docRef.id)
            firestore.collection("stores")
                .document(docRef.id)
                .set(updatedStore)
                .await()

            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create store")
        }
    }

    suspend fun updateStore(storeId: String, store: Store): Resource<Unit> {
        return try {
            firestore.collection("stores")
                .document(storeId)
                .set(store)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update store")
        }
    }
}
