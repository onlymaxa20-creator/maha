package com.example.data.repository

import android.util.Log
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.remote.FirestoreProductService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductRepository(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val firestoreService: FirestoreProductService = FirestoreProductService(),
    private val firestoreSyncService: com.example.data.remote.FirestoreSyncService = com.example.data.remote.FirestoreSyncService()
) {
    private val TAG = "ProductRepository"

    /**
     * Starts listening to Firestore real-time snapshots and synchronizes
     * cloud products into the local database cache automatically.
     */
    fun startRealtimeSync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                firestoreService.observeProductsRealtime().collectLatest { remoteProducts ->
                    if (remoteProducts.isNotEmpty()) {
                        try {
                            productDao.insertProducts(remoteProducts)
                            val remoteIds = remoteProducts.map { it.id }.toSet()
                            val localProducts = productDao.getAllProductsDirect()
                            for (local in localProducts) {
                                if (!remoteIds.contains(local.id)) {
                                    productDao.deleteProductById(local.id)
                                }
                            }
                            Log.d(TAG, "Synchronized ${remoteProducts.size} products from Firestore.")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to insert Firestore products locally: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore sync subscription notice: ${e.message}")
            }
        }
    }

    /**
     * Ensures all 20 standard marketplace categories exist locally and on Firestore.
     */
    suspend fun ensureDefaultCategories() {
        val standardCategories = listOf(
            CategoryEntity(name = "Smartfonlar va Gadjetlar", iconKey = "smartphones", sortOrder = 1),
            CategoryEntity(name = "Kompyuterlar va Noutbuklar", iconKey = "computers", sortOrder = 2),
            CategoryEntity(name = "Maishiy texnika", iconKey = "appliances", sortOrder = 3),
            CategoryEntity(name = "Oziq-ovqat va Ichimliklar", iconKey = "food", sortOrder = 4),
            CategoryEntity(name = "Kiyim-kechak", iconKey = "clothing", sortOrder = 5),
            CategoryEntity(name = "Poyabzallar", iconKey = "shoes", sortOrder = 6),
            CategoryEntity(name = "Go‘zallik va Parvarish", iconKey = "beauty", sortOrder = 7),
            CategoryEntity(name = "Salomatlik va Dorixona", iconKey = "health", sortOrder = 8),
            CategoryEntity(name = "Uy-ro‘zg‘or va Oshxona", iconKey = "home", sortOrder = 9),
            CategoryEntity(name = "Bolalar mahsulotlari", iconKey = "kids", sortOrder = 10),
            CategoryEntity(name = "Avtomobil jihozlari", iconKey = "auto", sortOrder = 11),
            CategoryEntity(name = "Sport va Dam olish", iconKey = "sport", sortOrder = 12),
            CategoryEntity(name = "Qurilish va Ta’mirlash", iconKey = "tools", sortOrder = 13),
            CategoryEntity(name = "Kantselyariya va Kitoblar", iconKey = "books", sortOrder = 14),
            CategoryEntity(name = "Zargarlik va Aksessuarlar", iconKey = "jewelry", sortOrder = 15),
            CategoryEntity(name = "Hayvonlar uchun tovarlar", iconKey = "pets", sortOrder = 16),
            CategoryEntity(name = "Bog‘dorchilik va Tomorqa", iconKey = "garden", sortOrder = 17),
            CategoryEntity(name = "Hunarmandchilik va Xobbi", iconKey = "hobby", sortOrder = 18),
            CategoryEntity(name = "Aqlli uy va Xavfsizlik", iconKey = "smarthome", sortOrder = 19),
            CategoryEntity(name = "Boshqa mahsulotlar", iconKey = "other", sortOrder = 20)
        )

        for (cat in standardCategories) {
            val existing = categoryDao.getCategoryByName(cat.name)
            if (existing == null) {
                val insertedId = categoryDao.insertCategory(cat)
                val created = cat.copy(id = insertedId)
                try {
                    firestoreSyncService.saveCategory(created)
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Starts listening to Firestore real-time categories.
     */
    fun startRealtimeCategorySync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                ensureDefaultCategories()
            } catch (_: Exception) {}

            try {
                firestoreSyncService.observeCategoriesRealtime().collectLatest { remoteCategories ->
                    if (remoteCategories.isNotEmpty()) {
                        try {
                            categoryDao.insertCategories(remoteCategories)
                        } catch (_: Exception) {}
                    } else {
                        // If remote is empty, seed defaults
                        try {
                            ensureDefaultCategories()
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun getAllAvailableProducts(): Flow<List<ProductEntity>> = productDao.getAllAvailableProducts()
    fun getPromotionalProducts(): Flow<List<ProductEntity>> = productDao.getPromotionalProducts()
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()
    fun getAvailableProductsByCategory(categoryId: Long): Flow<List<ProductEntity>> =
        productDao.getAvailableProductsByCategory(categoryId)
    fun getProductsBySeller(sellerId: Long): Flow<List<ProductEntity>> =
        productDao.getProductsBySeller(sellerId)
    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)
    fun getProductById(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)
    suspend fun getProductByIdDirect(id: Long): ProductEntity? = productDao.getProductByIdDirect(id)

    suspend fun addProduct(product: ProductEntity): Result<Long> {
        if (product.name.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Mahsulot nomini kiriting"))
        }
        if (product.price <= 0) {
            return Result.failure(IllegalArgumentException("Mahsulot narxi 0 dan katta bo‘lishi kerak"))
        }
        if (product.stock < 0) {
            return Result.failure(IllegalArgumentException("Mahsulot soni manfiy bo‘lishi mumkin emas"))
        }
        val uniqueId = if (product.id > 0) product.id else (System.currentTimeMillis() * 1000L + (100..999).random())
        val productToSave = product.copy(id = uniqueId)
        val id = productDao.insertProduct(productToSave)
        val finalId = if (id > 0) id else uniqueId
        val savedProduct = productToSave.copy(id = finalId)

        // Sync to Firebase Firestore
        try {
            firestoreService.saveProduct(savedProduct)
        } catch (e: Exception) {
            Log.w(TAG, "Saved locally; Firestore upload queued: ${e.message}")
        }

        return Result.success(finalId)
    }

    suspend fun updateProduct(product: ProductEntity): Result<Unit> {
        if (product.name.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Mahsulot nomini kiriting"))
        }
        if (product.price <= 0) {
            return Result.failure(IllegalArgumentException("Mahsulot narxi 0 dan katta bo‘lishi kerak"))
        }
        val updated = product.copy(updatedAt = System.currentTimeMillis())
        productDao.updateProduct(updated)

        // Sync update to Firebase Firestore
        try {
            firestoreService.saveProduct(updated)
        } catch (e: Exception) {
            Log.w(TAG, "Updated locally; Firestore update queued: ${e.message}")
        }

        return Result.success(Unit)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
        try {
            firestoreService.deleteProduct(product.id)
        } catch (e: Exception) {
            Log.w(TAG, "Deleted locally; Firestore delete queued: ${e.message}")
        }
    }

    suspend fun deleteProductById(id: Long) {
        productDao.deleteProductById(id)
        try {
            firestoreService.deleteProduct(id)
        } catch (e: Exception) {
            Log.w(TAG, "Deleted locally; Firestore delete queued: ${e.message}")
        }
    }

    suspend fun deleteProductsBySeller(sellerId: Long) {
        val sellerProducts = productDao.getProductsBySellerDirect(sellerId)
        productDao.deleteProductsBySeller(sellerId)
        for (prod in sellerProducts) {
            try {
                firestoreService.deleteProduct(prod.id)
            } catch (e: Exception) {
                Log.w(TAG, "Deleted seller product ${prod.id} locally; Firestore delete queued: ${e.message}")
            }
        }
    }

    suspend fun updateAvailability(productId: Long, isAvailable: Boolean) {
        productDao.updateAvailability(productId, isAvailable)
        val current = productDao.getProductByIdDirect(productId)
        if (current != null) {
            try {
                firestoreService.saveProduct(current.copy(isAvailable = isAvailable, updatedAt = System.currentTimeMillis()))
            } catch (e: Exception) {
                Log.w(TAG, "Updated availability locally: ${e.message}")
            }
        }
    }

    suspend fun updateStock(productId: Long, stock: Int) {
        productDao.updateStock(productId, stock)
        val current = productDao.getProductByIdDirect(productId)
        if (current != null) {
            try {
                firestoreService.saveProduct(current.copy(stock = stock, updatedAt = System.currentTimeMillis()))
            } catch (e: Exception) {
                Log.w(TAG, "Updated stock locally: ${e.message}")
            }
        }
    }

    fun getProductsCount(): Flow<Int> = productDao.getProductsCount()
    fun getAvailableProductsCount(): Flow<Int> = productDao.getAvailableProductsCount()
    fun getProductsCountBySeller(sellerId: Long): Flow<Int> = productDao.getProductsCountBySeller(sellerId)

    // Categories
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    suspend fun addCategory(name: String, iconKey: String = "general"): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Kategoriya nomini kiriting"))
        }
        val existing = categoryDao.getCategoryByName(trimmed)
        if (existing != null) {
            return Result.success(existing.id)
        }
        val id = categoryDao.insertCategory(CategoryEntity(name = trimmed, iconKey = iconKey))
        val created = CategoryEntity(id = id, name = trimmed, iconKey = iconKey)
        try {
            firestoreSyncService.saveCategory(created)
        } catch (_: Exception) {}
        return Result.success(id)
    }
    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
        try {
            firestoreSyncService.saveCategory(category)
        } catch (_: Exception) {}
    }
    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
        try {
            firestoreSyncService.deleteCategory(category.id)
        } catch (_: Exception) {}
    }
}

