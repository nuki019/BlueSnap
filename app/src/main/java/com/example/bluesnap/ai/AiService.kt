package com.example.bluesnap.ai

import com.example.bluesnap.data.AppPlan
import com.example.bluesnap.data.ChatMessage
import com.example.bluesnap.data.GenerationBundle
import kotlinx.coroutines.flow.Flow

interface AiService {
    /** ????? */
    suspend fun chat(messages: List<ChatMessage>): ChatMessage
    /** ???????????????? */
    fun chatStream(messages: List<ChatMessage>): Flow<String>
    suspend fun generatePlan(chatHistory: List<ChatMessage>): AppPlan
    suspend fun generateHtml(plan: AppPlan, chatHistory: List<ChatMessage>): String
    suspend fun generateBundle(plan: AppPlan, chatHistory: List<ChatMessage>): GenerationBundle {
        return GenerationBundle(
            html = generateHtml(plan, chatHistory),
            summary = plan.description
        )
    }
}
