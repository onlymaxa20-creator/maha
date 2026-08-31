package com.example.data.remote

import android.util.Log
import com.example.data.local.entity.ProductEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Service to synchronize products with Google Cloud Firebase Firestore.
 * Supports real-time snapshot listeners (onSnapshot / addSnapshotListener)
 * for immediate synchronization across all connected clients.
 */
class FirestoreProductService {
    companion object {
        private const val TAG = "FirestoreProductService"
        const val COLLECTION_PRODUCTS = "products"
    }

    private val firestore: FirebaseFirestore?
        get() {
            return try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.w(TAG, "Firebase Firestore not yet initialized: ${e.message}")
                null
            }
        }

    /**
     * Real-time listener for products collection using Firestore addSnapshotListener (onSnapshot).
     * Automatically emits updated product list whenever any user or seller adds, updates or deletes a product.
     */
    fun observeProductsRealtime(): Flow<List<ProductEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            Log.w(TAG, "Firestore is not available (waiting for Firebase initialization).")
            // Don't close immediately so it doesn't break upstream collectors
            awaitClose { }
            return@callbackFlow
        }

        var listenerRegistration: ListenerRegistration? = null
        try {
            listenerRegistration = db.collection(COLLECTION_PRODUCTS)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore onSnapshot listener error: ${error.message}", error)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val productList = mutableListOf<ProductEntity>()
                        for (doc in snapshots.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong()
                                    ?: doc.id.toLongOrNull()
                                    ?: 0L
                                val sellerId = (doc.get("sellerId") as? Number)?.toLong() ?: 1L
                                val sellerName = doc.getString("sellerName") ?: "Do‘kon"
                                val categoryId = (doc.get("categoryId") as? Number)?.toLong() ?: 1L
                                val categoryName = doc.getString("categoryName") ?: "Boshqa"
                                val name = doc.getString("name") ?: ""
                                val description = doc.getString("description") ?: ""
                                val price = (doc.get("price") as? Number)?.toDouble() ?: 0.0
                                val originalPrice = (doc.get("originalPrice") as? Number)?.toDouble() ?: 0.0
                                val discountPercent = (doc.get("discountPercent") as? Number)?.toInt() ?: 0
                                val isPromotional = doc.getBoolean("isPromotional") ?: false
                                val stock = (doc.get("stock") as? Number)?.toInt() ?: 1
                                val unit = doc.getString("unit") ?: "dona"
                                val imageUri = doc.getString("imageUri") ?: ""
                                val isAvailable = doc.getBoolean("isAvailable") ?: true
                                val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                                val updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                                if (name.isNotBlank() && id > 0) {
                                    productList.add(
                                        ProductEntity(
                                            id = id,
                                            sellerId = sellerId,
                                            sellerName = sellerName,
                                            categoryId = categoryId,
                                            categoryName = categoryName,
                                            name = name,
                                            description = description,
                                            price = price,
                                            originalPrice = originalPrice,
                                            discountPercent = discountPercent,
                                            isPromotional = isPromotional,
                                            stock = stock,
                                            unit = unit,
                                            imageUri = imageUri,
                                            isAvailable = isAvailable,
                                            createdAt = createdAt,
                                            updatedAt = updatedAt
                                        )
                                    )
                                }
                            } catch (parseEx: Exception) {
                                Log.e(TAG, "Error parsing Firestore document: ${doc.id}", parseEx)
                            }
                        }
                        trySend(productList)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach Firestore snapshot listener", e)
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    /**
     * Writes or updates product in Firestore under /products/{productId}
     */
    suspend fun saveProduct(product: ProductEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firebase Firestore is not initialized"))
        return try {
            val map = hashMapOf<String, Any>(
                "id" to product.id,
                "sellerId" to product.sellerId,
                "sellerName" to product.sellerName,
                "categoryId" to product.categoryId,
                "categoryName" to product.categoryName,
                "name" to product.name,
                "description" to product.description,
                "price" to product.price,
                "originalPrice" to product.originalPrice,
                "discountPercent" to product.discountPercent,
                "isPromotional" to product.isPromotional,
                "stock" to product.stock,
                "unit" to product.unit,
                "imageUri" to product.imageUri,
                "isAvailable" to product.isAvailable,
                "createdAt" to product.createdAt,
                "updatedAt" to product.updatedAt
            )
            db.collection(COLLECTION_PRODUCTS)
                .document(product.id.toString())
                .set(map, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving product to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes product from Firestore under /products/{productId}
     */
    suspend fun deleteProduct(productId: Long): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firebase Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_PRODUCTS)
                .document(productId.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
}
