package com.example.data.repository

import android.content.Context
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.data.model.AuthSession
import com.example.data.model.UserRole
import com.example.data.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthRepository(
    private val userDao: UserDao,
    private val context: Context,
    private val firestoreSyncService: com.example.data.remote.FirestoreSyncService = com.example.data.remote.FirestoreSyncService()
) {

    private val prefs = context.getSharedPreferences("gagarin_auth_prefs", Context.MODE_PRIVATE)

    private val _currentSession: MutableStateFlow<AuthSession> = MutableStateFlow(
        run {
            val isLoggedIn = prefs.getBoolean("saved_is_logged_in", false)
            val savedUserId = prefs.getLong("saved_user_id", -1L)
            val savedRoleStr = prefs.getString("saved_role", null)
            if (isLoggedIn && savedUserId > 0L && savedRoleStr != null) {
                val role = try {
                    UserRole.valueOf(savedRoleStr.uppercase())
                } catch (_: Exception) {
                    UserRole.CUSTOMER
                }
                val login = prefs.getString("saved_login", "") ?: ""
                val fullName = prefs.getString("saved_full_name", "") ?: ""
                val phone = prefs.getString("saved_phone", "") ?: ""
                val storeName = prefs.getString("saved_store_name", "") ?: ""
                val savedAddress = prefs.getString("saved_address", "") ?: ""
                val initialUser = UserEntity(
                    id = savedUserId,
                    role = role.name,
                    login = login,
                    email = "$login@gagarin.uz",
                    passwordHash = "",
                    salt = "",
                    fullName = fullName,
                    phone = phone,
                    storeName = storeName,
                    savedAddress = savedAddress,
                    isActive = true
                )
                AuthSession(
                    user = initialUser,
                    role = role,
                    isLoggedIn = true
                )
            } else {
                AuthSession()
            }
        }
    )
    val currentSession: StateFlow<AuthSession> = _currentSession.asStateFlow()

    init {
        // Automatically restore and verify saved session with local DB when app starts
        val isLoggedIn = prefs.getBoolean("saved_is_logged_in", false)
        val savedUserId = prefs.getLong("saved_user_id", -1L)
        if (isLoggedIn && savedUserId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val user = userDao.getUserByIdDirect(savedUserId)
                    if (user != null && user.isActive) {
                        val role = try {
                            UserRole.valueOf(user.role.uppercase())
                        } catch (e: Exception) {
                            UserRole.CUSTOMER
                        }
                        _currentSession.value = AuthSession(
                            user = user,
                            role = role,
                            isLoggedIn = true
                        )
                        saveSessionPrefs(user, role)
                    } else {
                        clearSessionPrefs()
                        _currentSession.value = AuthSession()
                    }
                } catch (e: Exception) {
                    // Keep the restored session
                }
            }
        }
    }

    private fun isUserMarkedDeleted(login: String, phone: String, storeName: String = ""): Boolean {
        val set = prefs.getStringSet("deleted_user_keys", emptySet()) ?: emptySet()
        return (login.isNotBlank() && set.contains("login_$login")) ||
               (phone.isNotBlank() && set.contains("phone_$phone")) ||
               (storeName.isNotBlank() && set.contains("store_$storeName"))
    }

    private fun markUserAsDeleted(login: String, phone: String, storeName: String = "", id: Long = 0L) {
        val set = prefs.getStringSet("deleted_user_keys", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (login.isNotBlank()) set.add("login_$login")
        if (phone.isNotBlank()) set.add("phone_$phone")
        if (storeName.isNotBlank()) set.add("store_$storeName")
        if (id > 0) set.add("id_$id")
        prefs.edit().putStringSet("deleted_user_keys", set).apply()
    }

    /**
     * Starts listening to Firestore for real-time customer registrations from other devices.
     * When any new user registers in the app on any device, it is instantly inserted/merged into
     * the local Room DB so the Admin sees them live in real-time.
     */
    fun startRealtimeUserSync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                firestoreSyncService.observeUsersRealtime().collect { remoteUsers ->
                    for (remoteUser in remoteUsers) {
                        try {
                            if (isUserMarkedDeleted(remoteUser.login, remoteUser.phone, remoteUser.storeName)) {
                                continue
                            }
                            val local = userDao.getUserByPhone(remoteUser.phone) ?: userDao.getUserByLogin(remoteUser.login)
                            if (local == null) {
                                userDao.insertUser(remoteUser)
                            } else {
                                // Update local if remote is newer
                                val updated = local.copy(
                                    fullName = remoteUser.fullName.ifBlank { local.fullName },
                                    savedAddress = remoteUser.savedAddress.ifBlank { local.savedAddress },
                                    isActive = remoteUser.isActive,
                                    storeName = remoteUser.storeName.ifBlank { local.storeName }
                                )
                                userDao.updateUser(updated)
                            }
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun saveSessionPrefs(user: UserEntity, role: UserRole) {
        prefs.edit()
            .putLong("saved_user_id", user.id)
            .putString("saved_role", role.name)
            .putString("saved_login", user.login)
            .putString("saved_full_name", user.fullName)
            .putString("saved_phone", user.phone)
            .putString("saved_store_name", user.storeName)
            .putString("saved_address", user.savedAddress)
            .putBoolean("saved_is_logged_in", true)
            .apply()
        com.example.data.util.NotificationHelper.updateSession(
            AuthSession(
                user = user,
                role = role,
                isLoggedIn = true
            )
        )
    }

    private fun clearSessionPrefs() {
        prefs.edit()
            .remove("saved_user_id")
            .remove("saved_role")
            .remove("saved_login")
            .remove("saved_full_name")
            .remove("saved_phone")
            .remove("saved_store_name")
            .remove("saved_address")
            .putBoolean("saved_is_logged_in", false)
            .apply()
        com.example.data.util.NotificationHelper.updateSession(
            AuthSession(
                user = null,
                role = UserRole.CUSTOMER,
                isLoggedIn = false
            )
        )
    }

    private val activeVerificationCodes = java.util.concurrent.ConcurrentHashMap<String, VerificationEntry>()

    data class VerificationEntry(
        val code: String,
        val expiresAt: Long = System.currentTimeMillis() + 5 * 60 * 1000
    )

    fun sanitizePhone(phone: String): String {
        val digitsOnly = phone.replace(Regex("[^0-9+]"), "")
        return if (digitsOnly.startsWith("+")) digitsOnly else "+$digitsOnly"
    }

    suspend fun sendPhoneVerificationCode(phone: String): Result<String> {
        val clean = sanitizePhone(phone)
        val digitsCount = clean.replace("+", "").length
        if (digitsCount < 9) {
            return Result.failure(IllegalArgumentException("Iltimos, to‘g‘ri telefon raqamingizni kiriting (kamida 9 ta raqam)"))
        }

        // Generate 6-digit code
        val generatedCode = (100000..999999).random().toString()
        activeVerificationCodes[clean] = VerificationEntry(generatedCode)

        // Trigger in-app + system notification
        com.example.data.util.NotificationHelper.showSmsNotification(context, clean, generatedCode)

        return Result.success(generatedCode)
    }

    suspend fun sendPhoneVerificationCodeForLogin(phone: String): Result<String> {
        val clean = sanitizePhone(phone)
        val digitsCount = clean.replace("+", "").length
        if (digitsCount < 9) {
            return Result.failure(IllegalArgumentException("Iltimos, to‘g‘ri telefon raqamingizni kiriting (kamida 9 ta raqam)"))
        }

        // Check if customer exists in DB
        val existingUser = userDao.getUserByPhone(clean) ?: userDao.getUserByLogin(clean)
        if (existingUser == null) {
            return Result.failure(IllegalArgumentException("Bunday foydalanuvchi topilmadi. Iltimos, avval ro‘yxatdan o‘ting."))
        }

        if (!existingUser.isActive) {
            return Result.failure(IllegalArgumentException("Ushbu hisob faolsizlantirilgan. Administratorga murojaat qiling."))
        }

        // Generate 6-digit code
        val generatedCode = (100000..999999).random().toString()
        activeVerificationCodes[clean] = VerificationEntry(generatedCode)

        // Trigger in-app + system notification
        com.example.data.util.NotificationHelper.showSmsNotification(context, clean, generatedCode)

        return Result.success(generatedCode)
    }

    suspend fun sendPhoneVerificationCodeForRegistration(phone: String): Result<String> {
        val clean = sanitizePhone(phone)
        val digitsCount = clean.replace("+", "").length
        if (digitsCount < 9) {
            return Result.failure(IllegalArgumentException("Iltimos, to‘g‘ri telefon raqamingizni kiriting (kamida 9 ta raqam)"))
        }

        // Check if user already exists
        val existingUser = userDao.getUserByPhone(clean) ?: userDao.getUserByLogin(clean)
        if (existingUser != null) {
            return Result.failure(IllegalArgumentException("Ushbu telefon raqami bilan hisob allaqachon mavjud. Iltimos, \"Kirish\" bo‘limidan kiring."))
        }

        // Generate 6-digit code
        val generatedCode = (100000..999999).random().toString()
        activeVerificationCodes[clean] = VerificationEntry(generatedCode)

        // Trigger in-app + system notification
        com.example.data.util.NotificationHelper.showSmsNotification(context, clean, generatedCode)

        return Result.success(generatedCode)
    }

    suspend fun registerCustomerWithPhone(
        phone: String,
        code: String,
        fullName: String,
        password: String = ""
    ): Result<UserEntity> {
        val clean = sanitizePhone(phone)
        val entry = activeVerificationCodes[clean]

        if (entry == null || entry.code != code.trim()) {
            return Result.failure(IllegalArgumentException("Tasdiqlash kodi noto‘g‘ri yoki kiritilmadi"))
        }

        if (System.currentTimeMillis() > entry.expiresAt) {
            activeVerificationCodes.remove(clean)
            return Result.failure(IllegalArgumentException("Tasdiqlash kodining amal qilish muddati tugagan. Qaytadan yuboring."))
        }

        // Remove used code
        activeVerificationCodes.remove(clean)

        // Check if customer already exists by phone or login
        val existingUser = userDao.getUserByPhone(clean) ?: userDao.getUserByLogin(clean)
        if (existingUser != null) {
            return Result.failure(IllegalArgumentException("Ushbu telefon raqami bilan hisob allaqachon mavjud. Iltimos, \"Kirish\" bo‘limi orqali kiring."))
        }

        val effectivePassword = if (password.isNotBlank() && password.length >= 6) password else "user_${clean.takeLast(4)}"
        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(effectivePassword, salt)

        val newUser = UserEntity(
            role = "CUSTOMER",
            login = clean,
            email = "",
            passwordHash = passwordHash,
            salt = salt,
            fullName = fullName.trim().ifEmpty { "Gagarin Xaridori" },
            phone = clean,
            savedAddress = "Gagarin shahri, Mirzacho‘l tumani"
        )

        val newId = userDao.insertUser(newUser)
        val createdUser = newUser.copy(id = newId)
        _currentSession.value = AuthSession(
            user = createdUser,
            role = UserRole.CUSTOMER,
            isLoggedIn = true
        )
        saveSessionPrefs(createdUser, UserRole.CUSTOMER)
        com.example.data.util.NotificationHelper.showAuthLoginNotification(createdUser, UserRole.CUSTOMER, context)

        // Sync new user to Firebase Firestore so Admin dashboard on all devices receives them in real-time
        try {
            firestoreSyncService.saveUser(createdUser)
        } catch (_: Exception) {}

        return Result.success(createdUser)
    }

    suspend fun loginCustomerWithPhoneCode(phone: String, code: String): Result<UserEntity> {
        val clean = sanitizePhone(phone)
        val entry = activeVerificationCodes[clean]

        if (entry == null || entry.code != code.trim()) {
            return Result.failure(IllegalArgumentException("Tasdiqlash kodi noto‘g‘ri"))
        }

        if (System.currentTimeMillis() > entry.expiresAt) {
            activeVerificationCodes.remove(clean)
            return Result.failure(IllegalArgumentException("Tasdiqlash kodining amal qilish muddati tugagan. Qaytadan yuboring."))
        }

        activeVerificationCodes.remove(clean)

        val user = userDao.getUserByPhone(clean) ?: userDao.getUserByLogin(clean)
        if (user == null) {
            return Result.failure(IllegalArgumentException("Bunday foydalanuvchi topilmadi. Iltimos, avval ro‘yxatdan o‘ting."))
        }

        if (user.role != "CUSTOMER") {
            return Result.failure(IllegalArgumentException("Ushbu hisob mijoz hisobi emas. Iltimos, tegishli panel orqali kiring."))
        }

        if (!user.isActive) {
            return Result.failure(IllegalArgumentException("Ushbu hisob faolsizlantirilgan. Administratorga murojaat qiling."))
        }

        _currentSession.value = AuthSession(
            user = user,
            role = UserRole.CUSTOMER,
            isLoggedIn = true
        )
        saveSessionPrefs(user, UserRole.CUSTOMER)
        com.example.data.util.NotificationHelper.showAuthLoginNotification(user, UserRole.CUSTOMER, context)
        return Result.success(user)
    }

    suspend fun registerCustomer(
        email: String = "",
        phone: String,
        password: String,
        fullName: String = ""
    ): Result<UserEntity> {
        val cleanPhone = sanitizePhone(phone)
        if (cleanPhone.replace("+", "").length < 9) {
            return Result.failure(IllegalArgumentException("Telefon raqami kamida 9 ta raqamdan iborat bo‘lishi kerak"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Parol kamida 6 ta belgidan iborat bo‘lishi kerak"))
        }

        val existingUser = userDao.getUserByPhone(cleanPhone) ?: userDao.getUserByLogin(cleanPhone)
        if (existingUser != null) {
            return Result.failure(IllegalArgumentException("Ushbu telefon raqami bilan hisob allaqachon mavjud"))
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)

        val newUser = UserEntity(
            role = "CUSTOMER",
            login = cleanPhone,
            email = email.trim(),
            passwordHash = passwordHash,
            salt = salt,
            fullName = fullName.trim().ifEmpty { "Gagarin Xaridori" },
            phone = cleanPhone,
            savedAddress = "Gagarin shahri, Mirzacho‘l tumani"
        )

        val newId = userDao.insertUser(newUser)
        val createdUser = newUser.copy(id = newId)
        _currentSession.value = AuthSession(
            user = createdUser,
            role = UserRole.CUSTOMER,
            isLoggedIn = true
        )
        saveSessionPrefs(createdUser, UserRole.CUSTOMER)
        com.example.data.util.NotificationHelper.showAuthLoginNotification(createdUser, UserRole.CUSTOMER, context)
        return Result.success(createdUser)
    }

    suspend fun loginCustomer(loginOrPhone: String, password: String): Result<UserEntity> {
        val trimmed = loginOrPhone.trim()
        val cleanPhone = sanitizePhone(trimmed)
        if (trimmed.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Telefon raqami va parolni kiriting"))
        }

        val user = userDao.getUserByPhone(cleanPhone)
            ?: userDao.getUserByLogin(cleanPhone)
            ?: userDao.getUserByLogin(trimmed)
            ?: userDao.getUserByEmail(trimmed.lowercase())

        if (user == null) {
            return Result.failure(IllegalArgumentException("Bunday foydalanuvchi topilmadi. Iltimos, avval ro‘yxatdan o‘ting."))
        }

        if (user.role != "CUSTOMER") {
            return Result.failure(IllegalArgumentException("Ushbu hisob mijoz hisobi emas. Iltimos, tegishli panel orqali kiring."))
        }

        if (!user.isActive) {
            return Result.failure(IllegalArgumentException("Ushbu hisob faolsizlantirilgan. Administratorga murojaat qiling."))
        }

        val isPasswordCorrect = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (!isPasswordCorrect) {
            return Result.failure(IllegalArgumentException("Kiritilgan parol noto‘g‘ri"))
        }

        _currentSession.value = AuthSession(
            user = user,
            role = UserRole.CUSTOMER,
            isLoggedIn = true
        )
        saveSessionPrefs(user, UserRole.CUSTOMER)
        com.example.data.util.NotificationHelper.showAuthLoginNotification(user, UserRole.CUSTOMER, context)
        return Result.success(user)
    }

    suspend fun loginSeller(login: String, password: String): Result<UserEntity> {
        val trimmedLogin = login.trim()
        if (trimmedLogin.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Login va parolni kiriting"))
        }

        val user = userDao.getUserByLogin(trimmedLogin)
        if (user == null) {
            return Result.failure(IllegalArgumentException("Bunday sotuvchi topilmadi. Loginni tekshiring."))
        }

        if (user.role != "SELLER" && user.role != "BOZOR_SELLER") {
            return Result.failure(IllegalArgumentException("Ushbu hisob sotuvchi hisobi emas"))
        }

        if (!user.isActive) {
            return Result.failure(IllegalArgumentException("Ushbu sotuvchi hisobi faolsizlantirilgan. Administratorga murojaat qiling."))
        }

        val isPasswordCorrect = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (!isPasswordCorrect) {
            return Result.failure(IllegalArgumentException("Sotuvchi paroli noto‘g‘ri"))
        }

        _currentSession.value = AuthSession(
            user = user,
            role = UserRole.BOZOR_SELLER,
            isLoggedIn = true
        )
        saveSessionPrefs(user, UserRole.BOZOR_SELLER)
        com.example.data.util.NotificationHelper.showAuthLoginNotification(user, UserRole.BOZOR_SELLER, context)
        return Result.success(user)
    }

    suspend fun loginFoodsSeller(login: String, password: String): Result<UserEntity> {
        val trimmedLogin = login.trim()
        if (trimmedLogin.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Login va parolni kiriting"))
        }

        val user = userDao.getUserByLogin(trimmedLogin)
        if (user == null) {
            return Result.failure(IllegalArgumentException("Bunday oshxona / restoran hisobi topilmadi."))
        }

        if (user.role != "FOODS_SELLER") {
            return Result.failure(IllegalArgumentException("Ushbu hisob oshxona hisobi emas"))
        }

        if (!user.isActive) {
            return Result.failure(IllegalArgumentException("Ushbu oshxona hisobi faolsizlantirilgan. Administratorga murojaat qiling."))
        }

        val isPasswordCorrect = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (!isPasswordCorrect) {
            return Result.failure(IllegalArgumentException("Kiritilgan parol noto‘g‘ri"))
        }

        _currentSession.value = AuthSession(
            user = user,
            role = UserRole.FOODS_SELLER,
            isLoggedIn = true
        )
        saveSessionPrefs(user, UserRole.FOODS_SELLER)
        com.example.data.util.NotificationHelper.showAuthLoginNotification(user, UserRole.FOODS_SELLER, context)
        return Result.success(user)
    }

    suspend fun registerFoodsSeller(
        login: String,
        password: String,
        storeName: String,
        ownerName: String,
        phone: String,
        address: String = "Gagarin shahri"
    ): Result<UserEntity> {
        val trimmedLogin = login.trim()
        if (trimmedLogin.length < 3) {
            return Result.failure(IllegalArgumentException("Login kamida 3 ta belgidan iborat bo‘lishi kerak"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Parol kamida 6 ta belgidan iborat bo‘lishi kerak"))
        }
        if (storeName.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Oshxona / Restoran nomini kiriting"))
        }

        val existing = userDao.getUserByLogin(trimmedLogin)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Ushbu login allaqachon band qilingan"))
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)

        val seller = UserEntity(
            role = "FOODS_SELLER",
            login = trimmedLogin,
            email = "$trimmedLogin@food.uz",
            passwordHash = passwordHash,
            salt = salt,
            fullName = ownerName.trim(),
            phone = phone.trim(),
            storeName = storeName.trim(),
            savedAddress = address.trim(),
            isActive = true
        )

        val id = userDao.insertUser(seller)
        val createdUser = seller.copy(id = id)
        _currentSession.value = AuthSession(
            user = createdUser,
            role = UserRole.FOODS_SELLER,
            isLoggedIn = true
        )
        saveSessionPrefs(createdUser, UserRole.FOODS_SELLER)
        com.example.data.util.NotificationHelper.showAuthLoginNotification(createdUser, UserRole.FOODS_SELLER, context)
        return Result.success(createdUser)
    }

    suspend fun loginAdmin(login: String, password: String): Result<UserEntity> {
        val trimmedLogin = login.trim()
        if (trimmedLogin.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Admin logini va parolini kiriting"))
        }

        val user = userDao.getUserByLogin(trimmedLogin)
        if (user == null) {
            return Result.failure(IllegalArgumentException("Bunday administrator topilmadi. Loginni tekshiring."))
        }

        if (user.role != "ADMIN" && user.role != "SUPER_ADMIN") {
            return Result.failure(IllegalArgumentException("Ushbu hisob administrator huquqiga ega emas"))
        }

        if (!user.isActive) {
            return Result.failure(IllegalArgumentException("Ushbu admin hisobi faolsizlantirilgan."))
        }

        val isPasswordCorrect = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (!isPasswordCorrect) {
            return Result.failure(IllegalArgumentException("Admin paroli noto‘g‘ri"))
        }

        _currentSession.value = AuthSession(
            user = user,
            role = UserRole.SUPER_ADMIN,
            isLoggedIn = true
        )
        saveSessionPrefs(user, UserRole.SUPER_ADMIN)
        com.example.data.util.NotificationHelper.showAuthLoginNotification(user, UserRole.SUPER_ADMIN, context)
        return Result.success(user)
    }

    suspend fun loginFoodsAdmin(login: String, password: String): Result<UserEntity> {
        val trimmedLogin = login.trim()
        if (trimmedLogin.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Food Admin logini va parolini kiriting"))
        }

        val user = userDao.getUserByLogin(trimmedLogin)
        if (user == null) {
            return Result.failure(IllegalArgumentException("Bunday Food Admin topilmadi. Loginni tekshiring."))
        }

        if (user.role != "FOODS_ADMIN" && user.role != "ADMIN" && user.role != "SUPER_ADMIN") {
            return Result.failure(IllegalArgumentException("Ushbu hisob Food Admin huquqiga ega emas"))
        }

        if (!user.isActive) {
            return Result.failure(IllegalArgumentException("Ushbu Food Admin hisobi faolsizlantirilgan."))
        }

        val isPasswordCorrect = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (!isPasswordCorrect) {
            return Result.failure(IllegalArgumentException("Food Admin paroli noto‘g‘ri"))
        }

        _currentSession.value = AuthSession(
            user = user,
            role = UserRole.FOODS_ADMIN,
            isLoggedIn = true
        )
        saveSessionPrefs(user, UserRole.FOODS_ADMIN)
        com.example.data.util.NotificationHelper.showAuthLoginNotification(user, UserRole.FOODS_ADMIN, context)
        return Result.success(user)
    }

    suspend fun createSellerByAdmin(
        login: String,
        password: String,
        storeName: String,
        ownerName: String,
        phone: String,
        email: String = "",
        role: String = "SELLER",
        address: String = "Gagarin shahri, Mirzacho‘l tumani"
    ): Result<UserEntity> {
        val current = _currentSession.value
        if (current.role != UserRole.ADMIN && current.role != UserRole.SUPER_ADMIN && current.role != UserRole.FOODS_ADMIN) {
            return Result.failure(SecurityException("Faqat administrator sotuvchi yarata oladi"))
        }

        val trimmedLogin = login.trim()
        if (trimmedLogin.length < 3) {
            return Result.failure(IllegalArgumentException("Sotuvchi logini kamida 3 ta belgidan iborat bo‘lishi kerak"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Parol kamida 6 ta belgidan iborat bo‘lishi kerak"))
        }
        if (storeName.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Do‘kon yoki Oshxona nomini kiriting"))
        }

        val existing = userDao.getUserByLogin(trimmedLogin)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Ushbu login allaqachon band qilingan"))
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)
        val normalizedRole = if (role.equals("FOODS_SELLER", ignoreCase = true) || role.equals("FOOD", ignoreCase = true)) "FOODS_SELLER" else "SELLER"

        val seller = UserEntity(
            role = normalizedRole,
            login = trimmedLogin,
            email = email.trim().ifEmpty { "$trimmedLogin@gagarin.uz" },
            passwordHash = passwordHash,
            salt = salt,
            fullName = ownerName.trim().ifEmpty { storeName.trim() },
            phone = phone.trim(),
            storeName = storeName.trim(),
            savedAddress = address.trim().ifEmpty { "Gagarin shahri, Mirzacho‘l tumani" },
            isActive = true
        )

        val id = userDao.insertUser(seller)
        val createdSeller = seller.copy(id = id)
        try {
            firestoreSyncService.saveUser(createdSeller)
        } catch (_: Exception) {}
        return Result.success(createdSeller)
    }

    suspend fun updateSellerPasswordByAdmin(sellerId: Long, newPassword: String): Result<Unit> {
        val current = _currentSession.value
        if (current.role != UserRole.ADMIN && current.role != UserRole.SUPER_ADMIN && current.role != UserRole.FOODS_ADMIN) {
            return Result.failure(SecurityException("Ruxsat berilmagan"))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Parol kamida 6 ta belgidan iborat bo‘lishi kerak"))
        }
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(newPassword, salt)
        userDao.updatePassword(sellerId, hash, salt)
        return Result.success(Unit)
    }

    suspend fun toggleSellerActive(sellerId: Long, isActive: Boolean): Result<Unit> {
        val current = _currentSession.value
        if (current.role != UserRole.ADMIN && current.role != UserRole.SUPER_ADMIN && current.role != UserRole.FOODS_ADMIN) {
            return Result.failure(SecurityException("Ruxsat berilmagan"))
        }
        userDao.updateSellerStatus(sellerId, isActive)
        return Result.success(Unit)
    }

    suspend fun deleteSeller(sellerId: Long): Result<Unit> {
        val current = _currentSession.value
        if (current.role != UserRole.ADMIN && current.role != UserRole.SUPER_ADMIN && current.role != UserRole.FOODS_ADMIN) {
            return Result.failure(SecurityException("Ruxsat berilmagan"))
        }
        val user = userDao.getUserByIdDirect(sellerId)
        userDao.deleteUserById(sellerId)
        if (user != null) {
            markUserAsDeleted(user.login, user.phone, user.storeName, user.id)
            try {
                firestoreSyncService.deleteUser(user.id, user.phone, user.login)
            } catch (_: Exception) {}
        }
        return Result.success(Unit)
    }

    suspend fun updateCustomerPhone(userId: Long, phone: String): Result<Unit> {
        val trimmed = phone.trim()
        if (trimmed.length < 9) {
            return Result.failure(IllegalArgumentException("To‘g‘ri telefon raqamini kiriting"))
        }
        userDao.updatePhone(userId, trimmed)
        val currentUser = _currentSession.value.user
        if (currentUser != null && currentUser.id == userId) {
            _currentSession.value = _currentSession.value.copy(
                user = currentUser.copy(phone = trimmed)
            )
        }
        return Result.success(Unit)
    }

    suspend fun updateCustomerAddress(userId: Long, address: String): Result<Unit> {
        val trimmed = address.trim()
        userDao.updateSavedAddress(userId, trimmed)
        val currentUser = _currentSession.value.user
        if (currentUser != null && currentUser.id == userId) {
            _currentSession.value = _currentSession.value.copy(
                user = currentUser.copy(savedAddress = trimmed)
            )
        }
        return Result.success(Unit)
    }

    suspend fun updateCustomerProfile(userId: Long, fullName: String, phone: String, address: String): Result<Unit> {
        val trimmedPhone = phone.trim()
        if (trimmedPhone.isNotEmpty() && trimmedPhone.length < 9) {
            return Result.failure(IllegalArgumentException("Telefon raqami kamida 9 ta raqam bo‘lishi kerak"))
        }
        val user = userDao.getUserByIdDirect(userId) ?: return Result.failure(IllegalArgumentException("Foydalanuvchi topilmadi"))
        val updated = user.copy(
            fullName = fullName.trim(),
            phone = trimmedPhone,
            savedAddress = address.trim()
        )
        userDao.updateUser(updated)
        if (_currentSession.value.user?.id == userId) {
            _currentSession.value = _currentSession.value.copy(user = updated)
        }
        return Result.success(Unit)
    }

    suspend fun changePassword(userId: Long, oldPass: String, newPass: String): Result<Unit> {
        val user = userDao.getUserByIdDirect(userId) ?: return Result.failure(IllegalArgumentException("Foydalanuvchi topilmadi"))
        if (!SecurityUtils.verifyPassword(oldPass, user.salt, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("Eski parol noto‘g‘ri"))
        }
        if (newPass.length < 6) {
            return Result.failure(IllegalArgumentException("Yangi parol kamida 6 ta belgidan iborat bo‘lishi kerak"))
        }
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(newPass, salt)
        userDao.updatePassword(userId, hash, salt)
        return Result.success(Unit)
    }

    fun logout() {
        clearSessionPrefs()
        _currentSession.value = AuthSession(
            user = null,
            role = UserRole.CUSTOMER,
            isLoggedIn = false
        )
    }

    fun getAllCustomers(): Flow<List<UserEntity>> = userDao.getAllCustomers()
    fun getAllSellers(): Flow<List<UserEntity>> = userDao.getAllSellers()
    fun getCustomersCount(): Flow<Int> = userDao.getCustomersCount()
    fun getSellersCount(): Flow<Int> = userDao.getSellersCount()
    fun getActiveSellersCount(): Flow<Int> = userDao.getActiveSellersCount()
}
