package com.example.repositories

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.models.ChatMessage
import com.example.models.PaymentRequest
import com.example.models.UsageRecord
import com.example.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUser(userId: String): User?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET planId = :planId WHERE id = :userId")
    suspend fun updateUserPlan(userId: String, planId: String)
}

@Dao
interface UsageDao {
    @Query("SELECT * FROM usage_records WHERE id = :recordId LIMIT 1")
    fun getUsageFlow(recordId: String): Flow<UsageRecord?>

    @Query("SELECT * FROM usage_records WHERE id = :recordId LIMIT 1")
    suspend fun getUsage(recordId: String): UsageRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: UsageRecord)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment_requests ORDER BY createdAt DESC")
    fun getAllPaymentsFlow(): Flow<List<PaymentRequest>>

    @Query("SELECT * FROM payment_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserPaymentsFlow(userId: String): Flow<List<PaymentRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(request: PaymentRequest)

    @Query("UPDATE payment_requests SET status = :status, reviewedAt = :reviewedAt WHERE id = :paymentId")
    suspend fun updatePaymentStatus(paymentId: String, status: String, reviewedAt: Long)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesFlow(convId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteMessagesFlow(userId: String): Flow<List<ChatMessage>>

    @Query("SELECT DISTINCT conversationId FROM chat_messages WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserConversationsFlow(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("UPDATE chat_messages SET isFavorite = :isFav WHERE id = :msgId")
    suspend fun updateFavorite(msgId: String, isFav: Boolean)

    @Query("DELETE FROM chat_messages WHERE id = :msgId")
    suspend fun deleteMessage(msgId: String)
}

@Database(
    entities = [User::class, UsageRecord::class, PaymentRequest::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun usageDao(): UsageDao
    abstract fun paymentDao(): PaymentDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anjez_ai_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
