package com.example.bluesnap.ai

import android.util.Log
import com.example.bluesnap.data.AppPlan
import com.example.bluesnap.data.ChatMessage
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
                Log.w(FALLBACK_TAG, "????????? Mock", it)
                fallback.chat(messages)
            }

    override fun chatStream(messages: List<ChatMessage>): Flow<String> =
        primary.chatStream(messages).catch {
            Log.w(FALLBACK_TAG, "????????? Mock", it)
            fallback.chatStream(messages).collect { content -> emit(content) }
        }

    override suspend fun generatePlan(chatHistory: List<ChatMessage>): AppPlan =
        runCatching { primary.generatePlan(chatHistory) }
            .getOrElse {
                Log.w(FALLBACK_TAG, "????????? Mock", it)
                fallback.generatePlan(chatHistory)
            }

    override suspend fun generateHtml(plan: AppPlan, chatHistory: List<ChatMessage>): String =
        runCatching { primary.generateHtml(plan, chatHistory) }
            .getOrElse {
                Log.w(FALLBACK_TAG, "HTML ??????? Mock", it)
                fallback.generateHtml(plan, chatHistory)
            }

    override suspend fun generateBundle(plan: AppPlan, chatHistory: List<ChatMessage>): GenerationBundle =
        runCatching { primary.generateBundle(plan, chatHistory) }
            .getOrElse {
                Log.w(FALLBACK_TAG, "???????? Mock", it)
                fallback.generateBundle(plan, chatHistory)
            }
}
