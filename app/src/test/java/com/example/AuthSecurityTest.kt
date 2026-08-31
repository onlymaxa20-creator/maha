package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthSecurityTest {

    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var context: Context

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        AppDatabase.seedInitialSystemData(database)
        authRepository = AuthRepository(database.userDao(), context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testLoginNonExistentCustomerFails() = runBlocking {
        val result = authRepository.loginCustomer("+998999999999", "wrongpass123")
        assertTrue("Kirish muvaffaqiyatsiz bo‘lishi shart", result.isFailure)
        assertFalse("Sessiya ochilmasligi shart", authRepository.currentSession.value.isLoggedIn)
        assertNull("Sessiyadagi foydalanuvchi null bo‘lishi shart", authRepository.currentSession.value.user)
    }

    @Test
    fun testLoginCustomerWrongPasswordFails() = runBlocking {
        // Register customer first
        val regResult = authRepository.registerCustomer(
            phone = "+998901112233",
            password = "correct_pass_123",
            fullName = "Test Foydalanuvchi"
        )
        assertTrue(regResult.isSuccess)
        authRepository.logout()

        // Try wrong password
        val loginResult = authRepository.loginCustomer("+998901112233", "wrong_pass_999")
        assertTrue("Noto‘g‘ri parol bilan kirish xato berishi shart", loginResult.isFailure)
        assertFalse(authRepository.currentSession.value.isLoggedIn)

        // Try correct password
        val successLogin = authRepository.loginCustomer("+998901112233", "correct_pass_123")
        assertTrue("To‘g‘ri parol bilan kirish muvaffaqiyatli bo‘lishi shart", successLogin.isSuccess)
        assertTrue(authRepository.currentSession.value.isLoggedIn)
        assertEquals(UserRole.CUSTOMER, authRepository.currentSession.value.role)
    }

    @Test
    fun testLoginPhoneOtpNonExistentUserFails() = runBlocking {
        // Send OTP for login on non-existent phone
        val sendResult = authRepository.sendPhoneVerificationCodeForLogin("+998990001122")
        assertTrue("Mavjud bo‘lmagan foydalanuvchiga login kodi yuborilmasligi kerak", sendResult.isFailure)

        // Direct login attempt with phone code
        val loginOtpResult = authRepository.loginCustomerWithPhoneCode("+998990001122", "123456")
        assertTrue("Mavjud bo‘lmagan foydalanuvchi tizimga kirmasligi shart", loginOtpResult.isFailure)
        assertFalse(authRepository.currentSession.value.isLoggedIn)
    }

    @Test
    fun testAdminLoginSecurity() = runBlocking {
        // Non-existent admin
        val nonExistent = authRepository.loginAdmin("fake_admin", "password")
        assertTrue(nonExistent.isFailure)
        assertFalse(authRepository.currentSession.value.isLoggedIn)

        // Wrong password for seeded admin
        val wrongPass = authRepository.loginAdmin("maha10", "wrong_admin_pass")
        assertTrue(wrongPass.isFailure)
        assertFalse(authRepository.currentSession.value.isLoggedIn)

        // Correct password for maha10
        val correctAdmin = authRepository.loginAdmin("maha10", "maha1010")
        assertTrue(correctAdmin.isSuccess)
        assertTrue(authRepository.currentSession.value.isLoggedIn)
        assertEquals(UserRole.SUPER_ADMIN, authRepository.currentSession.value.role)
    }

    @Test
    fun testSellerLoginSecurity() = runBlocking {
        // Log in as super admin first to authorize seller creation
        authRepository.loginAdmin("maha10", "maha1010")

        // Admin creates seller
        authRepository.createSellerByAdmin(
            login = "test_seller",
            password = "seller_pass_123",
            ownerName = "Gagarin Test Do'koni",
            phone = "+998901234567",
            storeName = "Test Do'kon",
            address = "Gagarin bozor",
            role = "BOZOR_SELLER"
        )

        // Logout admin
        authRepository.logout()

        // Non-existent seller
        val nonExistent = authRepository.loginSeller("fake_seller", "password")
        assertTrue(nonExistent.isFailure)
        assertFalse(authRepository.currentSession.value.isLoggedIn)

        // Wrong password
        val wrongPass = authRepository.loginSeller("test_seller", "wrong_seller_pass")
        assertTrue(wrongPass.isFailure)
        assertFalse(authRepository.currentSession.value.isLoggedIn)

        // Correct password
        val correctSeller = authRepository.loginSeller("test_seller", "seller_pass_123")
        assertTrue(correctSeller.isSuccess)
        assertTrue(authRepository.currentSession.value.isLoggedIn)
        assertEquals(UserRole.BOZOR_SELLER, authRepository.currentSession.value.role)
    }
}
