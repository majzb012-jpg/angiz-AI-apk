package com.example.ai

import com.example.BuildConfig
import com.example.models.SourceCitation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiResult(
    val text: String,
    val sources: List<SourceCitation> = emptyList(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
    }

    fun isConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && !key.contains("MY_GEMINI_API_KEY")
    }

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        model: String = "gemini-3.5-flash",
        enableSearch: Boolean = false,
        imageBase64: String? = null
    ): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isConfigured()) {
            return@withContext GeminiResult(
                text = "مرحباً بك في أنجز AI! يرجى إدخال مفتاح GEMINI_API_KEY في لوحة Secrets أو ملف .env لتفعيل الذكاء الاصطناعي الفعلي المباشر.",
                isSuccess = false,
                errorMessage = "API Key not configured"
            )
        }

        try {
            val root = JSONObject()

            // System instruction
            if (!systemInstruction.isNullOrBlank()) {
                val sysContent = JSONObject()
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", systemInstruction))
                sysContent.put("parts", sysParts)
                root.put("systemInstruction", sysContent)
            }

            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            if (!imageBase64.isNullOrBlank()) {
                val inlineData = JSONObject()
                inlineData.put("mimeType", "image/jpeg")
                inlineData.put("data", imageBase64)
                partsArray.put(JSONObject().put("inlineData", inlineData))
            }

            partsArray.put(JSONObject().put("text", prompt))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            // Search tool if requested
            if (enableSearch) {
                val toolsArray = JSONArray()
                val toolObj = JSONObject()
                toolObj.put("googleSearch", JSONObject())
                toolsArray.put(toolObj)
                root.put("tools", toolsArray)
            }

            val requestBody = root.toString().toRequestBody(JSON_MEDIA_TYPE)
            val url = "$BASE_URL$model:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message")
                        ?: "خطأ من خدمة الذكاء الاصطناعي (رمز ${response.code})"
                } catch (e: Exception) {
                    "خطأ أثناء الاتصال بالخدمة (${response.code})"
                }
                return@withContext GeminiResult(
                    text = "حصلت مشكلة أثناء تنفيذ العملية. حاول مرة تانية.",
                    isSuccess = false,
                    errorMessage = errorMsg
                )
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiResult(
                    text = "لم يتم الحصول على إجابة مناسبة.",
                    isSuccess = false
                )
            }

            val firstCandidate = candidates.getJSONObject(0)
            val parts = firstCandidate.optJSONObject("content")?.optJSONArray("parts")
            val sb = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    if (p.has("text")) {
                        sb.append(p.getString("text"))
                    }
                }
            }

            // Extract search citations if groundingMetadata is returned
            val sourcesList = mutableListOf<SourceCitation>()
            val grounding = firstCandidate.optJSONObject("groundingMetadata")
            if (grounding != null) {
                val chunks = grounding.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val web = chunks.getJSONObject(i).optJSONObject("web")
                        if (web != null) {
                            val uri = web.optString("uri")
                            val title = web.optString("title", "مصدر ويب")
                            val domain = try {
                                java.net.URI(uri).host ?: "web"
                            } catch (e: Exception) {
                                "web"
                            }
                            sourcesList.add(SourceCitation(title = title, sourceName = domain, url = uri))
                        }
                    }
                }
            }

            GeminiResult(
                text = sb.toString().ifBlank { "تم تنفيذ الطلب بنجاح." },
                sources = sourcesList,
                isSuccess = true
            )
        } catch (e: Exception) {
            GeminiResult(
                text = "حصلت مشكلة أثناء تنفيذ العملية. تأكد من اتصال الإنترنت وحاول مرة تانية.",
                isSuccess = false,
                errorMessage = e.message
            )
        }
    }
}
