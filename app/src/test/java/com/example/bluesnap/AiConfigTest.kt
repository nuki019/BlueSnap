package com.example.bluesnap

import com.example.bluesnap.ai.AiConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiConfigTest {
    @Test
    fun placeholderKey_isNotUsable() {
        val config = testConfig(apiKey = "YOUR_API_KEY_HERE")

        assertFalse(config.hasUsableKey)
    }

    @Test
    fun blankKey_isNotUsable() {
        val config = testConfig(apiKey = "")

        assertFalse(config.hasUsableKey)
    }

    @Test
    fun configuredKey_isUsable() {
        val config = testConfig(apiKey = "demo-valid-key")

        assertTrue(config.hasUsableKey)
    }

    private fun testConfig(apiKey: String) = AiConfig(
        provider = "vivo",
        fallbackProvider = "mock",
        demoMode = true,
        apiKey = apiKey,
        baseUrl = "https://api-ai.vivo.com.cn/v1",
        model = "Doubao-Seed-2.0-pro"
    )
}
