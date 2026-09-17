package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String = if (email.trim().equals("majzb012@gmail.com", ignoreCase = true)) "admin" else "user",
    val planId: String = "free",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAdmin: Boolean get() = role == "admin" || email.trim().equals("majzb012@gmail.com", ignoreCase = true)
}

data class Plan(
    val id: String,
    val name: String,
    val priceSdg: Int,
    val aiDailyLimit: Int,
    val imageDailyLimit: Int,
    val videoDailyLimit: Int,
    val badge: String,
    val features: List<String>
) {
    companion object {
        val PLANS = listOf(
            Plan(
                id = "free",
                name = "مجاني",
                priceSdg = 0,
                aiDailyLimit = 10,
                imageDailyLimit = 5,
                videoDailyLimit = 3,
                badge = "البداية",
                features = listOf(
                    "10 عمليات ذكاء اصطناعي يومياً",
                    "5 صور مرفوعة يومياً",
                    "3 فيديوهات يومياً",
                    "وصول للمحادثة المركزية والبحث"
                )
            ),
            Plan(
                id = "basic",
                name = "أساسي",
                priceSdg = 20000,
                aiDailyLimit = 50,
                imageDailyLimit = 20,
                videoDailyLimit = 10,
                badge = "الأكثر طلباً",
                features = listOf(
                    "50 عملية ذكاء اصطناعي يومياً",
                    "20 صورة يومياً",
                    "10 فيديوهات يومياً",
                    "أولوية معالجة في النماذج السريعة",
                    "توليد نصوص متقدم بدون انقطاع"
                )
            ),
            Plan(
                id = "advanced",
                name = "متقدم",
                priceSdg = 40000,
                aiDailyLimit = 100,
                imageDailyLimit = 30,
                videoDailyLimit = 20,
                badge = "احترافي",
                features = listOf(
                    "100 عملية ذكاء اصطناعي يومياً",
                    "30 صورة يومياً",
                    "20 فيديو يومياً",
                    "مساعد دراسة وبرمجة وتحليل ملفات",
                    "دعم فني سريع"
                )
            ),
            Plan(
                id = "pro",
                name = "احترافي",
                priceSdg = 60000,
                aiDailyLimit = 200,
                imageDailyLimit = 50,
                videoDailyLimit = 30,
                badge = "الأقصى",
                features = listOf(
                    "200 عملية ذكاء اصطناعي يومياً",
                    "50 صورة يومياً",
                    "30 فيديو يومياً",
                    "الحد الأقصى لسرعة الاستجابة",
                    "وصول كامل لكل الأدوات ومراكز العمل"
                )
            )
        )

        fun getPlan(id: String): Plan = PLANS.find { it.id == id } ?: PLANS.first()
    }
}

@Entity(tableName = "usage_records")
data class UsageRecord(
    @PrimaryKey val id: String, // format: "userId_YYYY-MM-DD"
    val userId: String,
    val dateKey: String,
    val aiCount: Int = 0,
    val imageCount: Int = 0,
    val videoCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_requests")
data class PaymentRequest(
    @PrimaryKey val id: String,
    val userId: String,
    val userEmail: String,
    val userName: String,
    val planId: String,
    val amountSdg: Int,
    val kashiNumber: String = "402903869",
    val receiptReference: String, // phone or txn number or note
    val receiptImageUri: String? = null,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val adminNote: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val conversationId: String,
    val userId: String,
    val role: String, // "user" or "assistant"
    val text: String,
    val serviceCategory: String = "chat", // "chat", "writing", "image", "code", "study", "data", "search"
    val attachmentUri: String? = null,
    val attachmentType: String? = null, // "image", "file", "audio"
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val sourcesJson: String? = null
)

data class SourceCitation(
    val title: String,
    val sourceName: String,
    val url: String
)

enum class ServiceState(val labelAr: String) {
    AVAILABLE("متاح"),
    UNAVAILABLE("غير متاح حالياً"),
    MAINTENANCE("تحت الصيانة")
}

data class ServiceInfo(
    val id: String,
    val titleAr: String,
    val descriptionAr: String,
    val iconName: String,
    val state: ServiceState
)
