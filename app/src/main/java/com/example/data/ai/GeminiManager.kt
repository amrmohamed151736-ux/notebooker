package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AiModelOption(val modelId: String, val label: String, val description: String) {
    ARGON_THINKING(
        "gemini-4-argon",
        "Gemini 4 Argon (تفكير عالي)",
        "للتحليل الأكاديمي والبروتوكولات المعقدة وتصميم المناهج"
    ),
    FLASH_BALANCED(
        "gemini-3.8-flash",
        "Gemini 3.8 Flash (متوازن)",
        "للمهام العامة والتخطيط اليومي السريع والملاحظات"
    ),
    FLASH_LITE(
        "gemini-3.1-flash-lite",
        "Gemini 3.1 Flash-Lite (فائق السرعة)",
        "للعصف الذهني الفوري والاقتراحات الخاطفة"
    )
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null
)

object GeminiManager {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    val systemInstruction = """
        أنت NoteForge Architect ومستشار علوم الحياة والأبحاث الأكاديمية (Bioscience & Notebook Research Advisor).
        دورك:
        1. مساعدة الباحثين والطلاب والمعلمين في هيكلة دفاتر الملاحظات، وسجلات التجارب المخبرية، وجداول القياسات البيولوجية والكيميائية.
        2. تقديم أسئلة وملاحظات تلخيصية لنظام كورنيل (Cornell Cues & Questions).
        3. اقتراح هياكل تخطيط عادات ومهام إنتاجية قابلة للطباعة.
        4. اقتراح برومبتات وأفكار أغلفة فنية ملهمة للنوت بوك.
        كن دقيقاً، منظماً، وأعطِ إجابات عملية جاهزة للتطبيق.
    """.trimIndent()

    suspend fun sendMessage(
        history: List<ChatMessage>,
        newMessage: String,
        modelOption: AiModelOption
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("مفتاح API غير متوفر أو قيد الإعداد. يرجى ضبط مفتاح Gemini في لوحة Secrets.")
            )
        }

        try {
            val url = "$BASE_URL${modelOption.modelId}:generateContent?key=$apiKey"

            val contentsArray = JSONArray()
            // Add previous turns
            history.takeLast(10).forEach { msg ->
                val turnObj = JSONObject()
                turnObj.put("role", if (msg.isUser) "user" else "model")
                val parts = JSONArray().put(JSONObject().put("text", msg.text))
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }
            // Add current user message
            val currentTurn = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", newMessage)))
            }
            contentsArray.put(currentTurn)

            val rootJson = JSONObject().apply {
                put("contents", contentsArray)

                // Generation Config
                val genConfig = JSONObject()
                genConfig.put("temperature", 0.7)
                if (modelOption == AiModelOption.ARGON_THINKING) {
                    val thinkingConfig = JSONObject().put("thinkingLevel", "high")
                    genConfig.put("thinkingConfig", thinkingConfig)
                }
                put("generationConfig", genConfig)

                // System Instruction
                val sysInst = JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                }
                put("systemInstruction", sysInst)
            }

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini API Error (${response.code}): $responseBody"))
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val content = first.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val textBuilder = StringBuilder()
                    for (p in 0 until parts.length()) {
                        val part = parts.getJSONObject(p)
                        if (part.has("text")) {
                            textBuilder.append(part.getString("text"))
                        }
                    }
                    return@withContext Result.success(textBuilder.toString())
                }
            }
            Result.failure(Exception("لم يتم استلام نص في الرد من النموذج."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateCoverImage(
        prompt: String,
        imageSize: String = "1K" // "1K", "2K", "4K"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("مفتاح API غير متوفر أو قيد الإعداد. يرجى ضبط مفتاح Gemini في لوحة Secrets.")
            )
        }

        try {
            // High-quality image generation using gemini-3-pro-image-preview
            val url = "${BASE_URL}gemini-3-pro-image-preview:generateContent?key=$apiKey"

            val rootJson = JSONObject().apply {
                val contents = JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", prompt))
                    )
                )
                put("contents", contents)

                val genConfig = JSONObject().apply {
                    val imgConfig = JSONObject().apply {
                        put("aspectRatio", "1:1")
                        put("imageSize", imageSize)
                    }
                    put("imageConfig", imgConfig)
                    put("responseModalities", JSONArray().put("TEXT").put("IMAGE"))
                }
                put("generationConfig", genConfig)
            }

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Image Generation Error (${response.code}): $responseBody"))
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val content = first.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null) {
                    for (p in 0 until parts.length()) {
                        val part = parts.getJSONObject(p)
                        if (part.has("inlineData")) {
                            val inlineData = part.getJSONObject("inlineData")
                            val base64 = inlineData.optString("data", "")
                            if (base64.isNotEmpty()) {
                                return@withContext Result.success(base64)
                            }
                        }
                    }
                }
            }
            Result.failure(Exception("لم يتم العثور على صورة في استجابة النموذج."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
