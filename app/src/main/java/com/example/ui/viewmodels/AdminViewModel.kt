package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AdminAuditLogEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromoBannerEntity
import com.example.data.local.entity.SupportInfoEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.AdminDashboardStats
import com.example.data.model.OrderDetail
import com.example.data.model.RecommendationAnalyticsModel
import com.example.data.model.SellerRevenueStats
import com.example.data.repository.AdminAuditRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.BannerRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.RecommendationRepository
import com.example.data.repository.SupportRepository
import com.example.data.util.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val bannerRepository: BannerRepository,
    private val supportRepository: SupportRepository,
    private val auditRepository: AdminAuditRepository,
    private val recommendationRepository: RecommendationRepository? = null
) : ViewModel() {

    val recommendationAnalytics: StateFlow<RecommendationAnalyticsModel> =
        (recommendationRepository?.getRecommendationAnalytics() ?: kotlinx.coroutines.flow.flowOf(RecommendationAnalyticsModel()))
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RecommendationAnalyticsModel()
            )

    val dashboardStats: StateFlow<AdminDashboardStats> = orderRepository.getAdminStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AdminDashboardStats()
        )

    val sellersRevenueStats: StateFlow<List<SellerRevenueStats>> = orderRepository.getAllSellersRevenueStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val auditLogs: StateFlow<List<AdminAuditLogEntity>> = auditRepository.getRecentLogs(200)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customers: StateFlow<List<UserEntity>> = authRepository.getAllCustomers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sellers: StateFlow<List<UserEntity>> = authRepository.getAllSellers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allProducts: StateFlow<List<ProductEntity>> = productRepository.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBanners: StateFlow<List<PromoBannerEntity>> = bannerRepository.getAllBanners()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allOrders: StateFlow<List<OrderDetail>> = orderRepository.getAllOrders()
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

    val supportInfo: StateFlow<SupportInfoEntity> = supportRepository.getSupportInfo()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = supportRepository.defaultSupportInfo
        )

    val currentSession = authRepository.currentSession

    private val _selectedCustomer = MutableStateFlow<UserEntity?>(null)
    val selectedCustomer: StateFlow<UserEntity?> = _selectedCustomer.asStateFlow()

    private val _selectedSellerForRevenue = MutableStateFlow<SellerRevenueStats?>(null)
    val selectedSellerForRevenue: StateFlow<SellerRevenueStats?> = _selectedSellerForRevenue.asStateFlow()

    private val _sellerDateFilter = MutableStateFlow("Barchasi") // "Barchasi", "Bugun", "Shu hafta", "Shu oy"
    val sellerDateFilter: StateFlow<String> = _sellerDateFilter.asStateFlow()

    private val _adminError = MutableStateFlow<String?>(null)
    val adminError: StateFlow<String?> = _adminError.asStateFlow()

    private val _adminSuccess = MutableStateFlow<String?>(null)
    val adminSuccess: StateFlow<String?> = _adminSuccess.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun selectCustomer(customer: UserEntity?) {
        _selectedCustomer.value = customer
    }

    fun selectSellerForRevenue(seller: SellerRevenueStats?) {
        _selectedSellerForRevenue.value = seller
        if (seller != null) {
            logAdminAction(
                action = "Sotuvchi daromadi ko‘rildi",
                recordId = seller.sellerId.toString(),
                details = "Sotuvchi: ${seller.storeName} (${seller.sellerName})"
            )
        }
    }

    fun setSellerDateFilter(filter: String) {
        _sellerDateFilter.value = filter
    }

    fun clearMessages() {
        _adminError.value = null
        _adminSuccess.value = null
    }

    private fun logAdminAction(action: String, recordId: String = "", details: String = "") {
        viewModelScope.launch {
            val user = currentSession.value.user
            val adminId = user?.id ?: 1L
            val adminLogin = user?.login ?: "admin"
            auditRepository.logAction(
                adminId = adminId,
                adminLogin = adminLogin,
                action = action,
                recordId = recordId,
                details = details
            )
        }
    }

    fun changeAdminPassword(
        currentPass: String,
        newPass: String,
        confirmPass: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            _adminError.value = null

            val user = currentSession.value.user
            if (user == null) {
                _adminError.value = "Tizimga admin sifatida kirmagansiz"
                _isProcessing.value = false
                return@launch
            }

            if (newPass.length < 6) {
                _adminError.value = "Yangi parol kamida 6 ta belgidan iborat bo‘lishi kerak"
                _isProcessing.value = false
                return@launch
            }

            if (newPass != confirmPass) {
                _adminError.value = "Yangi parollar mos kelmadi"
                _isProcessing.value = false
                return@launch
            }

            val result = authRepository.changePassword(user.id, currentPass, newPass)
            _isProcessing.value = false
            result.onSuccess {
                _adminSuccess.value = "Parol muvaffaqiyatli o‘zgartirildi."
                logAdminAction(
                    action = "Admin paroli o‘zgartirildi",
                    recordId = user.id.toString(),
                    details = "Admin (${user.login}) o‘z parolini muvaffaqiyatli yangiladi"
                )
                onSuccess()
            }.onFailure {
                _adminError.value = it.message ?: "Joriy parol noto‘g‘ri."
            }
        }
    }

    fun createSeller(
        login: String,
        password: String,
        storeName: String,
        ownerName: String,
        phone: String,
        email: String = "",
        role: String = "SELLER",
        address: String = "Gagarin shahri, Mirzacho‘l tumani",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            _adminError.value = null
            val result = authRepository.createSellerByAdmin(
                login = login,
                password = password,
                storeName = storeName,
                ownerName = ownerName,
                phone = phone,
                email = email,
                role = role,
                address = address
            )
            _isProcessing.value = false
            result.onSuccess {
                val roleTitle = if (it.role == "FOODS_SELLER") "Gagarin Food oshxonasi" else "Bozor do‘koni"
                _adminSuccess.value = "$roleTitle muvaffaqiyatli yaratildi: ${it.storeName} (Login: ${it.login})"
                logAdminAction(
                    action = "Yangi sotuvchi yaratildi",
                    recordId = it.id.toString(),
                    details = "Do‘kon: ${it.storeName}, Login: ${it.login}, Rol: ${it.role}"
                )
                onSuccess()
            }.onFailure {
                _adminError.value = it.message ?: "Sotuvchi yaratishda xatolik"
            }
        }
    }

    fun updateSellerPassword(sellerId: Long, newPass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isProcessing.value = true
            _adminError.value = null
            val result = authRepository.updateSellerPasswordByAdmin(sellerId, newPass)
            _isProcessing.value = false
            result.onSuccess {
                _adminSuccess.value = "Sotuvchi paroli muvaffaqiyatli yangilandi"
                logAdminAction(
                    action = "Sotuvchi paroli yangilandi",
                    recordId = sellerId.toString(),
                    details = "Admin tomonidan sotuvchi paroli yangilandi"
                )
                onSuccess()
            }.onFailure {
                _adminError.value = it.message ?: "Parolni o‘zgartirishda xatolik"
            }
        }
    }

    fun toggleSellerActive(sellerId: Long, currentActive: Boolean) {
        viewModelScope.launch {
            authRepository.toggleSellerActive(sellerId, !currentActive)
            val actionText = if (!currentActive) "Sotuvchi faollashtirildi" else "Sotuvchi faolsizlantirildi"
            _adminSuccess.value = actionText
            logAdminAction(
                action = actionText,
                recordId = sellerId.toString(),
                details = "Sotuvchi holati: ${if (!currentActive) "Faol" else "Faolsiz"}"
            )
        }
    }

    fun deleteSeller(sellerId: Long) {
        viewModelScope.launch {
            productRepository.deleteProductsBySeller(sellerId)
            authRepository.deleteSeller(sellerId)
            recommendationRepository?.cleanupSellerEvents(sellerId)
            _adminSuccess.value = "Sotuvchi va barcha mahsulotlari o‘chirildi"
            logAdminAction(
                action = "Sotuvchi o‘chirildi",
                recordId = sellerId.toString(),
                details = "Sotuvchi va uning barcha mahsulotlari tizimdan to‘liq o‘chirildi"
            )
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: String) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, newStatus)
            result.onSuccess {
                _adminSuccess.value = "Buyurtma holati o‘zgartirildi: $newStatus"
                logAdminAction(
                    action = "Buyurtma holati o‘zgartirildi",
                    recordId = orderId.toString(),
                    details = "Yangi holat: $newStatus"
                )
            }.onFailure {
                _adminError.value = it.message ?: "Buyurtma holatini o‘zgartirishda xatolik"
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
            _adminSuccess.value = "Mahsulot o‘chirildi"
            logAdminAction(
                action = "Mahsulot o‘chirildi",
                recordId = product.id.toString(),
                details = "Mahsulot: ${product.name}, Narxi: ${product.price} so‘m"
            )
        }
    }

    fun updateProduct(product: ProductEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = productRepository.updateProduct(product)
            result.onSuccess {
                _adminSuccess.value = "Mahsulot muvaffaqiyatli yangilandi"
                logAdminAction(
                    action = "Mahsulot tahrirlandi",
                    recordId = product.id.toString(),
                    details = "Nomi: ${product.name}, Yangi narxi: ${product.price} so‘m, Qoldiq: ${product.stock}"
                )
                onSuccess()
            }.onFailure {
                _adminError.value = it.message ?: "Mahsulotni yangilashda xatolik"
            }
        }
    }

    fun addProduct(product: ProductEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = productRepository.addProduct(product)
            result.onSuccess { newId ->
                _adminSuccess.value = "Yangi mahsulot qo‘shildi"
                logAdminAction(
                    action = "Yangi mahsulot qo‘shildi",
                    recordId = newId.toString(),
                    details = "Nomi: ${product.name}, Narxi: ${product.price} so‘m, Sotuvchi: ${product.sellerName}"
                )
                onSuccess()
            }.onFailure {
                _adminError.value = it.message ?: "Mahsulot qo‘shishda xatolik"
            }
        }
    }

    fun toggleProductAvailability(product: ProductEntity) {
        viewModelScope.launch {
            val newStatus = !product.isAvailable
            productRepository.updateAvailability(product.id, newStatus)
            val actionText = if (newStatus) "Mahsulot sotuvga chiqarildi" else "Mahsulot sotuvdan olindi"
            _adminSuccess.value = actionText
            logAdminAction(
                action = actionText,
                recordId = product.id.toString(),
                details = "Mahsulot: ${product.name}"
            )
        }
    }

    fun addCategory(name: String, iconKey: String = "general", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = productRepository.addCategory(name, iconKey)
            result.onSuccess {
                _adminSuccess.value = "Yangi kategoriya qo‘shildi: $name"
                logAdminAction(
                    action = "Kategoriya qo‘shildi",
                    details = "Nomi: $name, Ikona: $iconKey"
                )
                onSuccess()
            }.onFailure {
                _adminError.value = it.message ?: "Kategoriya qo‘shishda xatolik"
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            productRepository.deleteCategory(category)
            _adminSuccess.value = "Kategoriya o‘chirildi"
            logAdminAction(
                action = "Kategoriya o‘chirildi",
                recordId = category.id.toString(),
                details = "Kategoriya: ${category.name}"
            )
        }
    }

    // Support Info Management
    fun updateSupportInfo(
        phone: String,
        secondaryPhone: String,
        telegramUsername: String,
        telegramChannel: String,
        workingHours: String,
        address: String,
        description: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            _adminError.value = null
            val result = supportRepository.updateSupportInfo(
                phone = phone,
                secondaryPhone = secondaryPhone,
                telegramUsername = telegramUsername,
                telegramChannel = telegramChannel,
                workingHours = workingHours,
                address = address,
                description = description
            )
            _isProcessing.value = false
            result.onSuccess {
                _adminSuccess.value = "Qo‘llab-quvvatlash ma’lumotlari muvaffaqiyatli saqlandi"
                logAdminAction(
                    action = "Qo‘llab-quvvatlash yangilandi",
                    details = "Tel: $phone, Telegram: $telegramUsername, Ish vaqti: $workingHours"
                )
                onSuccess()
            }.onFailure {
                _adminError.value = it.message ?: "Ma’lumotlarni saqlashda xatolik"
            }
        }
    }

    // App Remote Version and Update Management
    fun updateAppVersionConfig(
        latestVersionCode: Int,
        latestVersionName: String,
        minRequiredVersionCode: Int,
        isForceUpdate: Boolean,
        updateTitle: String,
        updateMessage: String,
        releaseNotes: String,
        playStoreUrl: String,
        telegramApkUrl: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            _adminError.value = null
            val result = supportRepository.updateAppVersionConfig(
                latestVersionCode = latestVersionCode,
                latestVersionName = latestVersionName,
                minRequiredVersionCode = minRequiredVersionCode,
                isForceUpdate = isForceUpdate,
                updateTitle = updateTitle,
                updateMessage = updateMessage,
                releaseNotes = releaseNotes,
                playStoreUrl = playStoreUrl,
                telegramApkUrl = telegramApkUrl
            )
            _isProcessing.value = false
            result.onSuccess {
                _adminSuccess.value = "Ilova versiyasi va yangilanish sozlamalari muvaffaqiyatli saqlandi!"
                logAdminAction(
                    action = "Ilova yangilanishi sozlamalari o‘zgartirildi",
                    details = "v$latestVersionName (kod: $latestVersionCode, Majburiy: $isForceUpdate, Min kod: $minRequiredVersionCode)"
                )
                onSuccess()
            }.onFailure {
                _adminError.value = it.message ?: "Versiya sozlamalarini saqlashda xatolik"
            }
        }
    }

    // Promo Banners (Admin only)
    fun addBanner(
        name: String,
        title: String,
        description: String = "",
        imageUrl: String = "",
        targetLink: String = "",
        startDate: Long = System.currentTimeMillis(),
        endDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
        badgeText: String = "REKLAMA",
        gradientType: String = "purple",
        isActive: Boolean = true,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = bannerRepository.addBanner(
                name = name,
                title = title,
                description = description,
                imageUrl = imageUrl,
                targetLink = targetLink,
                startDate = startDate,
                endDate = endDate,
                badgeText = badgeText,
                gradientType = gradientType,
                isActive = isActive
            )
            result.onSuccess { newId ->
                _adminSuccess.value = "Yangi reklama muvaffaqiyatli yaratildi va saqlandi"
                logAdminAction(
                    action = "Yangi reklama yaratildi",
                    recordId = newId.toString(),
                    details = "Nomi: $name, Sarlavha: $title, Rasm: ${if (imageUrl.isNotBlank()) "Mavjud" else "Yo‘q"}"
                )
                onSuccess()
            }.onFailure {
                _adminError.value = it.message ?: "Reklama bannerini qo‘shishda xatolik"
            }
        }
    }

    fun updateBanner(banner: PromoBannerEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = bannerRepository.updateBanner(banner)
            result.onSuccess {
                _adminSuccess.value = "Reklama muvaffaqiyatli yangilandi"
                logAdminAction(
                    action = "Reklama yangilandi",
                    recordId = banner.id.toString(),
                    details = "Nomi: ${banner.name.ifBlank { banner.title }}, Sarlavha: ${banner.title}"
                )
                onSuccess()
            }.onFailure {
                _adminError.value = it.message ?: "Xatolik yuz berdi"
            }
        }
    }

    fun deleteBanner(banner: PromoBannerEntity) {
        viewModelScope.launch {
            bannerRepository.deleteBanner(banner)
            _adminSuccess.value = "Reklama muvaffaqiyatli o‘chirildi"
            logAdminAction(
                action = "Reklama o‘chirildi",
                recordId = banner.id.toString(),
                details = "O‘chirilgan: ${banner.name.ifBlank { banner.title }}"
            )
        }
    }

    fun toggleBannerActive(banner: PromoBannerEntity) {
        viewModelScope.launch {
            bannerRepository.toggleActive(banner.id, banner.isActive)
            val actionText = if (!banner.isActive) "Reklama faollashtirildi (mijozlarga ko‘rinmoqda)" else "Reklama to‘xtatildi (faolsizlantirildi)"
            _adminSuccess.value = actionText
            logAdminAction(
                action = actionText,
                recordId = banner.id.toString(),
                details = "Reklama: ${banner.name.ifBlank { banner.title }}"
            )
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val productRepository: ProductRepository,
        private val orderRepository: OrderRepository,
        private val bannerRepository: BannerRepository,
        private val supportRepository: SupportRepository,
        private val auditRepository: AdminAuditRepository,
        private val recommendationRepository: RecommendationRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminViewModel(
                authRepository,
                productRepository,
                orderRepository,
                bannerRepository,
                supportRepository,
                auditRepository,
                recommendationRepository
            ) as T
        }
    }
}
