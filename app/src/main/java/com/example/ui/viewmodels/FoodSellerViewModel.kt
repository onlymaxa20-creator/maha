package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.FoodCategoryEntity
import com.example.data.local.entity.FoodOrderEntity
import com.example.data.local.entity.FoodProductEntity
import com.example.data.local.entity.FoodRestaurantEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.FoodSellerDashboardStats
import com.example.data.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodSellerViewModel(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow<Long?>(null)
    private val _currentSellerUser = MutableStateFlow<UserEntity?>(null)
    val currentSellerUser: StateFlow<UserEntity?> = _currentSellerUser.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    val categories: StateFlow<List<FoodCategoryEntity>> =
        foodRepository.activeCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val myRestaurant: StateFlow<FoodRestaurantEntity?> = _currentUserId.flatMapLatest { userId ->
        if (userId != null) foodRepository.getRestaurantByUserId(userId)
        else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val myProducts: StateFlow<List<FoodProductEntity>> = myRestaurant.flatMapLatest { restaurant ->
        if (restaurant != null) foodRepository.getProductsByRestaurant(restaurant.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val myOrders: StateFlow<List<FoodOrderEntity>> = myRestaurant.flatMapLatest { restaurant ->
        if (restaurant != null) foodRepository.getOrdersByRestaurant(restaurant.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<FoodSellerDashboardStats> = combine(
        myProducts,
        myOrders
    ) { products, orders ->
        val activeProducts = products.count { it.isAvailable && it.status == "APPROVED" }
        val pendingOrders = orders.count { it.status == "YANGI" || it.status == "QABUL_QILINDI" || it.status == "TAYYORLANMOQDA" }
        val completedOrders = orders.count { it.status == "YETKAZILDI" }
        val revenue = orders.filter { it.status == "YETKAZILDI" }.sumOf { it.totalPrice }
        FoodSellerDashboardStats(
            todayOrdersCount = orders.size,
            activeProductsCount = activeProducts,
            pendingOrdersCount = pendingOrders,
            completedOrdersCount = completedOrders,
            totalRevenue = revenue
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FoodSellerDashboardStats())

    fun clearMessages() {
        _actionSuccess.value = null
        _actionError.value = null
    }

    fun setSellerUser(user: UserEntity) {
        _currentSellerUser.value = user
        _currentUserId.value = user.id
        viewModelScope.launch {
            ensureRestaurantExists(user)
        }
    }

    fun setSellerUserId(userId: Long?) {
        _currentUserId.value = userId
    }

    private suspend fun ensureRestaurantExists(user: UserEntity): FoodRestaurantEntity {
        val existing = foodRepository.getRestaurantByUserIdDirect(user.id)
        if (existing != null) {
            return existing
        }
        val defaultName = if (user.storeName.isNotBlank()) user.storeName else if (user.fullName.isNotBlank()) user.fullName else "Gagarin Fast Food"
        val newRestaurant = FoodRestaurantEntity(
            id = user.id,
            userId = user.id,
            name = defaultName,
            ownerName = if (user.fullName.isNotBlank()) user.fullName else "Fast Food Sotuvchisi",
            phone = user.phone,
            address = if (user.savedAddress.isNotBlank()) user.savedAddress else "Gagarin shahri",
            workingHours = "09:00 - 23:00",
            category = "Fast Food & Taomlar",
            description = "Mazzali va sifatli tezkor taomlar",
            coverUri = "",
            logoUri = "",
            status = "TASDIQLANGAN",
            isOpen = true
        )
        val insertedId = foodRepository.registerOrUpdateRestaurant(newRestaurant)
        return newRestaurant.copy(id = insertedId)
    }

    fun saveRestaurantProfile(
        name: String,
        ownerName: String,
        phone: String,
        address: String,
        workingHours: String,
        category: String,
        description: String,
        coverUri: String = "",
        logoUri: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val userId = _currentUserId.value ?: return@launch
            val existing = myRestaurant.value
            val entity = FoodRestaurantEntity(
                id = existing?.id ?: userId,
                userId = userId,
                name = name.trim(),
                ownerName = ownerName.trim(),
                phone = phone.trim(),
                address = address.trim(),
                workingHours = workingHours.trim(),
                category = category.trim(),
                description = description.trim(),
                coverUri = coverUri.trim(),
                logoUri = logoUri.trim(),
                status = existing?.status ?: "TASDIQLANGAN",
                isOpen = existing?.isOpen ?: true
            )
            foodRepository.registerOrUpdateRestaurant(entity)
            _actionSuccess.value = "Oshxona ma'lumotlari saqlandi"
            onSuccess()
        }
    }

    fun toggleStoreOpen(isOpen: Boolean) {
        viewModelScope.launch {
            val existing = myRestaurant.value ?: return@launch
            foodRepository.registerOrUpdateRestaurant(existing.copy(isOpen = isOpen))
            _actionSuccess.value = if (isOpen) "Oshxona ochiq holatga o‘tkazildi" else "Oshxona yopiq holatga o‘tkazildi"
        }
    }

    fun addProduct(
        name: String,
        categoryName: String,
        price: Double,
        description: String,
        imageUri: String,
        preparationTime: String,
        ingredients: String,
        isAvailable: Boolean = true,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isSubmitting.value = true
                _actionError.value = null

                // Ensure restaurant is guaranteed
                var restaurant = myRestaurant.value
                if (restaurant == null) {
                    val user = _currentSellerUser.value
                    if (user != null) {
                        restaurant = ensureRestaurantExists(user)
                    }
                }

                val restaurantId = restaurant?.id ?: (_currentUserId.value ?: System.currentTimeMillis())
                val restaurantName = restaurant?.name ?: (_currentSellerUser.value?.storeName ?: "Gagarin Fast Food")

                val uniqueId = System.currentTimeMillis() * 1000L + (100..999).random()
                val product = FoodProductEntity(
                    id = uniqueId,
                    restaurantId = restaurantId,
                    restaurantName = restaurantName,
                    categoryId = 0L,
                    categoryName = categoryName.trim().ifBlank { "Boshqa" },
                    name = name.trim(),
                    price = price,
                    description = description.trim(),
                    imageUri = imageUri.trim(),
                    preparationTime = preparationTime.trim().ifBlank { "15-20 daqiqa" },
                    ingredients = ingredients.trim(),
                    isAvailable = isAvailable,
                    status = "APPROVED"
                )

                foodRepository.addFoodProduct(product)
                _isSubmitting.value = false
                _actionSuccess.value = "Taom menyuga muvaffaqiyatli qo‘shildi"
                onSuccess()
            } catch (e: Exception) {
                _isSubmitting.value = false
                _actionError.value = e.localizedMessage ?: "Taom qo‘shishda xatolik yuz berdi"
            }
        }
    }

    fun updateProduct(
        product: FoodProductEntity,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isSubmitting.value = true
                _actionError.value = null
                foodRepository.updateFoodProduct(product)
                _isSubmitting.value = false
                _actionSuccess.value = "Taom ma'lumotlari yangilandi"
                onSuccess()
            } catch (e: Exception) {
                _isSubmitting.value = false
                _actionError.value = e.localizedMessage ?: "Yangilashda xatolik yuz berdi"
            }
        }
    }

    fun toggleProductAvailability(productId: Long, isAvailable: Boolean) {
        viewModelScope.launch {
            foodRepository.toggleFoodAvailability(productId, isAvailable)
            _actionSuccess.value = if (isAvailable) "Taom sotuvga chiqarildi" else "Taom vaqtincha to‘xtatildi"
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            foodRepository.deleteFoodProduct(productId)
            _actionSuccess.value = "Taom menyudan o‘chirildi"
        }
    }

    fun updateOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            foodRepository.updateOrderStatus(orderId, status)
            _actionSuccess.value = "Buyurtma holati yangilandi"
        }
    }

    fun rejectOrder(orderId: Long, reason: String) {
        viewModelScope.launch {
            foodRepository.rejectOrder(orderId, reason)
            _actionSuccess.value = "Buyurtma bekor qilindi"
        }
    }

    class Factory(
        private val foodRepository: FoodRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FoodSellerViewModel(foodRepository) as T
        }
    }
}

