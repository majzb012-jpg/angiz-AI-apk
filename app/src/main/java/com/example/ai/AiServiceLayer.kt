package com.example.ai

import com.example.models.ServiceInfo
import com.example.models.ServiceState

interface BaseAiService {
    val serviceInfo: ServiceInfo
}

class ChatService : BaseAiService {
    override val serviceInfo = ServiceInfo(
        id = "chat",
        titleAr = "المحادثة الذكية",
        descriptionAr = "المساعد المركزي لتنفيذ الاستفسارات والمهام الذكية",
        iconName = "chat",
        state = ServiceState.AVAILABLE
    )

    suspend fun execute(prompt: String): GeminiResult {
        return GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = IntentRouter.SYSTEM_BEHAVIOR_PROMPT
        )
    }
}

class SearchService : BaseAiService {
    override val serviceInfo = ServiceInfo(
        id = "search",
        titleAr = "البحث الذكي المباشر",
        descriptionAr = "البحث المباشر في الويب وجلب المصادر الموثوقة",
        iconName = "search",
        state = ServiceState.AVAILABLE
    )

    suspend fun search(query: String): GeminiResult {
        val searchPrompt = "ابحث عن أحدث وأدق المعلومات المتعلقة بالتالي: $query\nوقدم ملخصاً شاملاً ودقيقاً مع ذكر المصادر بدقة."
        return GeminiApiClient.generateContent(
            prompt = searchPrompt,
            systemInstruction = "${IntentRouter.SYSTEM_BEHAVIOR_PROMPT}\nاستخدم محرك البحث المباشر وقدم المصادر الحقيقية.",
            enableSearch = true
        )
    }
}

class WritingService : BaseAiService {
    override val serviceInfo = ServiceInfo(
        id = "writing",
        titleAr = "مركز الكتابة والمحتوى",
        descriptionAr = "صياغة المنشورات والإعلانات والمقالات والترجمة بمختلف اللهجات",
        iconName = "edit",
        state = ServiceState.AVAILABLE
    )

    suspend fun generateContent(
        topic: String,
        type: String, // "post", "ad", "email", "article", "grammar", "translate"
        dialect: String // "سوداني", "فصحى", "مصري", "خليجي"
    ): GeminiResult {
        val prompt = "المهمة: كتابة $type\nالموضوع: $topic\nالأسلوب واللهجة المطلوبة: $dialect.\nاكتب محتوى احترافي، جذاب، وجاهز للنشر مباشرة."
        return GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = IntentRouter.SYSTEM_BEHAVIOR_PROMPT
        )
    }
}

class StudyService : BaseAiService {
    override val serviceInfo = ServiceInfo(
        id = "study",
        titleAr = "مركز الدراسة والتعليم",
        descriptionAr = "شرح الدروس وتبسيط المفاهيم وإنشاء أسئلة واختبارات تفاعلية",
        iconName = "school",
        state = ServiceState.AVAILABLE
    )

    suspend fun explainConcept(topic: String, level: String = "مبسط"): GeminiResult {
        val prompt = "اشرح لي الدرس أو المفهوم التالي: $topic\nمستوى الشرح المطلوب: $level.\nقم بالتقسيم: 1. نظرة عامة، 2. شرح مبسط خطوة بخطوة، 3. أمثلة عملية، 4. أسئلة للتأكد من الفهم."
        return GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = IntentRouter.SYSTEM_BEHAVIOR_PROMPT
        )
    }
}

class CodingService : BaseAiService {
    override val serviceInfo = ServiceInfo(
        id = "coding",
        titleAr = "مركز البرمجة والأكواد",
        descriptionAr = "كتابة الأكواد وحل الأخطاء وشرحها لمختلف لغات البرمجة",
        iconName = "code",
        state = ServiceState.AVAILABLE
    )

    suspend fun processCode(request: String, language: String): GeminiResult {
        val prompt = "لغة البرمجة: $language\nالطلب: $request\nقدم كوداً نظيفاً مع شرح دقيق وكيفية التشغيل وكتل الكود المنظمة."
        return GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = IntentRouter.SYSTEM_BEHAVIOR_PROMPT
        )
    }
}

class ImageService : BaseAiService {
    override val serviceInfo = ServiceInfo(
        id = "image",
        titleAr = "مركز الصور والتصميم",
        descriptionAr = "تحليل الصور، واقتراح أفكار التصاميم والبوسترات بالذكاء الاصطناعي",
        iconName = "image",
        state = ServiceState.AVAILABLE
    )

    suspend fun analyzeImage(prompt: String, imageBase64: String): GeminiResult {
        return GeminiApiClient.generateContent(
            prompt = prompt.ifBlank { "حلل هذه الصورة بالتفصيل واشرح كل العناصر والنصوص والألوان الموجودة فيها." },
            systemInstruction = IntentRouter.SYSTEM_BEHAVIOR_PROMPT,
            imageBase64 = imageBase64
        )
    }

