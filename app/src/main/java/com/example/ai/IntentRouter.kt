package com.example.ai

enum class TaskIntent(val titleAr: String, val category: String) {
    WRITING("كتابة ومحتوى", "writing"),
    IMAGE_GENERATION("توليد وتصميم صور", "image"),
    IMAGE_ANALYSIS("تحليل صور", "image"),
    STUDY("دراسة وشرح", "study"),
    CODING("برمجة وأكواد", "coding"),
    DATA_ANALYSIS("تحليل بيانات", "data"),
    SEARCH("بحث وتوثيق", "search"),
    VIDEO_SCRIPT("سكريبت فيديو", "video"),
    AUDIO("صوتيات وتفريغ", "audio"),
    FILE_ANALYSIS("تحليل مستندات", "file"),
    GENERAL_CHAT("محادثة ذكية", "chat")
}

object IntentRouter {

    fun detectIntent(prompt: String, hasImage: Boolean, hasFile: Boolean, hasAudio: Boolean): TaskIntent {
        val p = prompt.trim().lowercase()

        if (hasImage && (p.contains("حلل") || p.contains("اشرح") || p.contains("شنو") || p.contains("ما هذا") || p.isEmpty())) {
            return TaskIntent.IMAGE_ANALYSIS
        }
        if (hasFile) {
            return TaskIntent.FILE_ANALYSIS
        }
        if (hasAudio) {
            return TaskIntent.AUDIO
        }

        // Image generation keywords
        if (p.contains("صورة") || p.contains("صمم لي") || p.contains("ارسم") || p.contains("بوستر") || p.contains("شعار") || p.contains("توليد صورة") || p.contains("خلفية")) {
            return TaskIntent.IMAGE_GENERATION
        }

        // Video keywords
        if (p.contains("فيديو") || p.contains("سكريبت") || p.contains("تيك توك") || p.contains("ريلز") || p.contains("ستوري بورد")) {
            return TaskIntent.VIDEO_SCRIPT
        }

        // Coding keywords
        if (p.contains("كود") || p.contains("برمج") || p.contains("دالة") || p.contains("function") || p.contains("error") ||
            p.contains("بايثون") || p.contains("python") || p.contains("kotlin") || p.contains("javascript") || p.contains("sql") ||
            p.contains("debug") || p.contains("خطأ في الكود") || p.contains("api")) {
            return TaskIntent.CODING
        }

        // Data analysis keywords
        if (p.contains("بيانات") || p.contains("جدول") || p.contains("إكسل") || p.contains("csv") || p.contains("احصائيات") ||
            p.contains("مبيعات") || p.contains("نسبة") || p.contains("ارقام")) {
            return TaskIntent.DATA_ANALYSIS
        }

        // Study keywords
        if (p.contains("درس") || p.contains("فيزياء") || p.contains("كيمياء") || p.contains("رياضيات") || p.contains("تاريخ") ||
            p.contains("امتحان") || p.contains("اختبار") || p.contains("اشرح لي مفهوم") || p.contains("قانون") || p.contains("لخص لي")) {
            return TaskIntent.STUDY
        }

        // Search keywords
        if (p.contains("سعر اليوم") || p.contains("أخبار") || p.contains("اليوم") || p.contains("طقس") || p.contains("مباريات") ||
            p.contains("من فاز") || p.contains("أحدث")) {
            return TaskIntent.SEARCH
        }

        // Writing keywords
        if (p.contains("اكتب لي") || p.contains("إعلان") || p.contains("بوست") || p.contains("رسالة") || p.contains("ايميل") ||
            p.contains("صيغ") || p.contains("مقال") || p.contains("ترجم") || p.contains("تسويق") || p.contains("عنوان جذّاب")) {
            return TaskIntent.WRITING
        }

        return TaskIntent.GENERAL_CHAT
    }

    const val SYSTEM_BEHAVIOR_PROMPT = """
أنت «أنجز AI» — المساعد الذكي الشامل للهاتف، الموجه للعالم العربي والسودان.
الشعار الأساسي: «أها، داير تنجز شنو؟»

قواعد السلوك الصارمة التي يجب أن تلتزم بها حرفياً:
1. الصدق التام:
   - إذا كنت لا تعرف الإجابة: قل بوضوح «ما بعرف.»
   - إذا كنت غير متأكد: قل «ما متأكد.»
   - لا تخترع معلومات أو مصادر أو أرقام أو ملفات وهمية إطلاقاً.
2. استخدام الردود الحقيقية:
   - لا تقل كلمة «تم» إلا إذا نفذت العملية بالفعل وأعطيت النتيجة.
3. الأسلوب:
   - حديث، نظيف، محترم، عملي ومباشر بدون مقدمات طويلة غير مفيدة.
   - يدعم اللهجة السودانية الودية واللغة العربية الفصحى بسلاسة.
   - إذا سألك المستخدم بلهجة معينة أجب بنفس النمط برقي واحترافية.
"""
}
