package com.example.bluesnap.ai

import com.example.bluesnap.data.ModelAuthMode

data class AiConfig(
    val provider: String,
    val fallbackProvider: String,
    val demoMode: Boolean,
    val apiKey: String,
    val baseUrl: String,
    val model: String,
    val authMode: ModelAuthMode = ModelAuthMode.BEARER
) {
    val hasUsableKey: Boolean
        get() = apiKey.isNotBlank() &&
            !apiKey.equals("YOUR_API_KEY_HERE", ignoreCase = true) &&
            !apiKey.contains("YOUR_API_KEY", ignoreCase = true)
}