    suspend fun designPrompt(prompt: String, style: String): GeminiResult {
        val genPrompt = "صمم وصفاً بصرياً دقيقاً (Visual Generation Prompt) لإنشاء صورة أو بوستر: $prompt بالأسلوب الفني: $style."
        return GeminiApiClient.generateContent(
            prompt = genPrompt,
            systemInstruction = IntentRouter.SYSTEM_BEHAVIOR_PROMPT
        )
    }
}

class FileService : BaseAiService {
    override val serviceInfo = ServiceInfo(
        id = "file",
        titleAr = "مركز المستندات والملفات",
        descriptionAr = "تلخيص وتحليل ملفات PDF و DOCX و TXT واستخراج البيانات",
        iconName = "description",
        state = ServiceState.AVAILABLE
    )

    suspend fun analyzeDocument(fileName: String, contentText: String, userQuestion: String): GeminiResult {
        val prompt = "اسم الملف: $fileName\nمحتوى المستند:\n$contentText\n\nطلب المستخدم حول الملف: $userQuestion\nقم بتقديم إجابة دقيقة مستندة تماماً للمستند."
        return GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = IntentRouter.SYSTEM_BEHAVIOR_PROMPT
        )
    }
}

class DataAnalysisService : BaseAiService {
    override val serviceInfo = ServiceInfo(
        id = "data",
        titleAr = "تحليل البيانات والإحصاء",
        descriptionAr = "تحليل الجداول وملفات CSV واستخراج الإحصائيات المهمة",
        iconName = "analytics",
        state = ServiceState.AVAILABLE
    )

    suspend fun analyzeData(dataSnippet: String, inquiry: String): GeminiResult {
        val prompt = "البيانات:\n$dataSnippet\n\nالمطلوب تحليله: $inquiry\nقم بتنظيف وتلخيص الأرقام، وحساب النسب، وتوضيح النتائج بلغة بسيطة وواضحة."
        return GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = IntentRouter.SYSTEM_BEHAVIOR_PROMPT
        )
    }
}

// Honest Service implementations for external media rendering
class VideoService : BaseAiService {
    override val serviceInfo = ServiceInfo(
        id = "video",
        titleAr = "مركز الفيديو والمونتاج",
        descriptionAr = "توليد سكريبتات المشاهد وتخطيط الستوري بورد (توليد الفيديو الفعلي تحت الربط الخارجي)",
        iconName = "videocam",
        state = ServiceState.AVAILABLE
    )

    suspend fun generateScriptAndStoryboard(topic: String, durationSec: Int): GeminiResult {
        val prompt = "اكتب سيناريو وسكريبت فيديو متكامل ومفصل حول: $topic\nالمدة التقريبية: $durationSec ثانية.\nقسّم الرد إلى: 1. المشهد البصري (Visuals)، 2. النص الصوتي (Voiceover)، 3. المؤثرات المقترحة."
        return GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = IntentRouter.SYSTEM_BEHAVIOR_PROMPT
        )
    }

    fun getVideoRenderStateNotice(): String {
        return "توليد ملفات الفيديو المباشرة بصيغة MP4 يتطلب ربط نموذج خارجي (Veo Render Worker). خدمة كتابة وتخطيط المشاهد والسكريبت متاحة وتعمل الآن."
    }
}

class AudioService : BaseAiService {
    override val serviceInfo = ServiceInfo(
        id = "audio",
        titleAr = "مركز الصوت والتفريغ",
        descriptionAr = "تحويل الصوت إلى نص وتلخيص التسجيلات الصوتية",
        iconName = "mic",
        state = ServiceState.AVAILABLE
    )

    suspend fun summarizeTranscript(transcript: String): GeminiResult {
        val prompt = "قم بتفريغ وتلخيص التسجيل الصوتي التالي واستخراج النقاط المهمة والتوصيات:\n$transcript"
        return GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = IntentRouter.SYSTEM_BEHAVIOR_PROMPT
        )
    }
}

object AiServiceRegistry {
    val chatService = ChatService()
    val searchService = SearchService()
    val writingService = WritingService()
    val studyService = StudyService()
    val codingService = CodingService()
    val imageService = ImageService()
    val fileService = FileService()
    val dataAnalysisService = DataAnalysisService()
    val videoService = VideoService()
    val audioService = AudioService()

    val allServices: List<BaseAiService> = listOf(
        chatService,
        writingService,
        imageService,
        codingService,
        studyService,
        searchService,
        fileService,
        dataAnalysisService,
        videoService,
        audioService
    )
}
