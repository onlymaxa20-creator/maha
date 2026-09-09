package com.example.data.search

import android.util.Log
import android.util.LruCache
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

/**
 * AI-powered Semantic Search Service powered by Gemini 3.5 Flash.
 * Performs real-time query comprehension:
 * - Intent extraction
 * - Typo and slang translation (Uzbek dialects, Russian, English)
 * - Category guessing & semantic synonym generation
 * - 1-2s response time with fast in-memory LRU caching
 */
class GeminiSearchService(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
) {
    private val TAG = "GeminiSearchService"
    private val cache = LruCache<String, AiSearchEnrichment>(100)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    /**
     * Enriches a user query with LLM semantics.
     * Returns cached result instantly if previously analyzed.
     */
    suspend fun analyzeQuery(rawQuery: String): AiSearchEnrichment = withContext(Dispatchers.IO) {
        val cleanQuery = rawQuery.trim().lowercase()
        if (cleanQuery.isEmpty()) {
            return@withContext AiSearchEnrichment.EMPTY
        }

        // Check in-memory cache first for sub-millisecond response
        cache.get(cleanQuery)?.let {
            return@withContext it
        }

        // Fast local fallback enrichment
        val localNormalized = SearchNormalizer.normalize(cleanQuery)
        val defaultEnrichment = AiSearchEnrichment(
            normalizedQuery = localNormalized.canonicalQuery.ifBlank { cleanQuery },
            didYouMean = localNormalized.didYouMean,
            synonyms = localNormalized.synonyms,
            categoryIntent = null,
            isAiAssisted = false
        )

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            cache.put(cleanQuery, defaultEnrichment)
            return@withContext defaultEnrichment
        }

        try {
            val prompt = """
                Siz O'zbekistondagi Gagarin Go elektron savdo platformasining aqlli qidiruv tahlilchisisiz.
                Foydalanuvchi qidiruv so'zi: "$cleanQuery"
                
                Quyidagi vazifalarni bajaring:
                1. Imlo xatolari, sheva yoki jargon (masalan: krosofka, tel, nout, moyka, svitshot, ayfon) bo'lsa standart o'zbek/rus tilidagi shakliga o'giring.
                2. Agar "shuni nazarda tutdingizmi?" kerak bo'lsa, to'g'ri variantini ko'rsating.
                3. Unga mos 3-5 ta sinonim yoki tegishli kalit so'zlar ro'yxatini bering (o'zbekcha va ruscha).
                4. Mo'ljallangan mahsulot toifasini aniqlang (masalan: Poyabzallar, Kiyimlar, Elektronika, Avto jihozlar, Taomlar, Maishiy texnika).
                
                Javobni FAQAT quyidagi toza JSON formatida bering (boshqa hech qanday izohsiz):
                {
                   "normalizedQuery": "standart qidiruv so'zi",
                   "didYouMean": "agar xato yozilgan bo'lsa to'g'ri varianti, aks holda null",
                   "synonyms": ["sinonim1", "sinonim2", "sinonim3"],
                   "categoryIntent": "mo'ljallangan toifa yoki null"
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBodyStr = response.body?.string().orEmpty()
                val parsedEnrichment = parseGeminiResponse(responseBodyStr, cleanQuery, defaultEnrichment)
                cache.put(cleanQuery, parsedEnrichment)
                return@withContext parsedEnrichment
            } else {
                Log.w(TAG, "Gemini API HTTP code: ${response.code}, falling back to local normalizer")
                cache.put(cleanQuery, defaultEnrichment)
                return@withContext defaultEnrichment
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini search enrichment failed: ${e.message}, using local normalizer")
            cache.put(cleanQuery, defaultEnrichment)
            return@withContext defaultEnrichment
        }
    }

    private fun parseGeminiResponse(
        jsonStr: String,
        originalQuery: String,
        fallback: AiSearchEnrichment
    ): AiSearchEnrichment {
        try {
            val root = JSONObject(jsonStr)
            val candidates = root.optJSONArray("candidates") ?: return fallback
            if (candidates.length() == 0) return fallback

            val firstCand = candidates.getJSONObject(0)
            val content = firstCand.optJSONObject("content") ?: return fallback
            val parts = content.optJSONArray("parts") ?: return fallback
            if (parts.length() == 0) return fallback

            val rawText = parts.getJSONObject(0).optString("text", "").trim()
            if (rawText.isBlank()) return fallback

            val dataObj = JSONObject(rawText)
            val normalizedQuery = dataObj.optString("normalizedQuery", originalQuery)
            val didYouMean = if (dataObj.isNull("didYouMean")) null else dataObj.optString("didYouMean", null)
            val categoryIntent = if (dataObj.isNull("categoryIntent")) null else dataObj.optString("categoryIntent", null)

            val synonymsList = mutableListOf<String>()
            val synArray = dataObj.optJSONArray("synonyms")
            if (synArray != null) {
                for (i in 0 until synArray.length()) {
                    val s = synArray.optString(i).trim()
                    if (s.isNotBlank()) synonymsList.add(s)
                }
            }

            // Merge with local synonyms
            for (localSyn in fallback.synonyms) {
                if (!synonymsList.contains(localSyn)) {
                    synonymsList.add(localSyn)
                }
            }

            return AiSearchEnrichment(
                normalizedQuery = normalizedQuery.ifBlank { originalQuery },
                didYouMean = didYouMean?.takeIf { it.isNotBlank() && !it.equals(originalQuery, ignoreCase = true) },
                synonyms = synonymsList,
                categoryIntent = categoryIntent?.takeIf { it.isNotBlank() },
                isAiAssisted = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error in Gemini response: ${e.message}")
            return fallback
        }
    }
}

data class AiSearchEnrichment(
    val normalizedQuery: String,
    val didYouMean: String?,
    val synonyms: List<String>,
    val categoryIntent: String?,
    val isAiAssisted: Boolean
) {
    companion object {
        val EMPTY = AiSearchEnrichment(
            normalizedQuery = "",
            didYouMean = null,
            synonyms = emptyList(),
            categoryIntent = null,
            isAiAssisted = false
        )
    }
}
