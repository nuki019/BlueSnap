package com.example.bluesnap.data

enum class ModelAuthMode {
    BEARER,
    API_KEY
}

data class ModelPreset(
    val id: String,
    val displayName: String,
    val modelId: String,
    val provider: String,
    val baseUrl: String,
    val authMode: ModelAuthMode = ModelAuthMode.BEARER
) {
    val shortLabel: String
        get() = displayName
}

object ModelPresets {
    val all: List<ModelPreset> = listOf(
        ModelPreset(
            id = "deepseek-v4-flash",
            displayName = "DeepSeek V4 Flash",
            modelId = "deepseek-v4-flash",
            provider = "deepseek",
            baseUrl = "https://api.deepseek.com"
        ),
        ModelPreset(
            id = "deepseek-v4-pro",
            displayName = "DeepSeek V4 Pro",
            modelId = "deepseek-v4-pro",
            provider = "deepseek",
            baseUrl = "https://api.deepseek.com"
        ),
        ModelPreset(
            id = "glm-5.2",
            displayName = "GLM-5.2",
            modelId = "glm-5.2",
            provider = "glm",
            baseUrl = "https://api.z.ai/api/paas/v4"
        ),
        ModelPreset(
            id = "glm-5-turbo",
            displayName = "GLM-5-Turbo",
            modelId = "glm-5-turbo",
            provider = "glm",
            baseUrl = "https://api.z.ai/api/paas/v4"
        ),
        ModelPreset(
            id = "qwen3.7-plus",
            displayName = "Qwen3.7-Plus",
            modelId = "qwen3.7-plus",
            provider = "qwen",
            baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1"
        ),
        ModelPreset(
            id = "qwen3-coder",
            displayName = "Qwen3-Coder",
            modelId = "qwen3-coder",
            provider = "qwen",
            baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1"
        ),
        ModelPreset(
            id = "kimi-k2.7-code",
            displayName = "Kimi K2.7 Code",
            modelId = "kimi-k2.7-code",
            provider = "kimi",
            baseUrl = "https://api.moonshot.cn/v1"
        ),
        ModelPreset(
            id = "kimi-k2.7-code-highspeed",
            displayName = "Kimi K2.7 Code Highspeed",
            modelId = "kimi-k2.7-code-highspeed",
            provider = "kimi",
            baseUrl = "https://api.moonshot.cn/v1"
        ),
        ModelPreset(
            id = "mimo-v2.5-pro",
            displayName = "MiMo-V2.5-Pro",
            modelId = "mimo-v2.5-pro",
            provider = "mimo",
            baseUrl = "https://api.xiaomimimo.com/v1",
            authMode = ModelAuthMode.API_KEY
        ),
        ModelPreset(
            id = "mimo-v2.5-pro-ultraspeed",
            displayName = "MiMo-V2.5-Pro UltraSpeed",
            modelId = "mimo-v2.5-pro-ultraspeed",
            provider = "mimo",
            baseUrl = "https://api.xiaomimimo.com/v1",
            authMode = ModelAuthMode.API_KEY
        )
    )

    fun find(id: String?): ModelPreset? = all.firstOrNull { it.id == id }
}
