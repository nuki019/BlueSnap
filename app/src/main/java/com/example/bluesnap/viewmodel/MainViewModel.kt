package com.example.bluesnap.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluesnap.ai.AiService
import com.example.bluesnap.ai.AiServiceFactory
import com.example.bluesnap.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val aiService: AiService = AiServiceFactory.create()

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    // ?? ???? ??????????????????????????????????????????????????????

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        val userMsg = ChatMessage(role = Role.USER, content = content.trim())
        _state.update { it.copy(messages = it.messages + userMsg, isGenerating = true, streamingContent = "") }

        viewModelScope.launch {
            try {
                // ??????
                var collectCount = 0
                aiService.chatStream(_state.value.messages).collect { partialContent ->
                    collectCount++
                    Log.d("MainViewModel", "collect #$collectCount, ??=${partialContent.length}")
                    _state.update { it.copy(streamingContent = partialContent) }
                }

                // ????????????????
                val finalContent = _state.value.streamingContent
                Log.d("MainViewModel", "????, collectCount=$collectCount, finalContent??=${finalContent.length}")
                if (finalContent.isNotEmpty()) {
                    val aiReply = ChatMessage(role = Role.ASSISTANT, content = finalContent)
                    _state.update {
                        it.copy(
                            messages = it.messages + aiReply,
                            streamingContent = "",
                            isGenerating = false
                        )
                    }
                    Log.d("MainViewModel", "????????")

                    // ?? AI ????????????
                    if (shouldAutoPlan(finalContent)) {
                        generatePlan()
                    }
                } else {
                    Log.w("MainViewModel", "????? finalContent ??")
                    _state.update { it.copy(isGenerating = false, streamingContent = "") }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "AI ????", e)
                val errorDetail = e.message ?: "????"
                val errMsg = ChatMessage(
                    role = Role.ASSISTANT,
                    content = "????: $errorDetail"
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
                Log.e("MainViewModel", "??????", e)
                val errMsg = ChatMessage(
                    role = Role.ASSISTANT,
                    content = "??????: ${e.message ?: "????"}"
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
        val currentPlan = _state.value.currentPlan ?: return
        val plan = ensureAtLeastOneFeature(currentPlan)
        if (plan != currentPlan) {
            _state.update { it.copy(currentPlan = plan) }
        }
        _state.update { it.copy(isGenerating = true) }

        viewModelScope.launch {
            try {
                val bundle = aiService.generateBundle(plan, _state.value.messages)
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
                        generatedApp = app,
                        savedApps = it.savedApps + app,
                        isGenerating = false
                    )
                }
                navigateTo(Screen.PREVIEW)
            } catch (e: Exception) {
                Log.e("MainViewModel", "?? HTML ??", e)
                _state.update { it.copy(isGenerating = false) }
            }
        }
    }

    fun iterateWithFeedback(feedback: String) {
        navigateTo(Screen.CHAT)
        sendMessage(feedback)
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

    fun openGeneratedPreview() {
        _state.update { it.copy(previewReturnScreen = it.currentScreen, currentScreen = Screen.PREVIEW) }
    }

    fun navigateTo(screen: Screen) {
        _state.update { it.copy(currentScreen = screen) }
    }

    fun handleBack() {
        val current = _state.value.currentScreen
        when (current) {
            Screen.SHARE -> navigateTo(Screen.PREVIEW)
            Screen.PREVIEW -> navigateTo(_state.value.previewReturnScreen.takeIf { it == Screen.HISTORY } ?: Screen.HOME)
            Screen.PLAN -> navigateTo(Screen.CHAT)
            Screen.CHAT -> navigateTo(Screen.HOME)
            Screen.HISTORY -> navigateTo(Screen.HOME)
            Screen.HOME -> Unit
        }
    }

    fun clearChat() {
        _state.update { it.copy(messages = emptyList(), currentPlan = null, streamingContent = "") }
    }

    fun startFromTemplate(templateRequest: String) {
        clearChat()
        navigateTo(Screen.CHAT)
        val request = if (templateRequest.contains("???") || templateRequest.length > 10) {
            templateRequest
        } else {
            "?????$templateRequest"
        }
        sendMessage(request)
    }

    // ?? ???? ??????????????????????????????????????????????????????

    /**
     * ?? AI ???????????????
     * ? AI ?????????????????
     */
    private fun shouldAutoPlan(aiReply: String): Boolean {
        val triggers = listOf("??", "???", "????", "????", "????")
        return triggers.any { aiReply.contains(it) }
    }

    private fun ensureAtLeastOneFeature(plan: AppPlan): AppPlan {
        if (plan.features.any { it.enabled } || plan.features.isEmpty()) return plan
        val updated = plan.features.mapIndexed { index, feature ->
            if (index == 0) feature.copy(enabled = true) else feature
        }
        return plan.copy(features = updated)
    }
}
