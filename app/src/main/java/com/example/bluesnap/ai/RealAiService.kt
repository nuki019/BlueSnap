package com.example.bluesnap.ai

import android.util.Log
import com.example.bluesnap.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val TAG = "RealAiService"

/**
 * ?? AI ??????? vivo ????? API?
 *
 * ?? OpenAI ????????? OkHttp ???????
 * ?? SSE ?????chat????????plan / HTML??
 */
class RealAiService(
    private val config: AiConfig
) : AiService {

    /** ????????chat / plan? */
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** ?????????HTML ??? */
    private val longClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    // ?? ?????? ??????????????????????????????????????????????????

    override suspend fun chat(messages: List<ChatMessage>): ChatMessage = withContext(Dispatchers.IO) {
        Log.d(TAG, "chat() ????????????: ${messages.size}")
        val apiMessages = buildChatMessages(messages, CHAT_SYSTEM_PROMPT)
        val content = callApi(apiMessages, temperature = 0.8, maxTokens = 1024, stream = false)
        ChatMessage(role = Role.ASSISTANT, content = content)
    }

    override fun chatStream(messages: List<ChatMessage>): Flow<String> = channelFlow {
        Log.d(TAG, "chatStream() ???????: ${messages.size}")

        val apiMessages = buildChatMessages(messages, CHAT_SYSTEM_PROMPT)
        val requestId = UUID.randomUUID().toString()

        val body = JSONObject().apply {
            put("model", config.model)
            put("messages", apiMessages)
            put("stream", true)
            put("temperature", 0.8)
            put("max_tokens", 1024)
        }

        val url = buildChatUrl(requestId)
        Log.d(TAG, "???? URL: $url")

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .post(body.toString().toRequestBody(jsonType))
            .build()

        withContext(Dispatchers.IO) {
            var response: okhttp3.Response? = null
            try {
                response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()?.take(200) ?: ""
                    throw IOException("API ???? (HTTP ${response.code}): $errorBody")
                }

                val reader = response.body!!.byteStream().bufferedReader()
                val buffer = StringBuilder()
                var chunkCount = 0

                reader.useLines { lines ->
                    for (line in lines) {
                        if (!line.startsWith("data:")) continue
                        val data = line.removePrefix("data:").trim()

                        if (data == "[DONE]") {
                            Log.d(TAG, "?? [DONE]?? chunk ?: $chunkCount")
                            break
                        }

                        if (data.isEmpty()) continue

                        try {
                            val json = JSONObject(data)
                            val choices = json.optJSONArray("choices")
                            if (choices != null && choices.length() > 0) {
                                val delta = choices.getJSONObject(0).optJSONObject("delta")
                                val content = delta?.optString("content", "") ?: ""
                                if (content.isNotEmpty()) {
                                    buffer.append(content)
                                    chunkCount++
                                    if (buffer.length >= 20 || content.contains(Regex("[????\\n.]"))) {
                                        Log.d(TAG, "emit #$chunkCount, ??=${buffer.length}")
                                        trySend(buffer.toString())
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "?? SSE chunk ??: $data", e)
                        }
                    }
                }

                if (buffer.isEmpty()) {
                    throw AiException("API ????")
                }

                // ???????????????????
                Log.d(TAG, "?????????: ${buffer.length}")
                trySend(buffer.toString())

            } catch (e: Exception) {
                Log.e(TAG, "??????", e)
                throw e
            } finally {
                response?.close()
                Log.d(TAG, "chatStream Flow ??")
            }
        }
    }

    override suspend fun generatePlan(chatHistory: List<ChatMessage>): AppPlan = withContext(Dispatchers.IO) {
        Log.d(TAG, "generatePlan() ???")
        val apiMessages = buildPlanMessages(chatHistory)
        val rawJson = callApi(apiMessages, temperature = 0.7, maxTokens = 2048, stream = false)
        Log.d(TAG, "??????: ${rawJson.take(300)}")
        parsePlan(rawJson)
    }

    override suspend fun generateHtml(plan: AppPlan, chatHistory: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        generateBundle(plan, chatHistory).html
    }

    override suspend fun generateBundle(plan: AppPlan, chatHistory: List<ChatMessage>): GenerationBundle = withContext(Dispatchers.IO) {
        val enabledFeatures = plan.features.filter { it.enabled }
        val featureDesc = enabledFeatures.joinToString("\n") { "- ${it.name}: ${it.description}" }
        val layout = plan.layouts.getOrElse(plan.layoutIndex) { "????" }

        val prompt = HTML_SYSTEM_PROMPT
            .replace("{app_name}", plan.name)
            .replace("{app_desc}", plan.description)
            .replace("{features}", featureDesc)
            .replace("{layout}", layout)

        val apiMessages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", prompt))
            put(JSONObject().put("role", "user").put("content", "?????????? HTML ???"))
        }
        val raw = callApi(apiMessages, temperature = 0.6, maxTokens = 8192, useLongTimeout = true, stream = false)
        parseGenerationBundle(raw, plan)
    }

    // ?? API ??????? ??????????????????????????????????????????????

    private fun callApi(
        messages: JSONArray,
        temperature: Double = 0.7,
        maxTokens: Int = 2048,
        useLongTimeout: Boolean = false,
        stream: Boolean = false
    ): String {
        val activeClient = if (useLongTimeout) longClient else client
        val requestId = UUID.randomUUID().toString()

        val body = JSONObject().apply {
            put("model", config.model)
            put("messages", messages)
            put("stream", stream)
            put("temperature", temperature)
            put("max_tokens", maxTokens)
        }

        val url = buildChatUrl(requestId)
        val timeoutLabel = if (useLongTimeout) "300s" else "120s"
        Log.d(TAG, "?? URL: $url (timeout=$timeoutLabel, stream=$stream)")
        Log.d(TAG, "?? Body: ${body.toString().take(500)}")

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .post(body.toString().toRequestBody(jsonType))
            .build()

        val response = activeClient.newCall(request).execute()
        val responseBody = response.body?.string()

        Log.d(TAG, "???: ${response.code}")
        Log.d(TAG, "???: ${responseBody?.take(500) ?: "null"}")

        if (responseBody == null) {
            throw AiException("API ???? (HTTP ${response.code})")
        }

        if (!response.isSuccessful) {
            throw AiException("API ???? (HTTP ${response.code}): ${responseBody.take(200)}")
        }

        val json = JSONObject(responseBody)

        // ???????
        if (json.has("code") && json.optInt("code", 0) != 0) {
            val msg = json.optString("message", "????")
            throw AiException("API ????: $msg")
        }

        val choices = json.optJSONArray("choices")
            ?: throw AiException("API ????? choices ??")

        if (choices.length() == 0) {
            throw AiException("API ??? choices ??")
        }

        val message = choices.getJSONObject(0).optJSONObject("message")
            ?: throw AiException("API choices[0] ??? message ??")

        val content = message.optString("content", "")
        val finishReason = choices.getJSONObject(0).optString("finish_reason", "")
        if (finishReason.equals("length", ignoreCase = true)) {
            Log.w(TAG, "AI ????????????")
        }
        Log.d(TAG, "AI ??: ${content.take(200)}")
        return content
    }

    private fun buildChatUrl(requestId: String): String {
        val path = "${config.baseUrl.trimEnd('/')}/chat/completions"
        return if (config.provider == "vivo") {
            "$path?request_id=$requestId"
        } else {
            path
        }
    }

    // ?? ???? ??????????????????????????????????????????????????????

    private fun buildChatMessages(messages: List<ChatMessage>, systemPrompt: String): JSONArray {
        return JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", systemPrompt))
            for (msg in messages) {
                val role = if (msg.role == Role.USER) "user" else "assistant"
                put(JSONObject().put("role", role).put("content", msg.content))
            }
        }
    }

    private fun buildPlanMessages(chatHistory: List<ChatMessage>): JSONArray {
        val transcript = chatHistory.joinToString("\n") { msg ->
            val speaker = if (msg.role == Role.USER) "??" else "??"
            "$speaker?${msg.content}"
        }
        return JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", PLAN_SYSTEM_PROMPT))
            put(
                JSONObject()
                    .put("role", "user")
                    .put("content", "????????????????? JSON?\n\n$transcript")
            )
        }
    }

    // ?? ???? ??????????????????????????????????????????????????????

    private fun parsePlan(raw: String): AppPlan {
        val jsonStr = extractJsonBlock(raw)
        return try {
            val json = JSONObject(jsonStr)
            val name = json.optString("name", "?????")
            val description = json.optString("description", "")
            val featuresArray = json.optJSONArray("features") ?: JSONArray()
            val features = (0 until featuresArray.length()).map { i ->
                val f = featuresArray.getJSONObject(i)
                Feature(
                    name = f.optString("name", "??"),
                    description = f.optString("description", ""),
                    enabled = f.optBoolean("enabled", true)
                )
            }
            if (features.isEmpty()) {
                AppPlan(name = name, description = description, features = defaultFeatures())
            } else {
                AppPlan(name = name, description = description, features = features)
            }
        } catch (e: Exception) {
            AppPlan(
                name = "?????",
                description = raw.take(100),
                features = defaultFeatures()
            )
        }
    }

    private fun defaultFeatures() = listOf(
        Feature("????", "??????????"),
        Feature("????", "??????????"),
        Feature("????", "???????"),
        Feature("????", "????????")
    )

    private fun extractJsonBlock(text: String): String {
        // ???? ```json ... ``` ???
        val codeBlockRegex = Regex("```(?:json)?\\s*\\n?(.*?)\\n?```", RegexOption.DOT_MATCHES_ALL)
        codeBlockRegex.find(text)?.let { return it.groupValues[1].trim() }
        // ??????? { ????? }
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1)
        }
        return text
    }

    private fun extractHtml(text: String): String {
        extractTaggedContent(text, "html")?.let { html ->
            return ensureCompleteHtml(html)
        }
        // ???? ```html ... ``` ???
        val codeBlockRegex = Regex("```(?:html)?\\s*\\n?(<!DOCTYPE[\\s\\S]*?</html>)\\s*```", RegexOption.DOT_MATCHES_ALL)
        codeBlockRegex.find(text)?.let { return it.groupValues[1].trim() }
        // ?????? HTML
        val htmlRegex = Regex("<!DOCTYPE[\\s\\S]*?</html>", RegexOption.IGNORE_CASE)
        htmlRegex.find(text)?.let { return it.value.trim() }
        throw AiException("AI ????? HTML?????????")
    }

    private fun parseGenerationBundle(text: String, plan: AppPlan): GenerationBundle {
        val html = extractHtml(text)
        return GenerationBundle(
            html = html,
            summary = extractTaggedContent(text, "summary")?.takeIf { it.isNotBlank() } ?: plan.description,
            imagePrompt = extractTaggedContent(text, "image_prompt")?.takeIf { it.isNotBlank() },
            audioPrompt = extractTaggedContent(text, "audio_prompt")?.takeIf { it.isNotBlank() }
        )
    }

    private fun extractTaggedContent(text: String, tag: String): String? {
        val regex = Regex("<$tag\\b[^>]*>([\\s\\S]*?)</$tag>", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.getOrNull(1)?.trim()
    }

    private fun ensureCompleteHtml(html: String): String {
        val trimmed = html.trim()
        if (!trimmed.contains("<!DOCTYPE", ignoreCase = true) || !trimmed.contains("</html>", ignoreCase = true)) {
            throw AiException("AI ??? <html> ???????????")
        }
        return trimmed
    }

    companion object {
        private const val CHAT_SYSTEM_PROMPT = """??"????"AI ????????????????????????????

?????
1. ??????????????
2. ??????????????????????
3. ??????????????????????????

?????
- ?????????
- ??????????????"???????????"????
- ??????????? 1-2 ??????????
- ????????????????????"""

        private const val PLAN_SYSTEM_PROMPT = """??"????"AI ?????????????????????????????

????????? JSON ??????????????
{
  "name": "????",
  "description": "?????????",
  "features": [
    {"name": "????1", "description": "????1", "enabled": true},
    {"name": "????2", "description": "????2", "enabled": true},
    {"name": "????3", "description": "????3", "enabled": true},
    {"name": "????4", "description": "????4", "enabled": true}
  ]
}

???
- ???? 3-5 ????????
- ???????2-6????????
- ?????? enabled: true
- ??? JSON????????"""

        private const val HTML_SYSTEM_PROMPT = """??"????"????????????????????? HTML ?????

?????{app_name}
?????{app_desc}
?????
{features}
?????{layout}

?????
<summary>?????????</summary>
<html>
<!DOCTYPE html>
...?? HTML...
</html>
<image_prompt>????????????????????????????????????</image_prompt>
<audio_prompt>?????????????????????????????????</audio_prompt>

?????
1. <html> ??????????? HTML ????? <!DOCTYPE html> ? </html>
2. ?? CSS ? JavaScript ??? HTML ???????????
3. ??????viewport ??????????????? 44px
4. ?? localStorage ???????
5. ????????????????????
6. ????????? -apple-system, "PingFang SC", sans-serif
7. JavaScript ???????????
8. ??????? CDN ?????
9. ???????????????? #1A73E8?
10. ??? HTML ?????? API???? HTML ???????????????

JavaScript ???????????
- ???????????????????????onclick ? addEventListener?
- ?? DOMContentLoaded ??? window.onload ?? DOM ??????????
- ?????????????????????????
- ?? let/const ???????????
- ??? eval() ? new Function()
- ????????? DOM ???????????
- ????????? event.preventDefault() ??????
- ????? setInterval/setTimeout???????????"""
    }
}

/** AI ???? */
class AiException(message: String, cause: Throwable? = null) : Exception(message, cause)
