package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiServiceRegistry
import com.example.ai.IntentRouter
import com.example.ai.TaskIntent
import com.example.core.NetworkHelper
import com.example.models.ChatMessage
import com.example.models.PaymentRequest
import com.example.models.Plan
import com.example.models.UsageRecord
import com.example.models.User
import com.example.repositories.AppDatabase
import com.example.repositories.ChatRepository
import com.example.repositories.PaymentRepository
import com.example.repositories.UsageRepository
import com.example.repositories.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val userRepo = UserRepository(db.userDao())
    val usageRepo = UsageRepository(db.usageDao())
    val paymentRepo = PaymentRepository(db.paymentDao(), db.userDao())
    val chatRepo = ChatRepository(db.chatDao())

    val currentUser: StateFlow<User?> = userRepo.currentUser

    private val _currentConversationId = MutableStateFlow(UUID.randomUUID().toString())
    val currentConversationId: StateFlow<String> = _currentConversationId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentGeneratingText = MutableStateFlow("")
    val currentGeneratingText: StateFlow<String> = _currentGeneratingText.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _hasSeenOnboarding = MutableStateFlow(false)
    val hasSeenOnboarding: StateFlow<Boolean> = _hasSeenOnboarding.asStateFlow()

    private val _currentUsage = MutableStateFlow<UsageRecord?>(null)
    val currentUsage: StateFlow<UsageRecord?> = _currentUsage.asStateFlow()

    val allPayments: StateFlow<List<PaymentRequest>> = paymentRepo.allPaymentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = userRepo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkNetwork()
        val prefs = application.getSharedPreferences("anjez_prefs", Context.MODE_PRIVATE)
        _hasSeenOnboarding.value = prefs.getBoolean("seen_onboarding", false)
        _isDarkMode.value = prefs.getBoolean("dark_mode", true)

        viewModelScope.launch {
            userRepo.initDefaultOrSavedUser()
            refreshUsage()
            loadConversationMessages(_currentConversationId.value)
        }
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        getApplication<Application>().getSharedPreferences("anjez_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("dark_mode", dark).apply()
    }

    fun completeOnboarding() {
        _hasSeenOnboarding.value = true
        getApplication<Application>().getSharedPreferences("anjez_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("seen_onboarding", true).apply()
    }

    fun checkNetwork() {
        _isOffline.value = !NetworkHelper.isOnline(getApplication())
    }

    fun dismissStatusMessage() {
        _statusMessage.value = null
    }

    fun showToast(msg: String) {
        _statusMessage.value = msg
    }

    suspend fun refreshUsage() {
        val user = currentUser.value ?: return
        _currentUsage.value = usageRepo.getCurrentUsage(user.id)
    }

    fun loadConversationMessages(convId: String) {
        _currentConversationId.value = convId
        viewModelScope.launch {
            chatRepo.getMessagesFlow(convId).collect {
                _messages.value = it
            }
        }
    }

    fun startNewConversation() {
        val newId = UUID.randomUUID().toString()
        _currentConversationId.value = newId
        _messages.value = emptyList()
        _currentGeneratingText.value = ""
    }

    fun sendMessage(
        prompt: String,
        attachmentUri: String? = null,
        attachmentType: String? = null,
        forceService: String? = null
    ) {
        val user = currentUser.value ?: return
        val text = prompt.trim()
        if (text.isBlank() && attachmentUri == null) return

        checkNetwork()

        viewModelScope.launch {
            val plan = Plan.getPlan(user.planId)
            val intent = if (forceService != null) {
                when (forceService) {
                    "writing" -> TaskIntent.WRITING
                    "image" -> TaskIntent.IMAGE_GENERATION
                    "code" -> TaskIntent.CODING
                    "study" -> TaskIntent.STUDY
                    "search" -> TaskIntent.SEARCH
                    "video" -> TaskIntent.VIDEO_SCRIPT
                    "audio" -> TaskIntent.AUDIO
                    "data" -> TaskIntent.DATA_ANALYSIS
                    "file" -> TaskIntent.FILE_ANALYSIS
                    else -> TaskIntent.GENERAL_CHAT
                }
            } else {
                IntentRouter.detectIntent(
                    prompt = text,
                    hasImage = attachmentType == "image",
                    hasFile = attachmentType == "file",
                    hasAudio = attachmentType == "audio"
                )
            }

            // Quota check
            val quotaType = when (intent) {
                TaskIntent.IMAGE_GENERATION, TaskIntent.IMAGE_ANALYSIS -> "image"
                TaskIntent.VIDEO_SCRIPT -> "video"
                else -> "ai"
            }

            val quotaResult = usageRepo.checkAndConsumeQuota(user.id, plan, quotaType)
            if (quotaResult.isFailure) {
                _statusMessage.value = quotaResult.exceptionOrNull()?.message
                return@launch
            }
            refreshUsage()

            // Save user message
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = _currentConversationId.value,
                userId = user.id,
                role = "user",
                text = text,
                serviceCategory = intent.category,
                attachmentUri = attachmentUri,
                attachmentType = attachmentType,
                timestamp = System.currentTimeMillis()
            )
            chatRepo.saveMessage(userMsg)

            // Prepare AI execution
            _isGenerating.value = true
            _currentGeneratingText.value = ""

            val aiResult = when (intent) {
                TaskIntent.SEARCH -> AiServiceRegistry.searchService.search(text)
                TaskIntent.WRITING -> AiServiceRegistry.writingService.generateContent(text, "منشور أو إعلان", "سوداني/عربي")
                TaskIntent.STUDY -> AiServiceRegistry.studyService.explainConcept(text)
                TaskIntent.CODING -> AiServiceRegistry.codingService.processCode(text, "لغة مناسبة")
                TaskIntent.IMAGE_GENERATION -> AiServiceRegistry.imageService.designPrompt(text, "واقعي وعالي الدقة")
                TaskIntent.IMAGE_ANALYSIS -> AiServiceRegistry.imageService.analyzeImage(text, "")
                TaskIntent.VIDEO_SCRIPT -> AiServiceRegistry.videoService.generateScriptAndStoryboard(text, 45)
                TaskIntent.AUDIO -> AiServiceRegistry.audioService.summarizeTranscript(text)
                TaskIntent.FILE_ANALYSIS -> AiServiceRegistry.fileService.analyzeDocument("مستند", text, "لخص واستخرج النقاط")
                TaskIntent.DATA_ANALYSIS -> AiServiceRegistry.dataAnalysisService.analyzeData(text, "تحليل إحصائي وتلخيص")
                TaskIntent.GENERAL_CHAT -> AiServiceRegistry.chatService.execute(text)
            }

            // Simulate smooth streaming reveal for UI delight
            val responseText = aiResult.text
            val words = responseText.split(" ")
            val streamBuilder = StringBuilder()
            for (w in words) {
                streamBuilder.append(w).append(" ")
                _currentGeneratingText.value = streamBuilder.toString()
                delay(12)
            }

            val sourcesJson = if (aiResult.sources.isNotEmpty()) {
                val jsonArr = JSONArray()
                aiResult.sources.forEach { s ->
                    jsonArr.put(org.json.JSONObject().apply {
                        put("title", s.title)
                        put("sourceName", s.sourceName)
                        put("url", s.url)
                    })
                }
                jsonArr.toString()
            } else null

            // Save assistant message
            val assistantMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = _currentConversationId.value,
                userId = user.id,
                role = "assistant",
                text = responseText,
                serviceCategory = intent.category,
                timestamp = System.currentTimeMillis(),
                sourcesJson = sourcesJson
            )
            chatRepo.saveMessage(assistantMsg)

            _isGenerating.value = false
            _currentGeneratingText.value = ""
        }
    }

    fun stopGeneration() {
        _isGenerating.value = false
        _currentGeneratingText.value = ""
    }

    fun toggleFavorite(msg: ChatMessage) {
        viewModelScope.launch {
            chatRepo.toggleFavorite(msg.id, msg.isFavorite)
        }
    }

    fun deleteMessage(msgId: String) {
        viewModelScope.launch {
            chatRepo.deleteMessage(msgId)
        }
    }

    fun submitKashiPayment(planId: String, amount: Int, reference: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = paymentRepo.submitPaymentRequest(user, planId, amount, reference)
            if (res.isSuccess) {
                _statusMessage.value = "تم إرسال طلب الدفع بنجاح! سيقوم المدير بمراجعة الإيصال وتفعيل الباقة."
            } else {
                _statusMessage.value = res.exceptionOrNull()?.message ?: "حدث خطأ أثناء إرسال الطلب."
            }
        }
    }

    fun approvePayment(payment: PaymentRequest) {
        val user = currentUser.value ?: return
        if (!user.isAdmin) {
            _statusMessage.value = "غير مصرح لك بتأكيد المدفوعات."
            return
        }
        viewModelScope.launch {
            paymentRepo.approvePayment(payment)
            userRepo.refreshCurrentUserData()
            refreshUsage()
            _statusMessage.value = "تم تأكيد الدفع وتفعيل الباقة للمستخدم ${payment.userName}."
        }
    }

    fun rejectPayment(payment: PaymentRequest) {
        val user = currentUser.value ?: return
        if (!user.isAdmin) {
            _statusMessage.value = "غير مصرح لك برفض المدفوعات."
            return
        }
        viewModelScope.launch {
            paymentRepo.rejectPayment(payment)
            _statusMessage.value = "تم رفض طلب الدفع."
        }
    }
}
