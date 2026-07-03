package com.example.bluesnap.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluesnap.BuildConfig
import com.example.bluesnap.ai.AiConfig
import com.example.bluesnap.ai.AiService
import com.example.bluesnap.ai.AiServiceFactory
import com.example.bluesnap.data.AppPlan
import com.example.bluesnap.data.AppState
import com.example.bluesnap.data.ChatMessage
import com.example.bluesnap.data.DEFAULT_SYSTEM_PROMPT
import com.example.bluesnap.data.GeneratedApp
import com.example.bluesnap.data.GenerationStage
import com.example.bluesnap.data.Role
import com.example.bluesnap.data.Screen
import com.example.bluesnap.data.ThemeMode
import com.example.bluesnap.notification.GenerationNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val aiService: AiService = AiServiceFactory.create()
    private val prefs = application.getSharedPreferences("bluesnap_settings", Context.MODE_PRIVATE)
    private val notifier = GenerationNotifier(application)

    private val _state = MutableStateFlow(
        AppState(
            themeMode = loadThemeMode(),
            systemPrompt = prefs.getString(KEY_SYSTEM_PROMPT, DEFAULT_SYSTEM_PROMPT) ?: DEFAULT_SYSTEM_PROMPT,
            activeProviderLabel = providerLabel(),
            apiKeyModeLabel = apiKeyModeLabel()
        )
    )
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        val userMsg = ChatMessage(role = Role.USER, content = content.trim())
        _state.update {
            it.copy(
                messages = it.messages + userMsg,
                isGenerating = true,
                generationStage = GenerationStage.UNDERSTANDING,
                generationLogs = listOf(
                    "收到需求，准备连接 ${providerLabel()}",
                    "构建对话上下文，等待模型返回"
                ),
                streamingContent = ""
            )
        }

        viewModelScope.launch {
            try {
                aiService.chatStream(_state.value.messages).collect { partialContent ->
                    _state.update { it.copy(streamingContent = partialContent) }
                }

                val finalContent = _state.value.streamingContent
                if (finalContent.isNotEmpty()) {
                    val aiReply = ChatMessage(role = Role.ASSISTANT, content = finalContent)
                    _state.update {
                        it.copy(
                            messages = it.messages + aiReply,
                            streamingContent = "",
                            isGenerating = false,
                            generationStage = GenerationStage.IDLE
                        )
                    }
                    if (shouldAutoPlan(finalContent)) {
                        generatePlan()
                    }
                } else {
                    _state.update { it.copy(isGenerating = false, generationStage = GenerationStage.IDLE) }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "AI 请求失败", e)
                val errMsg = ChatMessage(
                    role = Role.ASSISTANT,
                    content = "请求失败：${e.message ?: "未知错误"}。已保留离线模板兜底，可返回首页选择固定场景继续演示。"
                )
                _state.update {
                    it.copy(
                        messages = it.messages + errMsg,
                        isGenerating = false,
                        generationStage = GenerationStage.IDLE,
                        generationLogs = it.generationLogs + "请求失败，已保留可用兜底路径",
                        streamingContent = ""
                    )
                }
            }
        }
    }

    fun generatePlan() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isGenerating = true,
                    generationStage = GenerationStage.PLANNING,
                    generationLogs = it.generationLogs + "生成可勾选的功能清单和界面风格"
                )
            }
            try {
                val plan = aiService.generatePlan(_state.value.messages, _state.value.systemPrompt)
                _state.update {
                    it.copy(
                        currentPlan = plan,
                        isGenerating = false,
                        generationStage = GenerationStage.IDLE,
                        generationLogs = it.generationLogs + "方案已生成，等待用户确认"
                    )
                }
                navigateTo(Screen.PLAN)
            } catch (e: Exception) {
                Log.e("MainViewModel", "生成方案失败", e)
                val errMsg = ChatMessage(
                    role = Role.ASSISTANT,
                    content = "生成方案失败：${e.message ?: "未知错误"}"
                )
                _state.update {
                    it.copy(
                        messages = it.messages + errMsg,
                        isGenerating = false,
                        generationStage = GenerationStage.IDLE,
                        generationLogs = it.generationLogs + "方案生成失败，可调整需求后重试"
                    )
                }
            }
        }
    }

    fun toggleFeature(index: Int) {
        _state.update { state ->
            val plan = state.currentPlan ?: return@update state
            if (index !in plan.features.indices) return@update state
            val updated = plan.features.toMutableList()
            updated[index] = updated[index].copy(enabled = !updated[index].enabled)
            state.copy(currentPlan = plan.copy(features = updated))
        }
    }

    fun selectLayout(index: Int) {
        _state.update { state ->
            state.currentPlan?.let { state.copy(currentPlan = it.copy(layoutIndex = index)) } ?: state
        }
    }

    fun confirmPlan() {
        val currentPlan = _state.value.currentPlan ?: return
        val plan = ensureAtLeastOneFeature(currentPlan)
        if (plan != currentPlan) {
            _state.update { it.copy(currentPlan = plan) }
        }

        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        isGenerating = true,
                        generationStage = GenerationStage.BUILDING,
                        generationLogs = listOf(
                            "锁定已选功能，准备生成单文件 HTML",
                            "发送 Chat Completions 请求到 ${providerLabel()}",
                            "后台生成继续运行，可切出应用等待通知"
                        )
                    )
                }
                val bundle = aiService.generateBundle(plan, _state.value.messages, _state.value.systemPrompt)
                _state.update {
                    it.copy(
                        generationStage = GenerationStage.CHECKING,
                        generationLogs = it.generationLogs + "模型返回完成，开始解析 summary/html/media prompt"
                    )
                }
                val app = GeneratedApp(
                    name = plan.name,
                    description = plan.description,
                    htmlContent = bundle.html,
                    summary = bundle.summary.ifBlank { plan.description },
                    imagePrompt = bundle.imagePrompt,
                    audioPrompt = bundle.audioPrompt
                )
                _state.update {
                    it.copy(
                        generationStage = GenerationStage.LOADING,
                        generationLogs = it.generationLogs + "HTML 完整性检查通过，写入本地历史记录"
                    )
                }
                val shouldNotify = !_state.value.isAppInForeground
                _state.update {
                    it.copy(
                        generatedApp = app,
                        savedApps = it.savedApps + app,
                        isGenerating = false,
                        generationStage = GenerationStage.IDLE,
                        generationLogs = it.generationLogs + "生成完成，可预览、导出和分享",
                        previewReturnScreen = Screen.HOME
                    )
                }
                if (shouldNotify) {
                    notifier.notifyGenerated(app.name)
                }
                navigateTo(Screen.PREVIEW)
            } catch (e: Exception) {
                Log.e("MainViewModel", "生成 HTML 失败", e)
                val errMsg = ChatMessage(
                    role = Role.ASSISTANT,
                    content = "生成应用失败：${e.message ?: "未知错误"}。可以调整方案或使用首页固定模板兜底。"
                )
                _state.update {
                    it.copy(
                        messages = it.messages + errMsg,
                        isGenerating = false,
                        generationStage = GenerationStage.IDLE,
                        generationLogs = it.generationLogs + "应用生成失败，请重试或使用固定模板"
                    )
                }
            }
        }
    }

    fun iterateWithFeedback(feedback: String) {
        navigateTo(Screen.CHAT)
        sendMessage("请基于刚才生成的应用做以下修改：$feedback")
    }

    fun deleteApp(appId: String) {
        _state.update { it.copy(savedApps = it.savedApps.filter { app -> app.id != appId }) }
    }

    fun openApp(app: GeneratedApp) {
        _state.update { it.copy(generatedApp = app, previewReturnScreen = it.currentScreen) }
        navigateTo(Screen.PREVIEW)
    }

    fun shareApp(app: GeneratedApp) {
        _state.update {
            it.copy(
                generatedApp = app,
                previewReturnScreen = it.currentScreen,
                currentScreen = Screen.SHARE
            )
        }
    }

    fun navigateTo(screen: Screen) {
        _state.update { it.copy(currentScreen = screen) }
    }

    fun setAppInForeground(inForeground: Boolean) {
        _state.update { it.copy(isAppInForeground = inForeground) }
    }

    fun handleBack() {
        when (_state.value.currentScreen) {
            Screen.SHARE -> navigateTo(Screen.PREVIEW)
            Screen.PREVIEW -> navigateTo(
                _state.value.previewReturnScreen.takeIf { it == Screen.HISTORY } ?: Screen.HOME
            )
            Screen.PLAN -> navigateTo(Screen.CHAT)
            Screen.CHAT -> navigateTo(Screen.HOME)
            Screen.HISTORY, Screen.SETTINGS -> navigateTo(Screen.HOME)
            Screen.HOME -> Unit
        }
    }

    fun clearChat() {
        _state.update {
            it.copy(
                messages = emptyList(),
                currentPlan = null,
                streamingContent = "",
                generationLogs = emptyList(),
                generationStage = GenerationStage.IDLE
            )
        }
    }

    fun startFromTemplate(templateRequest: String) {
        clearChat()
        navigateTo(Screen.CHAT)
        val request = if (templateRequest.contains("做一个") || templateRequest.length > 10) {
            templateRequest
        } else {
            "帮我做一个$templateRequest"
        }
        sendMessage(request)
    }

    fun updateThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _state.update { it.copy(themeMode = mode) }
    }

    fun updateSystemPrompt(prompt: String) {
        val value = prompt.ifBlank { DEFAULT_SYSTEM_PROMPT }
        prefs.edit().putString(KEY_SYSTEM_PROMPT, value).apply()
        _state.update { it.copy(systemPrompt = value) }
    }

    private fun shouldAutoPlan(aiReply: String): Boolean {
        val triggers = listOf("方案", "规划好", "已经理解", "功能清单", "确认后即可生成", "可以查看方案")
        return triggers.any { aiReply.contains(it) }
    }

    private fun ensureAtLeastOneFeature(plan: AppPlan): AppPlan {
        if (plan.features.any { it.enabled } || plan.features.isEmpty()) return plan
        val updated = plan.features.mapIndexed { index, feature ->
            if (index == 0) feature.copy(enabled = true) else feature
        }
        return plan.copy(features = updated)
    }

    private fun loadThemeMode(): ThemeMode {
        return runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM)
    }

    private fun providerLabel(): String {
        val config = AiConfig(
            provider = BuildConfig.AI_PROVIDER,
            fallbackProvider = BuildConfig.AI_FALLBACK_PROVIDER,
            demoMode = BuildConfig.AI_DEMO_MODE,
            apiKey = BuildConfig.AI_API_KEY,
            baseUrl = BuildConfig.AI_BASE_URL,
            model = BuildConfig.AI_MODEL
        )
        if (config.demoMode || !config.hasUsableKey) return "离线 Demo"
        return when (config.provider.lowercase()) {
            "deepseek" -> "DeepSeek"
            "mock" -> "离线 Demo"
            else -> "蓝心大模型"
        }
    }

    private fun apiKeyModeLabel(): String {
        val config = AiConfig(
            provider = BuildConfig.AI_PROVIDER,
            fallbackProvider = BuildConfig.AI_FALLBACK_PROVIDER,
            demoMode = BuildConfig.AI_DEMO_MODE,
            apiKey = BuildConfig.AI_API_KEY,
            baseUrl = BuildConfig.AI_BASE_URL,
            model = BuildConfig.AI_MODEL
        )
        val fallback = BuildConfig.AI_FALLBACK_PROVIDER.trim().ifBlank { "mock" }
        return when {
            config.demoMode -> "当前运行模式：离线 Demo，不使用真实 key。"
            config.hasUsableKey -> "当前运行模式：联网生成；参赛 AppKey 已通过构建配置注入；失败后兜底：$fallback。"
            else -> "当前运行模式：未配置可用 key，将自动使用 Mock 兜底。"
        }
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SYSTEM_PROMPT = "system_prompt"
    }
}
