package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.model.SellerDashboardStats
import com.example.data.model.SellerOrderDetail
import com.example.data.repository.OrderRepository
import com.example.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SellerViewModel(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _currentSellerId = MutableStateFlow<Long?>(null)
    private val _currentSellerName = MutableStateFlow("")

    val categories: StateFlow<List<CategoryEntity>> = productRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val myProducts: StateFlow<List<ProductEntity>> = _currentSellerId.flatMapLatest { sellerId ->
        if (sellerId != null) {
            productRepository.getProductsBySeller(sellerId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val myOrders: StateFlow<List<SellerOrderDetail>> = _currentSellerId.flatMapLatest { sellerId ->
        if (sellerId != null) {
            orderRepository.getSellerOrders(sellerId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dashboardStats: StateFlow<SellerDashboardStats> = _currentSellerId.flatMapLatest { sellerId ->
        if (sellerId != null) {
            orderRepository.getSellerStats(sellerId)
        } else {
            flowOf(SellerDashboardStats())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SellerDashboardStats()
    )

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    fun setSeller(sellerId: Long, sellerName: String) {
        _currentSellerId.value = sellerId
        _currentSellerName.value = sellerName
    }

    fun clearMessages() {
        _actionError.value = null
        _actionSuccess.value = null
    }

    fun addProduct(
        name: String,
        description: String,
        categoryId: Long,
        categoryName: String,
        price: Double,
        stock: Int,
        unit: String,
        imageUri: String,
        isAvailable: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        val sellerId = _currentSellerId.value ?: return
        val sellerName = _currentSellerName.value

        viewModelScope.launch {
            _isSubmitting.value = true
            _actionError.value = null
            val product = ProductEntity(
                sellerId = sellerId,
                sellerName = sellerName,
                categoryId = categoryId,
                categoryName = categoryName,
                name = name.trim(),
                description = description.trim(),
                price = price,
                stock = stock,
                unit = if (unit.isNotBlank()) unit.trim() else "dona",
                imageUri = imageUri.trim(),
                isAvailable = isAvailable
            )
            val result = productRepository.addProduct(product)
            _isSubmitting.value = false
            result.onSuccess {
                _actionSuccess.value = "Mahsulot muvaffaqiyatli qo‘shildi"
                onSuccess()
            }.onFailure {
                _actionError.value = it.message ?: "Mahsulot qo‘shishda xatolik"
            }
        }
    }

    fun updateProduct(product: ProductEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _actionError.value = null
            val result = productRepository.updateProduct(product)
            _isSubmitting.value = false
            result.onSuccess {
                _actionSuccess.value = "Mahsulot yangilandi"
                onSuccess()
            }.onFailure {
                _actionError.value = it.message ?: "Yangilashda xatolik"
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
            _actionSuccess.value = "Mahsulot o‘chirildi"
        }
    }

    fun toggleAvailability(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.updateAvailability(product.id, !product.isAvailable)
        }
    }

    fun updateStock(product: ProductEntity, newStock: Int) {
        viewModelScope.launch {
            productRepository.updateStock(product.id, newStock.coerceAtLeast(0))
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _actionError.value = null
            val result = orderRepository.updateOrderStatus(orderId, newStatus)
            _isSubmitting.value = false
            result.onSuccess {
                _actionSuccess.value = "Buyurtma holati yangilandi: $newStatus"
                onSuccess()
            }.onFailure {
                _actionError.value = it.message ?: "Holatni yangilashda xatolik"
            }
        }
    }

    class Factory(
        private val productRepository: ProductRepository,
        private val orderRepository: OrderRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SellerViewModel(productRepository, orderRepository) as T
        }
    }
}
