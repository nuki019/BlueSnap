package com.example.bluesnap.ai

import android.util.Log
import com.example.bluesnap.data.AppPlan
import com.example.bluesnap.data.ChatMessage
import com.example.bluesnap.data.DEFAULT_SYSTEM_PROMPT
import com.example.bluesnap.data.GenerationBundle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

private const val FALLBACK_TAG = "FallbackAiService"

class FallbackAiService(
    private val primary: AiService,
    private val fallback: AiService
) : AiService {
    override suspend fun chat(messages: List<ChatMessage>) =
        runCatching { primary.chat(messages) }
            .getOrElse {
                Log.w(FALLBACK_TAG, "对话服务失败，切换到兜底服务", it)
                fallback.chat(messages)
            }

    override fun chatStream(messages: List<ChatMessage>): Flow<String> =
        primary.chatStream(messages).catch {
            Log.w(FALLBACK_TAG, "流式服务失败，切换到兜底服务", it)
            fallback.chatStream(messages).collect { content -> emit(content) }
        }

    override suspend fun generatePlan(
        chatHistory: List<ChatMessage>,
        systemPrompt: String
    ): AppPlan =
        runCatching { primary.generatePlan(chatHistory, systemPrompt) }
            .getOrElse {
                Log.w(FALLBACK_TAG, "方案生成失败，切换到兜底服务", it)
                fallback.generatePlan(chatHistory, systemPrompt)
            }

    override suspend fun generateHtml(
        plan: AppPlan,
        chatHistory: List<ChatMessage>,
        systemPrompt: String
    ): String =
        runCatching { primary.generateHtml(plan, chatHistory, systemPrompt) }
            .getOrElse {
                Log.w(FALLBACK_TAG, "HTML 生成失败，切换到兜底服务", it)
                fallback.generateHtml(plan, chatHistory, systemPrompt)
            }

    override suspend fun generateBundle(
        plan: AppPlan,
        chatHistory: List<ChatMessage>,
        systemPrompt: String
    ): GenerationBundle =
        runCatching { primary.generateBundle(plan, chatHistory, systemPrompt) }
            .getOrElse {
                Log.w(FALLBACK_TAG, "生成包失败，切换到兜底服务", it)
                fallback.generateBundle(plan, chatHistory, systemPrompt)
            }
}
