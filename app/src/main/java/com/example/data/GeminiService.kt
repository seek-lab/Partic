package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Double? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiNetwork {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    // Moshi instance to serialize / deserialize PracticeQuestion arrays
    val generalMoshi: Moshi = moshi

    suspend fun generateStudyMaterials(
        country: String,
        classLevel: String,
        subject: String,
        weekNum: Int,
        topic: String,
        totalWeeks: Int
    ): GeneratedMaterialResponse {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API key is missing or not set. Please add GEMINI_API_KEY to the Secrets Panel.")
        }

        val prompt = """
            You are an experienced secondary school teacher in $country. Generate detailed study notes and 8 practice questions (mix of theory and multiple choice) for a $classLevel student studying $subject. The topic is: $topic. Write clearly and in simple English that a secondary school student can understand. Structure notes with headings and bullet points.

            You MUST return a JSON object with EXACTLY the following structure (do NOT wrap with markdown backticks ```json ... ```, just return the raw JSON object string):
            {
              "notes": "Insert the detailed study notes here in markdown representation. It must follow the guidelines above, write clearly in simple English and structure notes with headings and bullet points.",
              "questions": [
                {
                  "questionText": "Question 1 text...",
                  "type": "MCQ",
                  "options": ["Option A", "Option B", "Option C", "Option D"],
                  "correctOptionIndex": 0,
                  "answer": "Option A is correct because..."
                },
                ...
                {
                  "questionText": "Question 8 (e.g. a theory/written question)...",
                  "type": "Theory",
                  "options": [],
                  "correctOptionIndex": -1,
                  "answer": "Detailed sample written answer and explanation..."
                }
              ]
            }

            Requirements:
            1. The 'notes' must be extensive, highly detailed, and customized to the topic ($topic).
            2. The 'questions' must be exactly 8 practice questions, which are a mix of MCQ (multiple choice) and Theory (written/theory).
            3. MCQ questions must have exactly 4 items in 'options', a valid 'correctOptionIndex' (0 to 3), and a clear reasoning 'answer'.
            4. Theory questions must have empty 'options' list, 'correctOptionIndex' as -1, and 'answer' showing the detailed grading response.
            5. Ensure the entire output is valid JSON.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt)
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7
            ),
            systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(text = "You are an experienced secondary school teacher helping students learn. You always return output in valid JSON matching the schema.")
                )
            )
        )

        val response = api.generateContent(apiKey, request)
        val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("No response received from Gemini server")

        val cleanedJsonText = cleanJsonResponse(textResponse)

        val adapter = generalMoshi.adapter(GeneratedMaterialResponse::class.java)
        return adapter.fromJson(cleanedJsonText)
            ?: throw Exception("Failed to parse response JSON from Gemini")
    }

    private fun cleanJsonResponse(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```")) {
            // Remove starting ```json or ```
            clean = clean.replace("^```(?:json)?".toRegex(), "")
        }
        if (clean.endsWith("```")) {
            // Remove ending ```
            clean = clean.replace("```$".toRegex(), "")
        }
        return clean.trim()
    }
}
