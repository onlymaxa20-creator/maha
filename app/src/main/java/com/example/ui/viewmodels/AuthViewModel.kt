package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuthSession
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val session: StateFlow<AuthSession> = authRepository.currentSession
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthSession()
        )

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun clearMessages() {
        _authError.value = null
        _authSuccessMessage.value = null
    }

    private val _lastSentCode = MutableStateFlow<String?>(null)
    val lastSentCode: StateFlow<String?> = _lastSentCode.asStateFlow()

    fun sendVerificationCode(phone: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.sendPhoneVerificationCode(phone)
            _isLoading.value = false
            result.onSuccess { code ->
                _lastSentCode.value = code
                _authSuccessMessage.value = "Tasdiqlash kodi telefoningizga yuborildi: $code"
                onResult(true, code)
            }.onFailure { error ->
                val msg = error.message ?: "Kod yuborishda xatolik"
                _authError.value = msg
                onResult(false, msg)
            }
        }
    }

    fun sendLoginVerificationCode(phone: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.sendPhoneVerificationCodeForLogin(phone)
            _isLoading.value = false
            result.onSuccess { code ->
                _lastSentCode.value = code
                _authSuccessMessage.value = "Tasdiqlash kodi telefoningizga yuborildi: $code"
                onResult(true, code)
            }.onFailure { error ->
                val msg = error.message ?: "Kod yuborishda xatolik"
                _authError.value = msg
                onResult(false, msg)
            }
        }
    }

    fun sendRegistrationVerificationCode(phone: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.sendPhoneVerificationCodeForRegistration(phone)
            _isLoading.value = false
            result.onSuccess { code ->
                _lastSentCode.value = code
                _authSuccessMessage.value = "Tasdiqlash kodi telefoningizga yuborildi: $code"
                onResult(true, code)
            }.onFailure { error ->
                val msg = error.message ?: "Kod yuborishda xatolik"
                _authError.value = msg
                onResult(false, msg)
            }
        }
    }

    fun registerWithPhoneCode(
        phone: String,
        code: String,
        fullName: String,
        password: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.registerCustomerWithPhone(phone, code, fullName, password)
            _isLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "Ro‘yxatdan muvaffaqiyatli o‘tdingiz! Xush kelibsiz!"
                onSuccess()
            }.onFailure { error ->
                _authError.value = error.message ?: "Ro‘yxatdan o‘tishda xatolik"
            }
        }
    }

    fun loginWithPhoneCode(
        phone: String,
        code: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.loginCustomerWithPhoneCode(phone, code)
            _isLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "Muvaffaqiyatli kirdingiz!"
                onSuccess()
            }.onFailure { error ->
                _authError.value = error.message ?: "Kirishda xatolik"
            }
        }
    }

    fun registerCustomer(email: String = "", phone: String, password: String, confirmPass: String, fullName: String, onSuccess: () -> Unit = {}) {
        if (password.isNotEmpty() && password != confirmPass) {
            _authError.value = "Parollar bir-biriga mos kelmadi"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.registerCustomer(email, phone, password, fullName)
            _isLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "Ro‘yxatdan muvaffaqiyatli o‘tdingiz"
                onSuccess()
            }.onFailure { error ->
                _authError.value = error.message ?: "Ro‘yxatdan o‘tishda xatolik yuz berdi"
            }
        }
    }

    fun loginCustomer(loginOrPhone: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.loginCustomer(loginOrPhone, password)
            _isLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "Xush kelibsiz!"
                onSuccess()
            }.onFailure { error ->
                _authError.value = error.message ?: "Kirishda xatolik"
            }
        }
    }

    fun loginSeller(login: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.loginSeller(login, password)
            _isLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "Sotuvchi paneliga xush kelibsiz"
                onSuccess()
            }.onFailure { error ->
                _authError.value = error.message ?: "Sotuvchi sifatida kirishda xatolik"
            }
        }
    }

    fun loginAdmin(login: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.loginAdmin(login, password)
            _isLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "Admin paneliga xush kelibsiz"
                onSuccess()
            }.onFailure { error ->
                _authError.value = error.message ?: "Admin sifatida kirishda xatolik"
            }
        }
    }

    suspend fun loginFoodsAdmin(login: String, password: String): Result<com.example.data.local.entity.UserEntity> {
        _isLoading.value = true
        _authError.value = null
        val result = authRepository.loginFoodsAdmin(login, password)
        _isLoading.value = false
        result.onSuccess {
            _authSuccessMessage.value = "Food Admin paneliga xush kelibsiz"
        }.onFailure { error ->
            _authError.value = error.message ?: "Kirishda xatolik"
        }
        return result
    }

    suspend fun loginFoodsSeller(login: String, password: String): Result<com.example.data.local.entity.UserEntity> {
        _isLoading.value = true
        _authError.value = null
        val result = authRepository.loginFoodsSeller(login, password)
        _isLoading.value = false
        result.onSuccess {
            _authSuccessMessage.value = "Restoran boshqaruv paneliga xush kelibsiz"
        }.onFailure { error ->
            _authError.value = error.message ?: "Kirishda xatolik"
        }
        return result
    }

    suspend fun registerFoodsSeller(
        login: String,
        password: String,
        storeName: String,
        ownerName: String,
        phone: String,
        address: String = "Gagarin shahri"
    ): Result<com.example.data.local.entity.UserEntity> {
        _isLoading.value = true
        _authError.value = null
        val result = authRepository.registerFoodsSeller(
            login = login,
            password = password,
            storeName = storeName,
            ownerName = ownerName,
            phone = phone,
            address = address
        )
        _isLoading.value = false
        result.onSuccess {
            _authSuccessMessage.value = "Restoran muvaffaqiyatli ro'yxatga olindi!"
        }.onFailure { error ->
            _authError.value = error.message ?: "Ro‘yxatdan o‘tishda xatolik"
        }
        return result
    }

    fun updateProfile(fullName: String, phone: String, address: String, onSuccess: () -> Unit = {}) {
        val user = session.value.user ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.updateCustomerProfile(user.id, fullName, phone, address)
            _isLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "Ma’lumotlar saqlandi"
                onSuccess()
            }.onFailure {
                _authError.value = it.message ?: "Saqlashda xatolik"
            }
        }
    }

    fun updatePhone(phone: String, onSuccess: () -> Unit = {}) {
        val user = session.value.user ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.updateCustomerPhone(user.id, phone)
            _isLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "Telefon raqami saqlandi"
                onSuccess()
            }.onFailure {
                _authError.value = it.message ?: "Telefon saqlanmadi"
            }
        }
    }

    fun updateSavedAddress(address: String, onSuccess: () -> Unit = {}) {
        val user = session.value.user ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.updateCustomerAddress(user.id, address)
            _isLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "Manzil saqlandi"
                onSuccess()
            }.onFailure {
                _authError.value = it.message ?: "Manzil saqlanmadi"
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String, confirmNewPass: String, onSuccess: () -> Unit = {}) {
        val user = session.value.user ?: return
        if (newPass != confirmNewPass) {
            _authError.value = "Yangi parollar mos kelmadi"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = authRepository.changePassword(user.id, oldPass, newPass)
            _isLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "Parol muvaffaqiyatli o‘zgartirildi"
                onSuccess()
            }.onFailure {
                _authError.value = it.message ?: "Parolni o‘zgartirishda xatolik"
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit = {}) {
        authRepository.logout()
        onLoggedOut()
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
