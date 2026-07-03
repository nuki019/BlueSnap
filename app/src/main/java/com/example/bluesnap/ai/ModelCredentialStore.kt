package com.example.bluesnap.ai

import android.content.Context
import com.example.bluesnap.data.ModelPreset
import com.example.bluesnap.data.ModelPresets

data class SavedModelCredential(
    val presetId: String,
    val hasKey: Boolean
) {
    val preset: ModelPreset?
        get() = ModelPresets.find(presetId)
}

class ModelCredentialStore(context: Context) {
    private val prefs = context.getSharedPreferences("bluesnap_model_credentials", Context.MODE_PRIVATE)

    fun load(): SavedModelCredential? {
        val presetId = prefs.getString(KEY_PRESET_ID, null).orEmpty()
        if (presetId.isBlank() || ModelPresets.find(presetId) == null) return null
        return SavedModelCredential(
            presetId = presetId,
            hasKey = loadApiKey().isNotBlank()
        )
    }

    fun loadApiKey(): String = prefs.getString(KEY_API_KEY, "").orEmpty()

    fun save(preset: ModelPreset, apiKey: String) {
        prefs.edit()
            .putString(KEY_PRESET_ID, preset.id)
            .putString(KEY_API_KEY, apiKey.trim())
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_PRESET_ID = "preset_id"
        private const val KEY_API_KEY = "api_key"
    }
}
