package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.EnvironmentType
import com.example.model.SceneCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class SceneAnalysisResult(
    val category: SceneCategory,
    val sceneTitle: String,
    val detectedElements: String,
    val poseAdvice: String,
    val isAiGenerated: Boolean = true
) {
    val environment: EnvironmentType get() = category.environment
}

class AiSceneAnalyzer {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private var fallbackIndex = 0

    suspend fun analyzeBackground(bitmap: Bitmap?, preferredCategory: SceneCategory? = null): SceneAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY" || bitmap == null) {
            // Smart offline heuristic fallback
            return@withContext getSmartFallbackResult(bitmap, preferredCategory)
        }

        try {
            val scaledBitmap = scaleBitmapDown(bitmap, 640)
            val base64Image = bitmapToBase64(scaledBitmap)

            val prompt = """
                Analyze the background environment of this camera viewfinder frame.
                First determine if it is INDOOR or OUTDOOR, then select the best matching category from:
                OUTDOOR CATEGORIES:
                - GRADUATION (academic campus, gowns, caps, diplomas, campus hall)
                - GARDEN_BENCH (flower gardens, park bench, lush greenery, blossom backdrop)
                - URBAN_STREET (city sidewalk, modern architecture, brick walls, crosswalks)
                - SCENIC_NATURE (mountains, trees, panoramic vista, hiking view)
                - BEACH_SUNSET (ocean, sandy beach, coastal boardwalk, sunset sky)

                INDOOR CATEGORIES:
                - CAFE_INDOOR (coffee shop table, mugs, cafe lighting, warm interior)
                - COZY_SOFA (living room couch, lounge sofa, floor cushions, cozy home)
                - MIRROR_SELFIE (full-length mirror, bedroom mirror, outfit check setting)
                - BOOKSHELF_WINDOW (library shelves, study nook, sunny window ledge)
                - STUDIO_PORTRAIT (minimalist clean backdrop, solid wall, studio setting)

                Respond STRICTLY with valid JSON matching:
                {
                  "detected_category": "CAFE_INDOOR",
                  "scene_title": "Cozy Espresso Cafe",
                  "detected_elements": "Wooden table, warm amber lighting, espresso cup",
                  "pose_advice": "Rest forearms on the table holding your warm mug for a cozy lifestyle shot."
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray()
                    partsArray.put(JSONObject().put("text", prompt))
                    val inlineDataObj = JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", base64Image)
                    }
                    partsArray.put(JSONObject().put("inlineData", inlineDataObj))
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                val genConfig = JSONObject().apply {
                    val respFormat = JSONObject().apply {
                        put("mimeType", "application/json")
                    }
                    put("responseFormat", respFormat)
                    put("temperature", 0.3)
                }
                put("generationConfig", genConfig)
            }

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("AiSceneAnalyzer", "API call failed with code: ${response.code}")
                return@withContext getSmartFallbackResult(bitmap, preferredCategory)
            }

            val responseString = response.body?.string() ?: ""
            val rootJson = JSONObject(responseString)
            val candidates = rootJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            parseAiJsonResponse(text, bitmap, preferredCategory)
        } catch (e: Exception) {
            Log.e("AiSceneAnalyzer", "Error analyzing background", e)
            getSmartFallbackResult(bitmap, preferredCategory)
        }
    }

    private fun parseAiJsonResponse(text: String, bitmap: Bitmap?, preferredCategory: SceneCategory?): SceneAnalysisResult {
        return try {
            val cleanJson = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val json = JSONObject(cleanJson)
            val categoryStr = json.optString("detected_category", "GRADUATION").uppercase()
            val category = try {
                SceneCategory.valueOf(categoryStr)
            } catch (e: Exception) {
                SceneCategory.GRADUATION
            }

            SceneAnalysisResult(
                category = category,
                sceneTitle = json.optString("scene_title", category.displayName),
                detectedElements = json.optString("detected_elements", "Identified scenery elements"),
                poseAdvice = json.optString("pose_advice", "Align with the suggested silhouette outline to frame this shot."),
                isAiGenerated = true
            )
        } catch (e: Exception) {
            getSmartFallbackResult(bitmap, preferredCategory)
        }
    }

    private fun getSmartFallbackResult(bitmap: Bitmap?, preferredCategory: SceneCategory?): SceneAnalysisResult {
        if (preferredCategory != null) {
            return when (preferredCategory) {
                SceneCategory.CAFE_INDOOR -> SceneAnalysisResult(
                    category = SceneCategory.CAFE_INDOOR,
                    sceneTitle = "Cozy Cafe Interior",
                    detectedElements = "Cafe table, warm amber ambient lighting",
                    poseAdvice = "Rest forearms on the table with a warm mug for an intimate candid shot.",
                    isAiGenerated = false
                )
                SceneCategory.COZY_SOFA -> SceneAnalysisResult(
                    category = SceneCategory.COZY_SOFA,
                    sceneTitle = "Living Room & Couch",
                    detectedElements = "Comfortable sofa, home cushions & soft rug",
                    poseAdvice = "Curl up sideways hugging a cushion with knees tucked comfortably.",
                    isAiGenerated = false
                )
                SceneCategory.MIRROR_SELFIE -> SceneAnalysisResult(
                    category = SceneCategory.MIRROR_SELFIE,
                    sceneTitle = "Mirror & Outfit Check",
                    detectedElements = "Full-length mirror surface and ambient reflection",
                    poseAdvice = "Hold phone at chest height, pop one hip to side for an aesthetic silhouette.",
                    isAiGenerated = false
                )
                SceneCategory.BOOKSHELF_WINDOW -> SceneAnalysisResult(
                    category = SceneCategory.BOOKSHELF_WINDOW,
                    sceneTitle = "Bookshelf & Sunlit Nook",
                    detectedElements = "Bookshelves & warm natural window daylight",
                    poseAdvice = "Reach to slide a book spine or read in the window light with peaceful poise.",
                    isAiGenerated = false
                )
                SceneCategory.STUDIO_PORTRAIT -> SceneAnalysisResult(
                    category = SceneCategory.STUDIO_PORTRAIT,
                    sceneTitle = "Studio Minimal Wall",
                    detectedElements = "Clean solid wall backdrop & even lighting",
                    poseAdvice = "Hook thumbs into pockets and drop one shoulder for effortless confidence.",
                    isAiGenerated = false
                )
                SceneCategory.GRADUATION -> SceneAnalysisResult(
                    category = SceneCategory.GRADUATION,
                    sceneTitle = "Campus Graduation Celebration",
                    detectedElements = "Academic architecture & ceremony atmosphere",
                    poseAdvice = "Raise diploma scroll high and kick out with celebratory energy!",
                    isAiGenerated = false
                )
                SceneCategory.GARDEN_BENCH -> SceneAnalysisResult(
                    category = SceneCategory.GARDEN_BENCH,
                    sceneTitle = "Garden Park Bench",
                    detectedElements = "Park bench, floral greenery & gentle foliage",
                    poseAdvice = "Drape one arm along the bench backrest and cross legs with relaxed posture.",
                    isAiGenerated = false
                )
                SceneCategory.URBAN_STREET -> SceneAnalysisResult(
                    category = SceneCategory.URBAN_STREET,
                    sceneTitle = "Urban Street Architecture",
                    detectedElements = "Textured building wall, sidewalk pavement",
                    poseAdvice = "Stride forward across crosswalk or lean back casually against the wall.",
                    isAiGenerated = false
                )
                SceneCategory.SCENIC_NATURE -> SceneAnalysisResult(
                    category = SceneCategory.SCENIC_NATURE,
                    sceneTitle = "Scenic Nature Vista",
                    detectedElements = "Open landscape, tree canopy & natural horizon",
                    poseAdvice = "Open both arms wide embracing the wide horizon scenery.",
                    isAiGenerated = false
                )
                SceneCategory.BEACH_SUNSET -> SceneAnalysisResult(
                    category = SceneCategory.BEACH_SUNSET,
                    sceneTitle = "Golden Hour Shoreline",
                    detectedElements = "Warm sunset horizon, coastal breeze",
                    poseAdvice = "Turn profile towards the sunset glow for radiant golden rim lighting.",
                    isAiGenerated = false
                )
            }
        }

        // Cycle through presets for varied suggestions
        val presets = listOf(
            SceneAnalysisResult(
                category = SceneCategory.GRADUATION,
                sceneTitle = "Campus Graduation Celebration",
                detectedElements = "Academic campus & festive graduation setting",
                poseAdvice = "Raise diploma scroll high and kick with joyful celebration energy!",
                isAiGenerated = false
            ),
            SceneAnalysisResult(
                category = SceneCategory.CAFE_INDOOR,
                sceneTitle = "Indoor Cafe & Coffee Table",
                detectedElements = "Wooden tabletop, warm lighting & cafe atmosphere",
                poseAdvice = "Rest forearms on the table holding your warm mug for an intimate portrait.",
                isAiGenerated = false
            ),
            SceneAnalysisResult(
                category = SceneCategory.GARDEN_BENCH,
                sceneTitle = "Garden Park Bench",
                detectedElements = "Park wooden bench & lush floral backdrop",
                poseAdvice = "Drape arm over the bench backrest and cross legs in relaxed elegance.",
                isAiGenerated = false
            ),
            SceneAnalysisResult(
                category = SceneCategory.COZY_SOFA,
                sceneTitle = "Indoor Living Room & Sofa",
                detectedElements = "Comfortable couch, soft pillows & warm room lights",
                poseAdvice = "Curl up on the sofa hugging a cushion with knees tucked comfortably.",
                isAiGenerated = false
            ),
            SceneAnalysisResult(
                category = SceneCategory.URBAN_STREET,
                sceneTitle = "Urban City Street",
                detectedElements = "Textured brick wall, city sidewalk & architecture",
                poseAdvice = "Lean your shoulder against the wall with one hand in pocket.",
                isAiGenerated = false
            ),
            SceneAnalysisResult(
                category = SceneCategory.MIRROR_SELFIE,
                sceneTitle = "Indoor Mirror & Outfit Check",
                detectedElements = "Full-length mirror frame & indoor room lighting",
                poseAdvice = "Hold phone at chest height, pop one hip to side for an aesthetic silhouette.",
                isAiGenerated = false
            )
        )

        val result = presets[fallbackIndex % presets.size]
        fallbackIndex++
        return result
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDim && height <= maxDim) return bitmap
        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (ratio > 1) {
            newWidth = maxDim
            newHeight = (maxDim / ratio).toInt()
        } else {
            newHeight = maxDim
            newWidth = (maxDim * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
