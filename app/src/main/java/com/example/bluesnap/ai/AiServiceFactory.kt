package com.example.bluesnap.ai

import android.util.Log
import com.example.bluesnap.BuildConfig

private const val FACTORY_TAG = "AiServiceFactory"

object AiServiceFactory {
    fun create(): AiService {
        val config = AiConfig(
            provider = BuildConfig.AI_PROVIDER.trim().lowercase(),
            fallbackProvider = BuildConfig.AI_FALLBACK_PROVIDER.trim().lowercase(),
            demoMode = BuildConfig.AI_DEMO_MODE,
            apiKey = BuildConfig.AI_API_KEY.trim(),
            baseUrl = BuildConfig.AI_BASE_URL.trim().trimEnd('/'),
            model = BuildConfig.AI_MODEL.trim()
        )

        if (config.demoMode || !config.hasUsableKey) {
            Log.i(FACTORY_TAG, "?????? AI ??")
            return MockAiService()
        }

        val primary = when (config.provider) {
            "vivo", "deepseek" -> RealAiService(config)
            "mock" -> MockAiService()
            else -> {
                Log.w(FACTORY_TAG, "?? AI provider=${config.provider}??? Mock")
                MockAiService()
            }
        }

        val fallback = when (config.fallbackProvider) {
            "mock", "" -> MockAiService()
            else -> MockAiService()
        }

        return if (primary is MockAiService) {
            primary
        } else {
            FallbackAiService(primary = primary, fallback = fallback)
        }
    }
}

