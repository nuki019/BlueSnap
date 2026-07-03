package com.example.bluesnap.ai

import com.example.bluesnap.data.AppPlan
import com.example.bluesnap.data.ChatMessage
import com.example.bluesnap.data.DEFAULT_SYSTEM_PROMPT
import com.example.bluesnap.data.GenerationBundle
import kotlinx.coroutines.flow.Flow

interface AiService {
    /** 非流式对话。 */
    suspend fun chat(messages: List<ChatMessage>): ChatMessage

    /** 流式对话，返回逐步生成的文本片段。 */
    fun chatStream(messages: List<ChatMessage>): Flow<String>

    suspend fun generatePlan(
        chatHistory: List<ChatMessage>,
        systemPrompt: String = DEFAULT_SYSTEM_PROMPT
    ): AppPlan

    suspend fun generateHtml(
        plan: AppPlan,
        chatHistory: List<ChatMessage>,
        systemPrompt: String = DEFAULT_SYSTEM_PROMPT
    ): String

    suspend fun generateBundle(
        plan: AppPlan,
        chatHistory: List<ChatMessage>,
        systemPrompt: String = DEFAULT_SYSTEM_PROMPT
    ): GenerationBundle {
        return GenerationBundle(
            html = generateHtml(plan, chatHistory, systemPrompt),
            summary = plan.description
        )
    }
}
