package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromoBannerEntity
import com.example.data.local.entity.ReviewEntity
import com.example.data.local.entity.SupportInfoEntity
import com.example.data.model.CartItemDetail
import com.example.data.model.OrderDetail
import com.example.data.repository.BannerRepository
import com.example.data.repository.CartRepository
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.RecommendationRepository
import com.example.data.repository.ReviewRepository
import com.example.data.repository.SupportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class StoreViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val favoriteRepository: FavoriteRepository,
    private val orderRepository: OrderRepository,
    private val bannerRepository: BannerRepository,
    private val reviewRepository: ReviewRepository,
    private val supportRepository: SupportRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    val supportInfo: StateFlow<SupportInfoEntity> = supportRepository.getSupportInfo()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = supportRepository.defaultSupportInfo
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    val banners: StateFlow<List<PromoBannerEntity>> = bannerRepository.getActiveBanners()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val promotionalProducts: StateFlow<List<ProductEntity>> = productRepository.getPromotionalProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<CategoryEntity>> = productRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val products: StateFlow<List<ProductEntity>> = combine(
        _searchQuery,
        _selectedCategoryId,
        productRepository.getAllAvailableProducts()
    ) { query, categoryId, allProducts ->
        allProducts.filter { product ->
            val matchesCategory = categoryId == null || product.categoryId == categoryId
            val matchesQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true) ||
                    product.categoryName.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentCustomerId = MutableStateFlow<Long?>(null)
    val currentCustomerId: StateFlow<Long?> = _currentCustomerId.asStateFlow()

    val personalizedRecommendations: StateFlow<List<ProductEntity>> = _currentCustomerId.flatMapLatest { customerId ->
        recommendationRepository.getPersonalizedRecommendations(customerId, limit = 14)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val purchaseBasedRecommendations: StateFlow<List<ProductEntity>> = _currentCustomerId.flatMapLatest { customerId ->
        recommendationRepository.getPurchasedBasedRecommendations(customerId, limit = 10)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getSimilarProducts(productId: Long): Flow<List<ProductEntity>> {
        return recommendationRepository.getSimilarProducts(productId, limit = 8)
    }

    fun getCategoryTrendingProducts(categoryId: Long, excludeProductId: Long? = null): Flow<List<ProductEntity>> {
        return recommendationRepository.getCategoryPopularProducts(categoryId, excludeProductId, limit = 8)
    }

    val cartItems: StateFlow<List<CartItemDetail>> = _currentCustomerId.flatMapLatest { customerId ->
        if (customerId != null) {
            cartRepository.getCartDetails(customerId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cartTotalSum: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.totalPrice }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val favoriteProducts: StateFlow<List<ProductEntity>> = _currentCustomerId.flatMapLatest { customerId ->
        if (customerId != null) {
            favoriteRepository.getFavoriteProducts(customerId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customerOrders: StateFlow<List<OrderDetail>> = _currentCustomerId.flatMapLatest { customerId ->
        if (customerId != null) {
            orderRepository.getCustomerOrders(customerId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    private val _orderPlacementError = MutableStateFlow<String?>(null)
    val orderPlacementError: StateFlow<String?> = _orderPlacementError.asStateFlow()

    private val _orderPlacementSuccess = MutableStateFlow<OrderEntity?>(null)
    val orderPlacementSuccess: StateFlow<OrderEntity?> = _orderPlacementSuccess.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    private val _showPhonePromptDialog = MutableStateFlow(false)
    val showPhonePromptDialog: StateFlow<Boolean> = _showPhonePromptDialog.asStateFlow()

    fun setCustomerId(id: Long?) {
        _currentCustomerId.value = id
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        val trimmed = query.trim()
        if (trimmed.length >= 3) {
            recommendationRepository.trackEvent(
                userId = _currentCustomerId.value ?: 0L,
                eventType = "search",
                searchQuery = trimmed
            )
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
        if (categoryId != null && _selectedCategoryId.value == categoryId) {
            recommendationRepository.trackEvent(
                userId = _currentCustomerId.value ?: 0L,
                eventType = "category_view",
                categoryId = categoryId
            )
        }
    }

    fun selectProduct(product: ProductEntity?) {
        _selectedProduct.value = product
        if (product != null) {
            trackProductView(product)
        }
    }

    fun trackProductView(product: ProductEntity) {
        recommendationRepository.trackEvent(
            userId = _currentCustomerId.value ?: 0L,
            eventType = "product_view",
            productId = product.id,
            categoryId = product.categoryId,
            sellerId = product.sellerId
        )
    }

    fun trackRecommendationImpression(productId: Long) {
        recommendationRepository.trackEvent(
            userId = _currentCustomerId.value ?: 0L,
            eventType = "recommendation_impression",
            productId = productId
        )
    }

    fun trackRecommendationClick(productId: Long) {
        recommendationRepository.trackEvent(
            userId = _currentCustomerId.value ?: 0L,
            eventType = "recommendation_click",
            productId = productId
        )
    }

    fun trackProductShare(productId: Long, categoryId: Long? = null, sellerId: Long? = null) {
        recommendationRepository.trackEvent(
            userId = _currentCustomerId.value ?: 0L,
            eventType = "product_share",
            productId = productId,
            categoryId = categoryId,
            sellerId = sellerId
        )
    }

    fun addToCart(productId: Long, quantity: Int = 1, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val customerId = _currentCustomerId.value
        if (customerId == null) {
            onResult(false, "Xarid qilish uchun tizimga kiring")
            return
        }
        viewModelScope.launch {
            val result = cartRepository.addToCart(customerId, productId, quantity)
            result.onSuccess {
                val prod = productRepository.getProductByIdDirect(productId)
                recommendationRepository.trackEvent(
                    userId = customerId,
                    eventType = "add_to_cart",
                    productId = productId,
                    categoryId = prod?.categoryId,
                    sellerId = prod?.sellerId
                )
                onResult(true, "Savatga qo‘shildi")
            }.onFailure {
                onResult(false, it.message ?: "Xatolik yuz berdi")
            }
        }
    }

    fun updateCartQuantity(cartDetail: CartItemDetail, newQty: Int) {
        viewModelScope.launch {
            cartRepository.updateCartItemQuantity(cartDetail.cartItem, newQty, cartDetail.product.stock)
        }
    }

    fun removeCartItem(cartDetail: CartItemDetail) {
        viewModelScope.launch {
            val customerId = _currentCustomerId.value ?: 0L
            cartRepository.removeCartItem(cartDetail.cartItem)
            recommendationRepository.trackEvent(
                userId = customerId,
                eventType = "remove_from_cart",
                productId = cartDetail.product.id,
                categoryId = cartDetail.product.categoryId,
                sellerId = cartDetail.product.sellerId
            )
        }
    }

    fun toggleFavorite(productId: Long) {
        val customerId = _currentCustomerId.value ?: return
        val wasFav = isProductInFavorites(productId)
        viewModelScope.launch {
            val prod = productRepository.getProductByIdDirect(productId)
            favoriteRepository.toggleFavorite(customerId, productId)
            recommendationRepository.trackEvent(
                userId = customerId,
                eventType = if (!wasFav) "product_like" else "product_unlike",
                productId = productId,
                categoryId = prod?.categoryId,
                sellerId = prod?.sellerId
            )
        }
    }

    fun isProductInFavorites(productId: Long): Boolean {
        return favoriteProducts.value.any { it.id == productId }
    }

    fun openPhonePromptDialog() {
        _showPhonePromptDialog.value = true
    }

    fun closePhonePromptDialog() {
        _showPhonePromptDialog.value = false
    }

    fun clearOrderPlacementState() {
        _orderPlacementError.value = null
        _orderPlacementSuccess.value = null
    }

    /**
     * Requirement #5:
     * When the customer tries to place an order, check if phone number exists.
     * If not exists: prompt customer to enter phone number.
     * If exists: proceed with checkout.
     */
    fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        deliveryAddress: String,
        deliveryNotes: String = "",
        deliveryLatitude: Double = 40.6622,
        deliveryLongitude: Double = 68.1672,
        deliveryStreet: String = "",
        deliveryHouseNumber: String = "",
        deliveryLandmark: String = "",
        onSuccess: (OrderEntity) -> Unit = {}
    ) {
        val customerId = _currentCustomerId.value
        if (customerId == null) {
            _orderPlacementError.value = "Buyurtma berish uchun tizimga kiring"
            return
        }

        // Rule #5 phone check
        if (customerPhone.trim().isEmpty() || customerPhone.trim().length < 9) {
            _showPhonePromptDialog.value = true
            _orderPlacementError.value = "Buyurtma berish uchun telefon raqamingizni kiriting."
            return
        }

        val items = cartItems.value
        if (items.isEmpty()) {
            _orderPlacementError.value = "Savat bo‘sh"
            return
        }

        viewModelScope.launch {
            _isPlacingOrder.value = true
            _orderPlacementError.value = null
            val result = orderRepository.placeOrder(
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                customerEmail = customerEmail,
                deliveryAddress = deliveryAddress,
                deliveryNotes = deliveryNotes,
                cartItems = items,
                deliveryLatitude = deliveryLatitude,
                deliveryLongitude = deliveryLongitude,
                deliveryStreet = deliveryStreet,
                deliveryHouseNumber = deliveryHouseNumber,
                deliveryLandmark = deliveryLandmark
            )
            _isPlacingOrder.value = false
            result.onSuccess { order ->
                _orderPlacementSuccess.value = order
                // Record purchase events for recommendations
                for (item in items) {
                    recommendationRepository.trackEvent(
                        userId = customerId,
                        eventType = "purchase",
                        productId = item.product.id,
                        categoryId = item.product.categoryId,
                        sellerId = item.product.sellerId
                    )
                }
                onSuccess(order)
            }.onFailure { error ->
                _orderPlacementError.value = error.message ?: "Buyurtma qabul qilinmadi"
            }
        }
    }

    val allReviews: StateFlow<List<ReviewEntity>> = reviewRepository.getAllReviews()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getReviewsForProduct(productId: Long): Flow<List<ReviewEntity>> {
        return reviewRepository.getReviewsForProduct(productId)
    }

    fun addReview(
        productId: Long,
        userId: Long,
        userName: String,
        userPhone: String,
        rating: Int,
        comment: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (comment.trim().isEmpty()) {
            onResult(false, "Iltimos, sharh matnini yozing")
            return
        }
        val safeRating = rating.coerceIn(1, 5)
        viewModelScope.launch {
            try {
                reviewRepository.addReview(
                    productId = productId,
                    userId = userId,
                    userName = userName.ifBlank { "Xaridor" },
                    userPhone = userPhone,
                    rating = safeRating,
                    comment = comment.trim()
                )
                onResult(true, "Sharhingiz uchun rahmat! Baho saqlandi ⭐")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Sharhni saqlashda xatolik yuz berdi")
            }
        }
    }

    class Factory(
        private val productRepository: ProductRepository,
        private val cartRepository: CartRepository,
        private val favoriteRepository: FavoriteRepository,
        private val orderRepository: OrderRepository,
        private val bannerRepository: BannerRepository,
        private val reviewRepository: ReviewRepository,
        private val supportRepository: SupportRepository,
        private val recommendationRepository: RecommendationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StoreViewModel(
                productRepository,
                cartRepository,
                favoriteRepository,
                orderRepository,
                bannerRepository,
                reviewRepository,
                supportRepository,
                recommendationRepository
            ) as T
        }
    }
}
