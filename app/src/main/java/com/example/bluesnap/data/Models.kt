package com.example.bluesnap.data

import java.util.UUID

enum class Role { USER, ASSISTANT }

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Feature(
    val name: String,
    val description: String,
    val enabled: Boolean = true
)

data class AppPlan(
    val name: String,
    val description: String,
    val features: List<Feature>,
    val layoutIndex: Int = 0,
    val layouts: List<String> = listOf("简约风格", "卡片风格", "拟物风格")
)

data class GeneratedApp(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val htmlContent: String,
    val summary: String = description,
    val imagePrompt: String? = null,
    val audioPrompt: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class GenerationBundle(
    val html: String,
    val summary: String = "",
    val imagePrompt: String? = null,
    val audioPrompt: String? = null
)

enum class Screen { HOME, HISTORY, SETTINGS, CHAT, PLAN, PREVIEW, SHARE }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class GenerationStage(val label: String) {
    IDLE(""),
    UNDERSTANDING("理解需求"),
    PLANNING("功能清单与风格"),
    BUILDING("生成 HTML"),
    CHECKING("安全检查"),
    LOADING("加载预览")
}

const val DEFAULT_SYSTEM_PROMPT =
    "生成应用整体风格：移动端优先、界面清爽、信息密度适中、适合大学生和准职场青年快速完成校园效率任务。避免花哨装饰，优先保证可读性、可操作性和本地保存。"

data class AppState(
    val messages: List<ChatMessage> = emptyList(),
    val currentPlan: AppPlan? = null,
    val isGenerating: Boolean = false,
    val generationStage: GenerationStage = GenerationStage.IDLE,
    /** 流式输出中 AI 正在生成的内容，尚未成为完整消息。 */
    val streamingContent: String = "",
    val generatedApp: GeneratedApp? = null,
    val savedApps: List<GeneratedApp> = emptyList(),
    val currentScreen: Screen = Screen.HOME,
    val previewReturnScreen: Screen = Screen.HOME,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val activeProviderLabel: String = "蓝心大模型",
    val apiKeyModeLabel: String = "参赛 AppKey 已通过构建配置注入"
)
