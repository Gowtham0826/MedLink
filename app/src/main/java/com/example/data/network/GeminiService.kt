package com.example.data.network

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val role: String, val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiInstruction(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiInstruction? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiAssistantManager {
    private val systemInstructions = """
        You are MedLink Secure AI Medical Assistant, a built-in safety-compliant assistant.
        YOUR CORE RULE:
        1. Never diagnose any condition under any circumstances.
        2. Never prescribe medications or suggest surgical treatments.
        3. Never offer definitive clinical opinions.
        4. ALWAYS provide general medical information only, and strictly recommend consulting Dr. MedLink or scheduling a consultation with verified clinical staff in the portal.
        5. Remain professional, comforting, objective, and clear.
    """.trimIndent()

    suspend fun chat(history: List<GeminiContent>, message: String): String {
        return try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return "AI Assistant Error: Gemini API Key is missing. Please configure your API key in the AI Studio Secrets Panel to initiate secure clinical consultations."
            }

            // Construct conversation history payload
            val fullContents = history + GeminiContent(role = "user", parts = listOf(GeminiPart(text = message)))
            val request = GeminiRequest(
                contents = fullContents,
                systemInstruction = GeminiInstruction(parts = listOf(GeminiPart(text = systemInstructions)))
            )

            val response = GeminiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I apologize, but I could not compute a clinical context. Please seek professional advice."
        } catch (e: Exception) {
            e.printStackTrace()
            "Error communicating with AI Clinical Proxy: ${e.localizedMessage ?: "Connection error"}. Please check your network or try again."
        }
    }
}
