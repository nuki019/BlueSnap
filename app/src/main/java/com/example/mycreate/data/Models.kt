package com.example.mycreate.data

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
    val createdAt: Long = System.currentTimeMillis()
)

enum class Screen { HOME, CHAT, PLAN, PREVIEW }

data class AppState(
    val messages: List<ChatMessage> = emptyList(),
    val currentPlan: AppPlan? = null,
    val isGenerating: Boolean = false,
    /** 流式输出中 AI 正在生成的内容（尚未成为完整消息） */
    val streamingContent: String = "",
    val generatedApp: GeneratedApp? = null,
    val savedApps: List<GeneratedApp> = emptyList(),
    val currentScreen: Screen = Screen.HOME
)
