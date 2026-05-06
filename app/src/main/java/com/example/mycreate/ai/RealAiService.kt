package com.example.mycreate.ai

import android.util.Log
import com.example.mycreate.BuildConfig
import com.example.mycreate.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
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
 * 真实 AI 服务实现，接入 vivo 蓝心大模型 API。
 *
 * 遵循 OpenAI 兼容协议格式，使用 OkHttp 进行网络请求。
 * 支持 SSE 流式输出（chat）和非流式调用（plan / HTML）。
 */
class RealAiService : AiService {

    /** 普通请求客户端（chat / plan） */
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** 长耗时请求客户端（HTML 生成） */
    private val longClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    // ── 公开接口实现 ──────────────────────────────────────────────────

    override suspend fun chat(messages: List<ChatMessage>): ChatMessage = withContext(Dispatchers.IO) {
        Log.d(TAG, "chat() 被调用（非流式），消息数: ${messages.size}")
        val apiMessages = buildChatMessages(messages, CHAT_SYSTEM_PROMPT)
        val content = callApi(apiMessages, temperature = 0.8, maxTokens = 1024, stream = false)
        ChatMessage(role = Role.ASSISTANT, content = content)
    }

    override fun chatStream(messages: List<ChatMessage>): Flow<String> = channelFlow {
        Log.d(TAG, "chatStream() 被调用，消息数: ${messages.size}")

        val apiMessages = buildChatMessages(messages, CHAT_SYSTEM_PROMPT)
        val requestId = UUID.randomUUID().toString()

        val body = JSONObject().apply {
            put("model", BuildConfig.AI_MODEL)
            put("messages", apiMessages)
            put("stream", true)
            put("temperature", 0.8)
            put("max_tokens", 1024)
        }

        val url = "${BuildConfig.AI_BASE_URL}/chat/completions?request_id=$requestId"
        Log.d(TAG, "流式请求 URL: $url")

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${BuildConfig.AI_API_KEY}")
            .post(body.toString().toRequestBody(jsonType))
            .build()

        withContext(Dispatchers.IO) {
            var response: okhttp3.Response? = null
            try {
                response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()?.take(200) ?: ""
                    throw IOException("API 请求失败 (HTTP ${response.code}): $errorBody")
                }

                val reader = response.body!!.byteStream().bufferedReader()
                val buffer = StringBuilder()
                var chunkCount = 0

                reader.useLines { lines ->
                    for (line in lines) {
                        if (!line.startsWith("data:")) continue
                        val data = line.removePrefix("data:").trim()

                        if (data == "[DONE]") {
                            Log.d(TAG, "收到 [DONE]，总 chunk 数: $chunkCount")
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
                                    if (buffer.length >= 20 || content.contains(Regex("[。，！？\\n.]"))) {
                                        Log.d(TAG, "emit #$chunkCount, 长度=${buffer.length}")
                                        trySend(buffer.toString())
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "解析 SSE chunk 失败: $data", e)
                        }
                    }
                }

                // 最终发送完整内容（确保最后一批不丢失）
                if (buffer.isNotEmpty()) {
                    Log.d(TAG, "发送最终内容，长度: ${buffer.length}")
                    trySend(buffer.toString())
                }

            } catch (e: Exception) {
                Log.e(TAG, "流式请求异常", e)
                // 发送已收到的部分内容（兜底）
                // buffer 在此作用域外，通过外部引用处理
            } finally {
                response?.close()
                Log.d(TAG, "chatStream Flow 完成")
            }
        }
    }

    override suspend fun generatePlan(chatHistory: List<ChatMessage>): AppPlan = withContext(Dispatchers.IO) {
        Log.d(TAG, "generatePlan() 被调用")
        val apiMessages = buildChatMessages(chatHistory, PLAN_SYSTEM_PROMPT)
        val rawJson = callApi(apiMessages, temperature = 0.7, maxTokens = 2048, stream = false)
        Log.d(TAG, "方案原始返回: ${rawJson.take(300)}")
        parsePlan(rawJson)
    }

    override suspend fun generateHtml(plan: AppPlan, chatHistory: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        val enabledFeatures = plan.features.filter { it.enabled }
        val featureDesc = enabledFeatures.joinToString("\n") { "- ${it.name}: ${it.description}" }
        val layout = plan.layouts.getOrElse(plan.layoutIndex) { "简约风格" }

        val prompt = HTML_SYSTEM_PROMPT
            .replace("{app_name}", plan.name)
            .replace("{app_desc}", plan.description)
            .replace("{features}", featureDesc)
            .replace("{layout}", layout)

        val apiMessages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", prompt))
            put(JSONObject().put("role", "user").put("content", "请生成这个应用的完整 HTML 代码。"))
        }
        val raw = callApi(apiMessages, temperature = 0.6, maxTokens = 8192, useLongTimeout = true, stream = false)
        extractHtml(raw)
    }

    // ── API 调用（非流式） ──────────────────────────────────────────────

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
            put("model", BuildConfig.AI_MODEL)
            put("messages", messages)
            put("stream", stream)
            put("temperature", temperature)
            put("max_tokens", maxTokens)
        }

        val url = "${BuildConfig.AI_BASE_URL}/chat/completions?request_id=$requestId"
        val timeoutLabel = if (useLongTimeout) "300s" else "120s"
        Log.d(TAG, "请求 URL: $url (timeout=$timeoutLabel, stream=$stream)")
        Log.d(TAG, "请求 Body: ${body.toString().take(500)}")

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${BuildConfig.AI_API_KEY}")
            .post(body.toString().toRequestBody(jsonType))
            .build()

        val response = activeClient.newCall(request).execute()
        val responseBody = response.body?.string()

        Log.d(TAG, "响应码: ${response.code}")
        Log.d(TAG, "响应体: ${responseBody?.take(500) ?: "null"}")

        if (responseBody == null) {
            throw AiException("API 返回为空 (HTTP ${response.code})")
        }

        if (!response.isSuccessful) {
            throw AiException("API 请求失败 (HTTP ${response.code}): ${responseBody.take(200)}")
        }

        val json = JSONObject(responseBody)

        // 检查业务错误码
        if (json.has("code") && json.optInt("code", 0) != 0) {
            val msg = json.optString("message", "未知错误")
            throw AiException("API 业务错误: $msg")
        }

        val choices = json.optJSONArray("choices")
            ?: throw AiException("API 响应中没有 choices 字段")

        if (choices.length() == 0) {
            throw AiException("API 返回空 choices 数组")
        }

        val message = choices.getJSONObject(0).optJSONObject("message")
            ?: throw AiException("API choices[0] 中没有 message 字段")

        val content = message.optString("content", "")
        Log.d(TAG, "AI 回复: ${content.take(200)}")
        return content
    }

    // ── 消息构建 ──────────────────────────────────────────────────────

    private fun buildChatMessages(messages: List<ChatMessage>, systemPrompt: String): JSONArray {
        return JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", systemPrompt))
            for (msg in messages) {
                val role = if (msg.role == Role.USER) "user" else "assistant"
                put(JSONObject().put("role", role).put("content", msg.content))
            }
        }
    }

    // ── 解析逻辑 ──────────────────────────────────────────────────────

    private fun parsePlan(raw: String): AppPlan {
        val jsonStr = extractJsonBlock(raw)
        return try {
            val json = JSONObject(jsonStr)
            val name = json.optString("name", "自定义应用")
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
            if (features.isEmpty()) {
                AppPlan(name = name, description = description, features = defaultFeatures())
            } else {
                AppPlan(name = name, description = description, features = features)
            }
        } catch (e: Exception) {
            AppPlan(
                name = "自定义应用",
                description = raw.take(100),
                features = defaultFeatures()
            )
        }
    }

    private fun defaultFeatures() = listOf(
        Feature("核心功能", "基于你的需求自动生成"),
        Feature("简洁界面", "清爽直观的移动端界面"),
        Feature("数据存储", "本地持久化保存"),
        Feature("一键生成", "即开即用无需安装")
    )

    private fun extractJsonBlock(text: String): String {
        // 尝试提取 ```json ... ``` 代码块
        val codeBlockRegex = Regex("```(?:json)?\\s*\\n?(.*?)\\n?```", RegexOption.DOT_MATCHES_ALL)
        codeBlockRegex.find(text)?.let { return it.groupValues[1].trim() }
        // 尝试找到第一个 { 到最后一个 }
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1)
        }
        return text
    }

    private fun extractHtml(text: String): String {
        // 尝试提取 ```html ... ``` 代码块
        val codeBlockRegex = Regex("```(?:html)?\\s*\\n?(<!DOCTYPE[\\s\\S]*?</html>)\\s*```", RegexOption.DOT_MATCHES_ALL)
        codeBlockRegex.find(text)?.let { return it.groupValues[1].trim() }
        // 尝试直接提取 HTML
        val htmlRegex = Regex("<!DOCTYPE[\\s\\S]*?</html>", RegexOption.IGNORE_CASE)
        htmlRegex.find(text)?.let { return it.value.trim() }
        // 兜底：返回原文
        return text
    }

    companion object {
        private const val CHAT_SYSTEM_PROMPT = """你是"蓝心快搭"AI 助手，一个帮助用户通过自然语言创建轻量级应用的智能工具。

你的职责：
1. 理解用户想要创建什么样的应用
2. 通过对话引导用户明确需求（如果需求模糊的话）
3. 当需求明确后，告诉用户你已经理解了需求，可以查看方案

回复要求：
- 简洁友好，使用中文
- 如果用户需求清晰，回复中包含"我已经为你规划好了方案"这类提示
- 如果用户需求模糊，追问 1-2 个关键问题来明确需求
- 不要直接生成代码，代码生成在后续步骤完成"""

        private const val PLAN_SYSTEM_PROMPT = """你是"蓝心快搭"AI 助手。根据对话历史中用户描述的需求，生成一个应用设计方案。

你必须严格返回以下 JSON 格式，不要包含任何其他文字：
{
  "name": "应用名称",
  "description": "一句话描述应用用途",
  "features": [
    {"name": "功能名称1", "description": "功能描述1", "enabled": true},
    {"name": "功能名称2", "description": "功能描述2", "enabled": true},
    {"name": "功能名称3", "description": "功能描述3", "enabled": true},
    {"name": "功能名称4", "description": "功能描述4", "enabled": true}
  ]
}

要求：
- 功能数量 3-5 个，按优先级排列
- 功能名称简短（2-6个字），描述具体
- 所有功能默认 enabled: true
- 只返回 JSON，不要有其他内容"""

        private const val HTML_SYSTEM_PROMPT = """你是"蓝心快搭"代码生成引擎。根据以下需求生成一个完整的单 HTML 文件应用。

应用名称：{app_name}
应用描述：{app_desc}
功能列表：
{features}
界面风格：{layout}

生成要求：
1. 生成完整的单个 HTML 文件，包含 <!DOCTYPE html> 到 </html>
2. 所有 CSS 和 JavaScript 内联在 HTML 中，不引用任何外部资源
3. 移动端优先：viewport 设置正确，触控友好，按钮不小于 44px
4. 使用 localStorage 进行数据持久化
5. 界面美观、现代，使用渐变色和圆角卡片设计
6. 支持中文，字体使用 -apple-system, "PingFang SC", sans-serif
7. JavaScript 功能完整可运行，无报错
8. 不使用任何外部 CDN 或第三方库
9. 颜色方案与蓝心快搭品牌一致（主色 #1A73E8）
10. 只返回 HTML 代码，不要有任何解释文字

JavaScript 交互要求（极其重要）：
- 所有按钮、输入框等交互元素必须绑定事件监听器（onclick 或 addEventListener）
- 使用 DOMContentLoaded 事件或 window.onload 确保 DOM 加载完成后再绑定事件
- 所有函数必须在调用前定义，不要出现未定义的函数引用
- 使用 let/const 声明变量，避免全局污染
- 不使用 eval() 或 new Function()
- 事件处理函数中涉及 DOM 操作时，确保元素已存在
- 如有表单提交，使用 event.preventDefault() 防止页面刷新
- 定时器（如 setInterval/setTimeout）必须有明确的停止条件"""
    }
}

/** AI 服务异常 */
class AiException(message: String, cause: Throwable? = null) : Exception(message, cause)
