package com.example.bluesnap.ai

import android.util.Log
import com.example.bluesnap.data.AppPlan
import com.example.bluesnap.data.ChatMessage
import com.example.bluesnap.data.DEFAULT_SYSTEM_PROMPT
import com.example.bluesnap.data.Feature
import com.example.bluesnap.data.GenerationBundle
import com.example.bluesnap.data.Role
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

class RealAiService(
    private val config: AiConfig
) : AiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val longClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    override suspend fun chat(messages: List<ChatMessage>): ChatMessage = withContext(Dispatchers.IO) {
        val apiMessages = buildChatMessages(messages, CHAT_SYSTEM_PROMPT)
        val content = callApi(apiMessages, temperature = 0.8, maxTokens = 1024, stream = false)
        ChatMessage(role = Role.ASSISTANT, content = content)
    }

    override fun chatStream(messages: List<ChatMessage>): Flow<String> = channelFlow {
        val apiMessages = buildChatMessages(messages, CHAT_SYSTEM_PROMPT)
        val body = JSONObject().apply {
            put("model", config.model)
            put("messages", apiMessages)
            put("stream", true)
            put("temperature", 0.8)
            put("max_tokens", 1024)
        }

        val request = buildRequest(body, stream = true)
        withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string()?.take(200).orEmpty()
                        throw IOException("API 请求失败 (HTTP ${response.code}): $errorBody")
                    }

                    val reader = response.body?.byteStream()?.bufferedReader()
                        ?: throw AiException("API 返回空响应")
                    val buffer = StringBuilder()
                    var chunks = 0
                    reader.useLines { lines ->
                        for (line in lines) {
                            if (!line.startsWith("data:")) continue
                            val data = line.removePrefix("data:").trim()
                            if (data == "[DONE]") break
                            if (data.isEmpty()) continue

                            val content = parseStreamContent(data)
                            if (content.isNotEmpty()) {
                                buffer.append(content)
                                chunks++
                                trySend(buffer.toString())
                            }
                        }
                    }

                    if (buffer.isEmpty()) {
                        throw AiException("API 返回空流")
                    }
                    Log.i(TAG, "stream done provider=${config.provider}, chunks=$chunks, chars=${buffer.length}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "stream failed provider=${config.provider}", e)
                throw e
            }
        }
    }

    override suspend fun generatePlan(
        chatHistory: List<ChatMessage>,
        systemPrompt: String
    ): AppPlan = withContext(Dispatchers.IO) {
        val apiMessages = buildPlanMessages(chatHistory, systemPrompt)
        val rawJson = callApi(apiMessages, temperature = 0.45, maxTokens = 1800, stream = false)
        parsePlan(rawJson)
    }

    override suspend fun generateHtml(
        plan: AppPlan,
        chatHistory: List<ChatMessage>,
        systemPrompt: String
    ): String = withContext(Dispatchers.IO) {
        generateBundle(plan, chatHistory, systemPrompt).html
    }

    override suspend fun generateBundle(
        plan: AppPlan,
        chatHistory: List<ChatMessage>,
        systemPrompt: String
    ): GenerationBundle = withContext(Dispatchers.IO) {
        val enabledFeatures = plan.features.filter { it.enabled }
        val featureDesc = enabledFeatures.joinToString("\n") { "- ${it.name}: ${it.description}" }
        val layout = plan.layouts.getOrElse(plan.layoutIndex) { "简约风格" }

        val prompt = HTML_SYSTEM_PROMPT
            .replace("{app_name}", plan.name)
            .replace("{app_desc}", plan.description)
            .replace("{features}", featureDesc)
            .replace("{layout}", layout)
            .replace("{style_prompt}", systemPrompt.ifBlank { DEFAULT_SYSTEM_PROMPT })

        val apiMessages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", prompt))
            put(JSONObject().put("role", "user").put("content", "请生成这个应用的完整单文件 HTML。只输出指定标签，不要 Markdown。"))
        }
        val raw = callApi(apiMessages, temperature = 0.35, maxTokens = 12000, useLongTimeout = true, stream = false)
        parseGenerationBundle(raw, plan)
    }

    private fun callApi(
        messages: JSONArray,
        temperature: Double = 0.7,
        maxTokens: Int = 2048,
        useLongTimeout: Boolean = false,
        stream: Boolean = false
    ): String {
        val activeClient = if (useLongTimeout) longClient else client
        val body = JSONObject().apply {
            put("model", config.model)
            put("messages", messages)
            put("stream", stream)
            put("temperature", temperature)
            put("max_tokens", maxTokens)
        }
        val started = System.currentTimeMillis()
        val response = activeClient.newCall(buildRequest(body, stream = stream)).execute()
        val responseBody = response.body?.string()
        val elapsed = System.currentTimeMillis() - started
        Log.i(TAG, "call provider=${config.provider}, status=${response.code}, ms=$elapsed, chars=${responseBody?.length ?: 0}")

        if (responseBody == null) {
            throw AiException("API 返回为空 (HTTP ${response.code})")
        }
        if (!response.isSuccessful) {
            throw AiException("API 请求失败 (HTTP ${response.code}): ${responseBody.take(200)}")
        }

        val json = JSONObject(responseBody)
        if (json.has("code") && json.optInt("code", 0) != 0) {
            val msg = json.optString("message", json.optString("msg", "未知错误"))
            throw AiException("API 业务错误: $msg")
        }

        val choices = json.optJSONArray("choices")
            ?: throw AiException("API 响应中没有 choices 字段")
        if (choices.length() == 0) throw AiException("API 返回空 choices")

        val first = choices.getJSONObject(0)
        val finishReason = first.optString("finish_reason", "")
        if (finishReason.equals("length", ignoreCase = true)) {
            Log.w(TAG, "AI 回复可能因长度限制被截断")
        }
        val message = first.optJSONObject("message")
            ?: throw AiException("API choices[0] 中没有 message 字段")
        return message.optString("content", "")
    }

    private fun buildRequest(body: JSONObject, stream: Boolean): Request {
        return Request.Builder()
            .url(buildChatUrl(UUID.randomUUID().toString()))
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .post(body.toString().toRequestBody(jsonType))
            .build()
    }

    private fun buildChatUrl(requestId: String): String {
        val path = "${config.baseUrl.trimEnd('/')}/chat/completions"
        return if (config.provider == "vivo") "$path?request_id=$requestId" else path
    }

    private fun buildChatMessages(messages: List<ChatMessage>, systemPrompt: String): JSONArray {
        return JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", systemPrompt))
            for (msg in messages) {
                val role = if (msg.role == Role.USER) "user" else "assistant"
                put(JSONObject().put("role", role).put("content", msg.content))
            }
        }
    }

    private fun buildPlanMessages(chatHistory: List<ChatMessage>, systemPrompt: String): JSONArray {
        val transcript = chatHistory.joinToString("\n") { msg ->
            val speaker = if (msg.role == Role.USER) "用户" else "助手"
            "$speaker：${msg.content}"
        }
        return JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", PLAN_SYSTEM_PROMPT))
            put(
                JSONObject()
                    .put("role", "user")
                    .put(
                        "content",
                        "产品风格约束：${systemPrompt.ifBlank { DEFAULT_SYSTEM_PROMPT }}\n\n" +
                            "请基于以下对话生成应用方案，只返回 JSON。\n\n$transcript"
                    )
            )
        }
    }

    private fun parseStreamContent(data: String): String {
        return try {
            val json = JSONObject(data)
            val choices = json.optJSONArray("choices") ?: return ""
            if (choices.length() == 0) return ""
            val delta = choices.getJSONObject(0).optJSONObject("delta") ?: return ""
            delta.optString("content", "")
        } catch (e: Exception) {
            Log.w(TAG, "SSE chunk parse failed", e)
            ""
        }
    }

    private fun parsePlan(raw: String): AppPlan {
        val jsonStr = extractJsonBlock(raw)
        return try {
            val json = JSONObject(jsonStr)
            val name = json.optString("name", "自定义工具")
            val description = json.optString("description", "")
            val featuresArray = json.optJSONArray("features") ?: JSONArray()
            val features = (0 until featuresArray.length()).map { i ->
                val f = featuresArray.getJSONObject(i)
                Feature(
                    name = f.optString("name", "功能"),
                    description = f.optString("description", ""),
                    enabled = f.optBoolean("enabled", true)
                )
            }
            AppPlan(
                name = name,
                description = description,
                features = features.takeIf { it.isNotEmpty() } ?: defaultFeatures()
            )
        } catch (e: Exception) {
            AppPlan(
                name = "自定义工具",
                description = raw.take(100),
                features = defaultFeatures()
            )
        }
    }

    private fun defaultFeatures() = listOf(
        Feature("核心功能", "基于你的需求自动生成"),
        Feature("清爽界面", "移动端优先，信息清晰"),
        Feature("本地保存", "使用 localStorage 持久化数据"),
        Feature("一键分享", "导出为单文件 HTML")
    )

    private fun extractJsonBlock(text: String): String {
        val codeBlockRegex = Regex("```(?:json)?\\s*\\n?(.*?)\\n?```", RegexOption.DOT_MATCHES_ALL)
        codeBlockRegex.find(text)?.let { return it.groupValues[1].trim() }
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) text.substring(start, end + 1) else text
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

    private fun extractHtml(text: String): String {
        extractTaggedContent(text, "html_code")?.let { return ensureCompleteHtml(it) }
        extractTaggedContent(text, "html")?.let { tagged ->
            val fixed = if (tagged.trimStart().startsWith("<!DOCTYPE", ignoreCase = true)) tagged else "<html>$tagged</html>"
            return ensureCompleteHtml(fixed)
        }
        val codeBlockRegex = Regex("```(?:html)?\\s*\\n?(<!DOCTYPE[\\s\\S]*?</html>)\\s*```", RegexOption.DOT_MATCHES_ALL)
        codeBlockRegex.find(text)?.let { return ensureCompleteHtml(it.groupValues[1]) }
        val htmlRegex = Regex("<!DOCTYPE[\\s\\S]*?</html>", RegexOption.IGNORE_CASE)
        htmlRegex.find(text)?.let { return ensureCompleteHtml(it.value) }
        throw AiException("AI 未返回完整 HTML，已阻止加载裸文本")
    }

    private fun extractTaggedContent(text: String, tag: String): String? {
        val regex = Regex("<$tag\\b[^>]*>([\\s\\S]*?)</$tag>", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.getOrNull(1)?.trim()
    }

    private fun ensureCompleteHtml(html: String): String {
        val trimmed = html.trim()
        if (!trimmed.contains("<!DOCTYPE", ignoreCase = true) || !trimmed.contains("</html>", ignoreCase = true)) {
            throw AiException("AI 返回的 HTML 片段不完整，已阻止加载")
        }
        return trimmed
    }

    companion object {
        private const val CHAT_SYSTEM_PROMPT = """你是“蓝心快搭”AI 助手，帮助大学生和准职场青年用一句话创建校园效率轻工具。
当需求明确时，用简洁中文说明你已经理解需求，并提示可以查看方案；需求模糊时，只追问 1 个关键问题。不要直接输出代码。"""

        private const val PLAN_SYSTEM_PROMPT = """你是“蓝心快搭”的方案生成器。根据用户需求生成一个移动端轻工具方案。
只返回 JSON，不要 Markdown、解释或代码：
{
  "name": "应用名称",
  "description": "一句话说明用途",
  "features": [
    {"name": "功能名", "description": "具体说明", "enabled": true}
  ]
}
要求：功能 3-5 个，功能名 2-6 个字，优先覆盖用户核心问题，避免功能堆砌。"""

        private const val HTML_SYSTEM_PROMPT = """你是“蓝心快搭”的单文件 HTML 生成引擎。根据以下方案生成一个可在 Android WebView 中运行的移动端轻工具。

应用名称：{app_name}
应用描述：{app_desc}
功能列表：
{features}
界面风格：{layout}
额外风格控制：{style_prompt}

返回时只能使用以下外层标签，不要 Markdown，不要解释：
<summary>一句话说明生成结果</summary>
<html_code>
<!DOCTYPE html>
<html lang="zh-CN">
...
</html>
</html_code>
<image_prompt>可选：适合后续图片生成的中文插图提示词；不需要则留空</image_prompt>
<audio_prompt>可选：适合后续 TTS 播报的中文文本；不需要则留空</audio_prompt>

硬性要求：
1. <html_code> 内只能放一个完整 HTML 文档，必须包含 <!DOCTYPE html>、viewport、</html>。
2. CSS 和 JavaScript 全部内联，不引用外部资源、CDN、字体、图片或 API。
3. 手机端优先，宽度 360-430px 下不横向溢出，按钮触控区域不小于 44px。
4. 使用 localStorage 保存数据；读写 localStorage 用 try/catch 包裹。
5. 所有用户输入写入页面时必须用 textContent 或等价转义，不要把未转义输入拼进 innerHTML。
6. 不使用内联 onclick；所有事件在 DOMContentLoaded 后用 addEventListener 绑定。
7. 不使用 eval 或 new Function，不写入密钥、接口地址或鉴权逻辑。
8. 视觉要清爽、可读、高对比，避免大面积浅色文字和遮挡输入栏。
9. 如果有固定底部输入区，必须给正文留出足够 padding，不能遮挡主要内容。"""
    }
}

class AiException(message: String, cause: Throwable? = null) : Exception(message, cause)
