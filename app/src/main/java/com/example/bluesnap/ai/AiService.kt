package com.example.bluesnap.ai

import com.example.bluesnap.data.AppPlan
import com.example.bluesnap.data.ChatMessage
import kotlinx.coroutines.flow.Flow

interface AiService {
    /** 非流式对话 */
    suspend fun chat(messages: List<ChatMessage>): ChatMessage
    /** 流式对话，返回逐步生成的文本片段 */
    fun chatStream(messages: List<ChatMessage>): Flow<String>
    suspend fun generatePlan(chatHistory: List<ChatMessage>): AppPlan
    suspend fun generateHtml(plan: AppPlan, chatHistory: List<ChatMessage>): String
}
