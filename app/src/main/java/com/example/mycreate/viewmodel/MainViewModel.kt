package com.example.mycreate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycreate.ai.AiService
import com.example.mycreate.ai.RealAiService
import com.example.mycreate.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val aiService: AiService = RealAiService()

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    // ── 公开方法 ──────────────────────────────────────────────────────

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        val userMsg = ChatMessage(role = Role.USER, content = content.trim())
        _state.update { it.copy(messages = it.messages + userMsg, isGenerating = true, streamingContent = "") }

        viewModelScope.launch {
            try {
                // 使用流式输出
                var collectCount = 0
                aiService.chatStream(_state.value.messages).collect { partialContent ->
                    collectCount++
                    Log.d("MainViewModel", "collect #$collectCount, 长度=${partialContent.length}")
                    _state.update { it.copy(streamingContent = partialContent) }
                }

                // 流式完成：将完整内容转为正式消息
                val finalContent = _state.value.streamingContent
                Log.d("MainViewModel", "流式完成, collectCount=$collectCount, finalContent长度=${finalContent.length}")
                if (finalContent.isNotEmpty()) {
                    val aiReply = ChatMessage(role = Role.ASSISTANT, content = finalContent)
                    _state.update {
                        it.copy(
                            messages = it.messages + aiReply,
                            streamingContent = "",
                            isGenerating = false
                        )
                    }
                    Log.d("MainViewModel", "消息已创建并添加")

                    // 根据 AI 回复判断是否自动生成方案
                    if (shouldAutoPlan(finalContent)) {
                        generatePlan()
                    }
                } else {
                    Log.w("MainViewModel", "流式完成但 finalContent 为空")
                    _state.update { it.copy(isGenerating = false, streamingContent = "") }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "AI 请求失败", e)
                val errorDetail = e.message ?: "未知错误"
                val errMsg = ChatMessage(
                    role = Role.ASSISTANT,
                    content = "请求失败: $errorDetail"
                )
                _state.update {
                    it.copy(
                        messages = it.messages + errMsg,
                        isGenerating = false,
                        streamingContent = ""
                    )
                }
            }
        }
    }

    fun generatePlan() {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true) }
            try {
                val plan = aiService.generatePlan(_state.value.messages)
                _state.update { it.copy(currentPlan = plan, isGenerating = false) }
                navigateTo(Screen.PLAN)
            } catch (e: Exception) {
                Log.e("MainViewModel", "生成方案失败", e)
                val errMsg = ChatMessage(
                    role = Role.ASSISTANT,
                    content = "生成方案失败: ${e.message ?: "未知错误"}"
                )
                _state.update { it.copy(messages = it.messages + errMsg, isGenerating = false) }
            }
        }
    }

    fun toggleFeature(index: Int) {
        _state.update { state ->
            val plan = state.currentPlan ?: return@update state
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
        val plan = _state.value.currentPlan ?: return
        _state.update { it.copy(isGenerating = true) }

        viewModelScope.launch {
            try {
                val html = aiService.generateHtml(plan, _state.value.messages)
                val app = GeneratedApp(
                    name = plan.name,
                    description = plan.description,
                    htmlContent = html
                )
                _state.update {
                    it.copy(
                        generatedApp = app,
                        savedApps = it.savedApps + app,
                        isGenerating = false
                    )
                }
                navigateTo(Screen.PREVIEW)
            } catch (e: Exception) {
                Log.e("MainViewModel", "生成 HTML 失败", e)
                _state.update { it.copy(isGenerating = false) }
            }
        }
    }

    fun iterateWithFeedback(feedback: String) {
        sendMessage(feedback)
    }

    fun deleteApp(appId: String) {
        _state.update { it.copy(savedApps = it.savedApps.filter { app -> app.id != appId }) }
    }

    fun openApp(app: GeneratedApp) {
        _state.update { it.copy(generatedApp = app) }
        navigateTo(Screen.PREVIEW)
    }

    fun navigateTo(screen: Screen) {
        _state.update { it.copy(currentScreen = screen) }
    }

    fun clearChat() {
        _state.update { it.copy(messages = emptyList(), currentPlan = null, streamingContent = "") }
    }

    fun startFromTemplate(templateName: String) {
        clearChat()
        navigateTo(Screen.CHAT)
        sendMessage("帮我做一个${templateName}")
    }

    // ── 私有方法 ──────────────────────────────────────────────────────

    /**
     * 根据 AI 回复内容判断是否自动生成方案。
     * 当 AI 表示已理解需求并准备好方案时触发。
     */
    private fun shouldAutoPlan(aiReply: String): Boolean {
        val triggers = listOf("方案", "规划好", "已经理解", "为你设计", "可以生成")
        return triggers.any { aiReply.contains(it) }
    }
}
