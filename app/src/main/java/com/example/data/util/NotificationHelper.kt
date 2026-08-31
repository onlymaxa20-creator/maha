package com.example.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.entity.UserEntity
import com.example.data.model.AuthSession
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class InAppNotification(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val code: String? = null,
    val targetRole: String? = null, // "SELLER", "FOOD_SELLER", "CUSTOMER", "ADMIN", "ALL", "SMS", "AUTH"
    val targetUserId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object NotificationHelper {
    private const val TAG = "NotificationHelper"

    const val CHANNEL_ORDERS = "gagarin_orders_notifications"
    const val CHANNEL_SELLER = "gagarin_seller_notifications"
    const val CHANNEL_SMS = "gagarin_sms_notifications"
    const val CHANNEL_AUTH = "gagarin_auth_notifications"

    private var applicationContext: Context? = null
    private var currentSession: AuthSession = AuthSession()

    private val _inAppNotifications = MutableSharedFlow<InAppNotification>(extraBufferCapacity = 50)
    val inAppNotifications: SharedFlow<InAppNotification> = _inAppNotifications.asSharedFlow()

    fun init(context: Context) {
        applicationContext = context.applicationContext
        initChannel(context)
        loadSessionFromPrefs(context)
    }

    fun updateSession(session: AuthSession) {
        currentSession = session
    }

    private fun loadSessionFromPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences("gagarin_auth_prefs", Context.MODE_PRIVATE)
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
                currentSession = AuthSession(
                    user = initialUser,
                    role = role,
                    isLoggedIn = true
                )
            } else {
                currentSession = AuthSession()
            }
        } catch (_: Exception) {}
    }

    fun initChannel(context: Context) {
        applicationContext = context.applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            // 1. Orders Status Channel (Customer updates like "Tayyorlanmoqda", "Yetkazilmoqda")
            val orderChannel = NotificationChannel(
                CHANNEL_ORDERS,
                "Gagarin Buyurtmalar Holati",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Buyurtmalaringiz tayyorlanishi va yetkazilishi haqida real vaqt bildirishnomalari"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 150, 350)
                enableLights(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // 2. Seller Channel (New orders for marketplace sellers & restaurants)
            val sellerChannel = NotificationChannel(
                CHANNEL_SELLER,
                "Gagarin Sotuvchilar: Yangi Buyurtmalar",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Do‘kon va restoranlar uchun yangi tushgan buyurtmalar signali"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                enableLights(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // 3. SMS Channel
            val smsChannel = NotificationChannel(
                CHANNEL_SMS,
                "Gagarin SMS Tasdiqlash Kodlari",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Tizimga kirish va telefonni tasdiqlash kodlari"
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // 4. Account & Security Channel
            val authChannel = NotificationChannel(
                CHANNEL_AUTH,
                "Gagarin Akkaunt & Xavfsizlik",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Akkauntga kirish va xavfsizlik xabarnomalari"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250)
                enableLights(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(orderChannel)
            notificationManager.createNotificationChannel(sellerChannel)
            notificationManager.createNotificationChannel(smsChannel)
            notificationManager.createNotificationChannel(authChannel)
        }
    }

    /**
     * Checks whether the notification should be delivered to the currently logged in user.
     * Rules:
     * - SMS / General public notifications are always delivered.
     * - Seller notifications are ONLY delivered if current user is logged in as a SELLER / FOOD_SELLER or ADMIN.
     *   If the seller has logged out, NO seller notification is delivered.
     * - Admin notifications are ONLY delivered if current user is logged in as an ADMIN.
     * - Customer notifications targeted to a specific userId are ONLY delivered if current logged in user ID matches targetUserId.
     */
    fun shouldDeliverNotification(targetRole: String?, targetUserId: Long?): Boolean {
        if (targetRole == "SMS" || targetRole == "ALL" || targetRole == null) {
            return true
        }

        val session = currentSession
        val isLoggedIn = session.isLoggedIn
        val currentRole = session.role
        val currentUserId = session.user?.id

        return when (targetRole) {
            "AUTH", "AUTH_LOGIN", "AUTH_EVENT" -> {
                if (targetUserId != null) {
                    isLoggedIn && (currentUserId == targetUserId)
                } else {
                    isLoggedIn
                }
            }
            "SELLER", "BOZOR_SELLER" -> {
                // Must be currently logged in as a marketplace seller, bozor seller, or admin
                if (!isLoggedIn) return false
                val isSellerOrAdmin = currentRole == UserRole.SELLER ||
                        currentRole == UserRole.BOZOR_SELLER ||
                        currentRole == UserRole.ADMIN ||
                        currentRole == UserRole.SUPER_ADMIN
                if (!isSellerOrAdmin) return false
                if (targetUserId != null && currentRole != UserRole.ADMIN && currentRole != UserRole.SUPER_ADMIN) {
                    currentUserId == targetUserId
                } else {
                    true
                }
            }
            "FOOD_SELLER", "FOODS_SELLER" -> {
                // Must be currently logged in as foods seller or admin
                if (!isLoggedIn) return false
                val isFoodSellerOrAdmin = currentRole == UserRole.FOODS_SELLER ||
                        currentRole == UserRole.FOODS_ADMIN ||
                        currentRole == UserRole.ADMIN ||
                        currentRole == UserRole.SUPER_ADMIN
                if (!isFoodSellerOrAdmin) return false
                if (targetUserId != null && currentRole != UserRole.ADMIN && currentRole != UserRole.SUPER_ADMIN && currentRole != UserRole.FOODS_ADMIN) {
                    currentUserId == targetUserId
                } else {
                    true
                }
            }
            "ADMIN", "SUPER_ADMIN" -> {
                isLoggedIn && (currentRole == UserRole.ADMIN || currentRole == UserRole.SUPER_ADMIN)
            }
            "FOODS_ADMIN" -> {
                isLoggedIn && (currentRole == UserRole.FOODS_ADMIN || currentRole == UserRole.ADMIN || currentRole == UserRole.SUPER_ADMIN)
            }
            "CUSTOMER" -> {
                if (targetUserId != null) {
                    // Critical rule: If targeted to a specific customer, deliver ONLY to that customer!
                    isLoggedIn && (currentUserId == targetUserId)
                } else {
                    isLoggedIn && currentRole == UserRole.CUSTOMER
                }
            }
            else -> true
        }
    }

    /**
     * Real system notification triggered when user logs into their account.
     */
    fun showAuthLoginNotification(
        user: UserEntity,
        role: UserRole,
        context: Context? = null
    ) {
        val (title, message, channelId) = when (role) {
            UserRole.SELLER, UserRole.BOZOR_SELLER -> Triple(
                "🏪 Sotuvchi Kabinetiga Xush Kelibsiz!",
                "${user.storeName.ifBlank { user.fullName }} do‘koni faollashtirildi. Yangi tushgan buyurtmalar shu yerda ko‘rinadi.",
                CHANNEL_AUTH
            )
            UserRole.FOODS_SELLER -> Triple(
                "🍔 Oshxona / Restoran Kabinetiga Xush Kelibsiz!",
                "${user.storeName.ifBlank { user.fullName }} muvaffaqiyatli kirdi. Buyurtmalarni qabul qilishga tayyorsiz!",
                CHANNEL_AUTH
            )
            UserRole.ADMIN, UserRole.SUPER_ADMIN -> Triple(
                "🛡️ Administrator Paneli Faollashdi",
                "Assalomu alaykum, Administrator! Gagarin Go tizimi to‘liq nazoratingiz ostida.",
                CHANNEL_AUTH
            )
            UserRole.FOODS_ADMIN -> Triple(
                "🥗 Fast Food Admin Paneli Faollashdi",
                "Fast Food va restoranlar boshqaruv paneliga muvaffaqiyatli kirdingiz.",
                CHANNEL_AUTH
            )
            else -> Triple(
                "🎉 Xush kelibsiz, ${user.fullName.ifBlank { "Qadrli Mijoz" }}!",
                "Gagarin Go ilovasiga muvaffaqiyatli kirdingiz. Qulay xaridlar va bepul yetkazib berish xizmatingizda!",
                CHANNEL_AUTH
            )
        }

        val targetRoleStr = when (role) {
            UserRole.SELLER, UserRole.BOZOR_SELLER -> "SELLER"
            UserRole.FOODS_SELLER -> "FOOD_SELLER"
            UserRole.ADMIN, UserRole.SUPER_ADMIN -> "ADMIN"
            UserRole.FOODS_ADMIN -> "FOODS_ADMIN"
            else -> "CUSTOMER"
        }

        showNotification(
            context = context,
            title = title,
            message = message,
            targetRole = targetRoleStr,
            targetUserId = user.id,
            channelOverride = channelId,
            notificationId = 1002
        )
    }

    fun showSmsNotification(context: Context? = null, phone: String, code: String) {
        val ctx = context ?: applicationContext
        val title = "📩 Gagarin Go: SMS Kod"
        val message = "Sizning tasdiqlash kodingiz: $code. Ushbu kodni hech kimga bermang!"

        // Emit to Compose in-app notification flow
        _inAppNotifications.tryEmit(
            InAppNotification(
                title = title,
                message = message,
                code = code,
                targetRole = "SMS"
            )
        )

        if (ctx == null) return

        try {
            initChannel(ctx)
            val intent = Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                ctx,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notification = NotificationCompat.Builder(ctx, CHANNEL_SMS)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText("Tasdiqlash kodi: $code")
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setSound(soundUri)
                .setVibrate(longArrayOf(0, 300, 200, 300))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(1001, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing SMS notification: ${e.message}")
        }
    }

    fun showNotification(
        context: Context? = null,
        title: String,
        message: String,
        targetRole: String? = null,
        targetUserId: Long? = null,
        notificationId: Int? = null,
        channelOverride: String? = null
    ) {
        val ctx = context ?: applicationContext

        // Strict role & user session check
        if (!shouldDeliverNotification(targetRole, targetUserId)) {
            Log.d(TAG, "Notification suppressed by role/user session filter (targetRole=$targetRole, targetUserId=$targetUserId)")
            return
        }

        // Emit to Compose in-app notification flow
        _inAppNotifications.tryEmit(
            InAppNotification(
                title = title,
                message = message,
                targetRole = targetRole,
                targetUserId = targetUserId
            )
        )

        if (ctx == null) return

        try {
            initChannel(ctx)

            val isSeller = targetRole == "SELLER" || targetRole == "FOOD_SELLER" || targetRole == "FOODS_SELLER"
            val channelId = channelOverride ?: if (isSeller) CHANNEL_SELLER else CHANNEL_ORDERS

            val intent = Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_TARGET_ROLE", targetRole)
                putExtra("EXTRA_NOTIF_TITLE", title)
            }

            val notifId = notificationId ?: (System.currentTimeMillis() % 100000).toInt()
            val pendingIntent = PendingIntent.getActivity(
                ctx,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val iconRes = if (isSeller) android.R.drawable.stat_notify_more else android.R.drawable.stat_notify_sync
            val soundUri = RingtoneManager.getDefaultUri(
                if (isSeller) RingtoneManager.TYPE_RINGTONE else RingtoneManager.TYPE_NOTIFICATION
            )

            val builder = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(if (isSeller) NotificationCompat.CATEGORY_EVENT else NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(if (isSeller) longArrayOf(0, 500, 200, 500, 200, 500) else longArrayOf(0, 350, 150, 350))
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(notifId, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error posting system notification: ${e.message}")
        }
    }
}
