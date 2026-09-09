package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.FoodBannerEntity
import com.example.data.local.entity.FoodCategoryEntity
import com.example.data.local.entity.FoodOrderEntity
import com.example.data.local.entity.FoodProductEntity
import com.example.data.local.entity.FoodPromotionEntity
import com.example.data.local.entity.FoodRestaurantEntity
import com.example.data.local.entity.FoodReviewEntity
import com.example.data.repository.FoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

data class FoodCartItem(
    val product: FoodProductEntity,
    val quantity: Int
) {
    val totalPrice: Double get() = product.price * quantity
}

@OptIn(ExperimentalCoroutinesApi::class)
class FoodStoreViewModel(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _currentCustomerId = MutableStateFlow<Long?>(null)
    val currentCustomerId: StateFlow<Long?> = _currentCustomerId.asStateFlow()

    fun setCurrentCustomerId(customerId: Long?) {
        _currentCustomerId.value = customerId
    }

    val customerFoodOrders: StateFlow<List<FoodOrderEntity>> = _currentCustomerId.flatMapLatest { customerId ->
        if (customerId != null && customerId > 0L) {
            foodRepository.getUserOrders(customerId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val restaurants: StateFlow<List<FoodRestaurantEntity>> =
        foodRepository.approvedRestaurants.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<FoodCategoryEntity>> =
        foodRepository.activeCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val banners: StateFlow<List<FoodBannerEntity>> =
        foodRepository.activeBanners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val promotions: StateFlow<List<FoodPromotionEntity>> =
        foodRepository.activePromotions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow("Barchasi")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedRestaurantId = MutableStateFlow<Long?>(null)
    val selectedRestaurantId: StateFlow<Long?> = _selectedRestaurantId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Food Cart
    private val _foodCart = MutableStateFlow<List<FoodCartItem>>(emptyList())
    val foodCart: StateFlow<List<FoodCartItem>> = _foodCart.asStateFlow()

    private val _cartWarning = MutableStateFlow<String?>(null)
    val cartWarning: StateFlow<String?> = _cartWarning.asStateFlow()

    val foodCartTotal: StateFlow<Double> = _foodCart.combine(_selectedCategory) { items, _ ->
        items.sumOf { it.totalPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val displayedProducts: StateFlow<List<FoodProductEntity>> = combine(
        foodRepository.approvedProducts,
        _selectedCategory,
        _selectedRestaurantId,
        _searchQuery
    ) { all, category, restaurantId, query ->
        all.filter { product ->
            val matchesCategory = (category == "Barchasi" || product.categoryName.equals(category, ignoreCase = true))
            val matchesRestaurant = (restaurantId == null || product.restaurantId == restaurantId)
            val matchesQuery = query.isBlank() || product.name.contains(query, ignoreCase = true) || product.restaurantName.contains(query, ignoreCase = true)
            matchesCategory && matchesRestaurant && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectRestaurant(restaurantId: Long?) {
        _selectedRestaurantId.value = restaurantId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: FoodProductEntity, quantity: Int = 1) {
        val current = _foodCart.value.toMutableList()
        if (current.isNotEmpty()) {
            val firstRestaurantId = current.first().product.restaurantId
            if (firstRestaurantId != product.restaurantId) {
                _cartWarning.value = "Savatda boshqa oshxona taomi bor (${current.first().product.restaurantName}). Bir vaqtda faqat bitta oshxonadan buyurtma berish mumkin. Savatni tozalab yangisini qo‘shmoqchimisiz?"
                return
            }
        }
        val existingIndex = current.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(FoodCartItem(product = product, quantity = quantity))
        }
        _foodCart.value = current
        _cartWarning.value = null
    }

    fun forceAddToCartWithClear(product: FoodProductEntity, quantity: Int = 1) {
        _foodCart.value = listOf(FoodCartItem(product = product, quantity = quantity))
        _cartWarning.value = null
    }

    fun dismissCartWarning() {
        _cartWarning.value = null
    }

    fun increaseQuantity(productId: Long) {
        val current = _foodCart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(quantity = item.quantity + 1)
            _foodCart.value = current
        }
    }

    fun decreaseQuantity(productId: Long) {
        val current = _foodCart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = current[index]
            if (item.quantity > 1) {
                current[index] = item.copy(quantity = item.quantity - 1)
            } else {
                current.removeAt(index)
            }
            _foodCart.value = current
        }
    }

    fun clearCart() {
        _foodCart.value = emptyList()
        _cartWarning.value = null
    }

    fun getFoodReviews(foodId: Long): Flow<List<FoodReviewEntity>> =
        foodRepository.getFoodReviews(foodId)

    fun getUserOrders(userId: Long): Flow<List<FoodOrderEntity>> =
        foodRepository.getUserOrders(userId)

    suspend fun placeOrder(
        userId: Long,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        customerNote: String = "",
        deliveryLatitude: Double = 40.6622,
        deliveryLongitude: Double = 68.1672,
        deliveryStreet: String = "",
        deliveryHouseNumber: String = "",
        deliveryLandmark: String = ""
    ): Result<Long> {
        val cartItems = _foodCart.value
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalStateException("Savat bo‘sh"))
        }

        val firstItem = cartItems.first().product
        val restaurantId = firstItem.restaurantId
        val restaurantName = firstItem.restaurantName
        val itemsSummary = cartItems.joinToString(", ") { "${it.product.name} x${it.quantity}" }
        val totalPrice = cartItems.sumOf { it.totalPrice }

        val result = foodRepository.placeFoodOrder(
            userId = userId,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            itemsSummary = itemsSummary,
            totalPrice = totalPrice,
            customerNote = customerNote,
            deliveryLatitude = deliveryLatitude,
            deliveryLongitude = deliveryLongitude,
            deliveryStreet = deliveryStreet,
            deliveryHouseNumber = deliveryHouseNumber,
            deliveryLandmark = deliveryLandmark
        )

        if (result.isSuccess) {
            clearCart()
        }
        return result
    }

    /**
     * Cancels food order placed by customer.
     * Allowed only before status reaches "YETKAZILMOQDA".
     */
    fun cancelFoodOrder(orderId: Long, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = foodRepository.cancelFoodOrderByCustomer(orderId)
            result.onSuccess {
                onResult(true, "Taom buyurtmasi muvaffaqiyatli bekor qilindi")
            }.onFailure { error ->
                onResult(false, error.message ?: "Buyurtmani bekor qilishda xatolik yuz berdi")
            }
        }
    }

    suspend fun submitReview(
        orderId: Long,
        userId: Long,
        userName: String,
        restaurantId: Long,
        foodId: Long,
        rating: Int,
        comment: String
    ) {
        foodRepository.addReview(
            orderId = orderId,
            userId = userId,
            userName = userName,
            restaurantId = restaurantId,
            foodId = foodId,
            rating = rating,
            comment = comment
        )
    }

    class Factory(
        private val foodRepository: FoodRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FoodStoreViewModel(foodRepository) as T
        }
    }
}
