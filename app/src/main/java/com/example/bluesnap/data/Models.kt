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
    val layouts: List<String> = listOf("????", "????", "????")
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

enum class Screen { HOME, HISTORY, CHAT, PLAN, PREVIEW, SHARE }

data class AppState(
    val messages: List<ChatMessage> = emptyList(),
    val currentPlan: AppPlan? = null,
    val isGenerating: Boolean = false,
    /** ????? AI ????????????????? */
    val streamingContent: String = "",
    val generatedApp: GeneratedApp? = null,
    val savedApps: List<GeneratedApp> = emptyList(),
    val currentScreen: Screen = Screen.HOME,
    val previewReturnScreen: Screen = Screen.HOME
)
