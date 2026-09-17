package com.example.repositories

import com.example.models.ChatMessage
import com.example.models.PaymentRequest
import com.example.models.Plan
import com.example.models.UsageRecord
import com.example.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class UserRepository(private val userDao: UserDao) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun initDefaultOrSavedUser(defaultEmail: String = "majzb012@gmail.com", defaultName: String = "مجذوب (المدير)") {
        var user = userDao.getUser(defaultEmail)
        if (user == null) {
            val role = if (defaultEmail.equals("majzb012@gmail.com", ignoreCase = true)) "admin" else "user"
            user = User(
                id = defaultEmail,
                name = defaultName,
                email = defaultEmail,
                role = role,
                planId = if (role == "admin") "pro" else "free"
            )
            userDao.insertUser(user)
        }
        _currentUser.value = user
    }

    suspend fun login(email: String, name: String = ""): Result<User> {
        val cleanEmail = email.trim().lowercase(Locale.US)
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("البريد الإلكتروني غير صحيح."))
        }
        var user = userDao.getUser(cleanEmail)
        if (user == null) {
            val role = if (cleanEmail.equals("majzb012@gmail.com", ignoreCase = true)) "admin" else "user"
            val displayName = name.ifBlank { cleanEmail.substringBefore("@") }
            user = User(
                id = cleanEmail,
                name = displayName,
                email = cleanEmail,
                role = role,
                planId = if (role == "admin") "pro" else "free"
            )
            userDao.insertUser(user)
        }
        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun register(name: String, email: String, password: String, confirmPass: String): Result<User> {
        val cleanEmail = email.trim().lowercase(Locale.US)
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("الرجاء كتابة الاسم الكامل."))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("البريد الإلكتروني غير صحيح."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("كلمة المرور يجب أن تكون 6 أحرف على الأقل."))
        }
        if (password != confirmPass) {
            return Result.failure(IllegalArgumentException("كلمتا المرور غير متطابقتين."))
        }

        val role = if (cleanEmail.equals("majzb012@gmail.com", ignoreCase = true)) "admin" else "user"
        val user = User(
            id = cleanEmail,
            name = name.trim(),
            email = cleanEmail,
            role = role,
            planId = if (role == "admin") "pro" else "free"
        )
        userDao.insertUser(user)
        _currentUser.value = user
        return Result.success(user)
    }

    fun logout() {
        _currentUser.value = null
    }

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsersFlow()

    suspend fun refreshCurrentUserData() {
        val current = _currentUser.value ?: return
        val fresh = userDao.getUser(current.id)
        if (fresh != null) {
            _currentUser.value = fresh
        }
    }
}

class UsageRepository(private val usageDao: UsageDao) {

    private fun getTodayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun getRecordId(userId: String): String {
        return "${userId}_${getTodayKey()}"
    }

    suspend fun getCurrentUsage(userId: String): UsageRecord {
        val id = getRecordId(userId)
        val existing = usageDao.getUsage(id)
        if (existing != null) return existing
        val newRecord = UsageRecord(id = id, userId = userId, dateKey = getTodayKey())
        usageDao.insertUsage(newRecord)
        return newRecord
    }

    fun getUsageFlow(userId: String): Flow<UsageRecord?> {
        return usageDao.getUsageFlow(getRecordId(userId))
    }

    suspend fun checkAndConsumeQuota(userId: String, plan: Plan, type: String): Result<Unit> {
        val record = getCurrentUsage(userId)
        when (type) {
            "ai" -> {
                if (record.aiCount >= plan.aiDailyLimit) {
                    return Result.failure(IllegalStateException("لقد وصلت للحد اليومي لعمليات AI (${plan.aiDailyLimit}). يمكنك ترقية باقتك للاستمرار."))
                }
                usageDao.insertUsage(record.copy(aiCount = record.aiCount + 1, lastUpdated = System.currentTimeMillis()))
            }
            "image" -> {
                if (record.imageCount >= plan.imageDailyLimit) {
                    return Result.failure(IllegalStateException("لقد وصلت للحد اليومي للصور (${plan.imageDailyLimit}). يمكنك ترقية باقتك."))
                }
                usageDao.insertUsage(record.copy(imageCount = record.imageCount + 1, lastUpdated = System.currentTimeMillis()))
            }
            "video" -> {
                if (record.videoCount >= plan.videoDailyLimit) {
                    return Result.failure(IllegalStateException("لقد وصلت للحد اليومي للفيديوهات (${plan.videoDailyLimit}). يمكنك ترقية باقتك."))
                }
                usageDao.insertUsage(record.copy(videoCount = record.videoCount + 1, lastUpdated = System.currentTimeMillis()))
            }
        }
        return Result.success(Unit)
    }
}

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val userDao: UserDao
) {
    val allPaymentsFlow: Flow<List<PaymentRequest>> = paymentDao.getAllPaymentsFlow()

    fun getUserPaymentsFlow(userId: String): Flow<List<PaymentRequest>> =
        paymentDao.getUserPaymentsFlow(userId)

    suspend fun submitPaymentRequest(
        user: User,
        planId: String,
        amountSdg: Int,
        receiptRef: String,
        receiptImageUri: String? = null
    ): Result<PaymentRequest> {
        if (receiptRef.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى كتابة رقم التحويل أو اسم المحوّل للتأكد من الإيصال."))
        }

        val request = PaymentRequest(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            userEmail = user.email,
            userName = user.name,
            planId = planId,
            amountSdg = amountSdg,
            kashiNumber = "402903869",
            receiptReference = receiptRef.trim(),
            receiptImageUri = receiptImageUri,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        paymentDao.insertPayment(request)
        return Result.success(request)
    }

    suspend fun approvePayment(payment: PaymentRequest): Result<Unit> {
        paymentDao.updatePaymentStatus(payment.id, "APPROVED", System.currentTimeMillis())
        userDao.updateUserPlan(payment.userId, payment.planId)
        return Result.success(Unit)
    }

    suspend fun rejectPayment(payment: PaymentRequest): Result<Unit> {
        paymentDao.updatePaymentStatus(payment.id, "REJECTED", System.currentTimeMillis())
        return Result.success(Unit)
    }
}

class ChatRepository(private val chatDao: ChatDao) {
    fun getMessagesFlow(conversationId: String): Flow<List<ChatMessage>> =
        chatDao.getMessagesFlow(conversationId)

    fun getFavoriteMessages(userId: String): Flow<List<ChatMessage>> =
        chatDao.getFavoriteMessagesFlow(userId)

    suspend fun saveMessage(message: ChatMessage) {
        chatDao.insertMessage(message)
    }

    suspend fun toggleFavorite(msgId: String, currentFav: Boolean) {
        chatDao.updateFavorite(msgId, !currentFav)
    }

    suspend fun deleteMessage(msgId: String) {
        chatDao.deleteMessage(msgId)
    }
}
