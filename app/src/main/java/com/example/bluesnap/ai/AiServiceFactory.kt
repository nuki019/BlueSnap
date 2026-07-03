package com.example.bluesnap.ai

import android.util.Log
import com.example.bluesnap.BuildConfig

private const val FACTORY_TAG = "AiServiceFactory"

object AiServiceFactory {
    fun create(): AiService {
        return create(buildPrimaryConfig(), buildBuildConfigFallback())
    }

    fun create(fallbackConfig: AiConfig?): AiService {
        return create(buildPrimaryConfig(), fallbackConfig ?: buildBuildConfigFallback())
    }

    fun create(primaryConfig: AiConfig, fallbackConfig: AiConfig?): AiService {
        val primary = serviceFor(primaryConfig)
        val fallback = serviceFor(fallbackConfig)
        val safeFallback = if (fallback is MockAiService) fallback else FallbackAiService(fallback, MockAiService())
        return if (primary is MockAiService) {
            safeFallback
        } else {
            FallbackAiService(primary, safeFallback)
        }
    }

    private fun buildPrimaryConfig(): AiConfig {
        val config = AiConfig(
            provider = BuildConfig.AI_PROVIDER.trim().lowercase(),
            fallbackProvider = BuildConfig.AI_FALLBACK_PROVIDER.trim().lowercase(),
            demoMode = BuildConfig.AI_DEMO_MODE,
            apiKey = BuildConfig.AI_API_KEY.trim(),
            baseUrl = BuildConfig.AI_BASE_URL.trim().trimEnd('/'),
            model = BuildConfig.AI_MODEL.trim()
        )

        if (config.demoMode || !config.hasUsableKey) {
            Log.i(FACTORY_TAG, "使用演示模式 AI 服务")
            return config.copy(provider = "mock")
        }

        return config
    }

    private fun serviceFor(config: AiConfig?): AiService {
        if (config == null || config.demoMode || !config.hasUsableKey) return MockAiService()
        return when (config.provider.lowercase()) {
            "vivo", "deepseek", "glm", "qwen", "kimi", "mimo" -> RealAiService(config)
            "mock" -> MockAiService()
            else -> {
                Log.w(FACTORY_TAG, "未知 AI provider=${config.provider}，使用 Mock")
                MockAiService()
            }
        }
    }

    private fun buildBuildConfigFallback(): AiConfig? {
        return when (BuildConfig.AI_FALLBACK_PROVIDER.trim().lowercase()) {
            "", "mock" -> null
            "deepseek" -> {
                AiConfig(
                    provider = "deepseek",
                    fallbackProvider = "mock",
                    demoMode = false,
                    apiKey = BuildConfig.AI_FALLBACK_API_KEY.trim(),
                    baseUrl = BuildConfig.AI_FALLBACK_BASE_URL.trim().trimEnd('/'),
                    model = BuildConfig.AI_FALLBACK_MODEL.trim()
                )
            }
            "vivo" -> {
                buildPrimaryConfig().copy(provider = "vivo", fallbackProvider = "mock")
            }
            else -> null
        }
    }
}
