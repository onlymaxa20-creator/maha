package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdDirect(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE login = :login LIMIT 1")
    suspend fun getUserByLogin(login: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role ORDER BY createdAt DESC")
    fun getUsersByRole(role: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'CUSTOMER' ORDER BY createdAt DESC")
    fun getAllCustomers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role IN ('SELLER', 'FOODS_SELLER') ORDER BY createdAt DESC")
    fun getAllSellers(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER'")
    fun getCustomersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE role IN ('SELLER', 'FOODS_SELLER')")
    fun getSellersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE role IN ('SELLER', 'FOODS_SELLER') AND isActive = 1")
    fun getActiveSellersCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Long)

    @Query("UPDATE users SET phone = :phone WHERE id = :userId")
    suspend fun updatePhone(userId: Long, phone: String)

    @Query("UPDATE users SET savedAddress = :address WHERE id = :userId")
    suspend fun updateSavedAddress(userId: Long, address: String)

    @Query("UPDATE users SET passwordHash = :passwordHash, salt = :salt WHERE id = :userId")
    suspend fun updatePassword(userId: Long, passwordHash: String, salt: String)

    @Query("UPDATE users SET isActive = :isActive WHERE id = :sellerId")
    suspend fun updateSellerStatus(sellerId: Long, isActive: Boolean)
}
