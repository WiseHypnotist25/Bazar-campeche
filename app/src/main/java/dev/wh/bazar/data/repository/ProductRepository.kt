package dev.wh.bazar.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dev.wh.bazar.data.model.Product
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getRecommendedProducts(limit: Long = 20): Resource<List<Product>> {
        return try {
            val products = firestore.collection("products")
                .whereEqualTo("available", true)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
                .toObjects(Product::class.java)

            Resource.Success(products)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch products")
        }
    }

    suspend fun getProductById(productId: String): Resource<Product> {
        return try {
            val product = firestore.collection("products")
                .document(productId)
                .get()
                .await()
                .toObject(Product::class.java)
                ?: throw Exception("Product not found")

            Resource.Success(product)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch product")
        }
    }

    suspend fun getProductsByStore(storeId: String): Resource<List<Product>> {
        return try {
            val products = firestore.collection("products")
                .whereEqualTo("storeId", storeId)
                .get()
                .await()
                .toObjects(Product::class.java)

            Resource.Success(products)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch store products")
        }
    }

    suspend fun searchProducts(query: String): Resource<List<Product>> {
        return try {
            val products = firestore.collection("products")
                .whereEqualTo("available", true)
                .get()
                .await()
                .toObjects(Product::class.java)
                .filter {
                    // Si el query está vacío, devolver todos los productos
                    if (query.isBlank()) {
                        true
                    } else {
                        it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                    }
                }

            Resource.Success(products)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Search failed")
        }
    }

    suspend fun addProduct(product: Product): Resource<String> {
        return try {
            val docRef = firestore.collection("products")
                .add(product)
                .await()

            // Actualizar el producto con su ID real
            val updatedProduct = product.copy(id = docRef.id)
            firestore.collection("products")
                .document(docRef.id)
                .set(updatedProduct)
                .await()

            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add product")
        }
    }

    suspend fun updateProduct(productId: String, product: Product): Resource<Unit> {
        return try {
            firestore.collection("products")
                .document(productId)
                .set(product)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update product")
        }
    }

    suspend fun deleteProduct(productId: String): Resource<Unit> {
        return try {
            firestore.collection("products")
                .document(productId)
                .delete()
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete product")
        }
    }
}
