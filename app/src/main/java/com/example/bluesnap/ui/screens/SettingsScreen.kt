package com.example.bluesnap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bluesnap.data.ModelPreset
import com.example.bluesnap.data.ModelPresets
import com.example.bluesnap.data.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    systemPrompt: String,
    activeProviderLabel: String,
    apiKeyModeLabel: String,
    fallbackModelPresetId: String?,
    fallbackModelLabel: String,
    fallbackModelKeyLabel: String,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSystemPromptChange: (String) -> Unit,
    onSaveFallbackModel: (String, String) -> Unit,
    onBackHome: () -> Unit
) {
    val density = LocalDensity.current
    val safeTopPadding = with(density) {
        WindowInsets.statusBars.getTop(this).toDp().coerceAtMost(44.dp)
    }
    var showModelDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = safeTopPadding + 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "系统设置",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "管理主题、生成风格与模型连接状态。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
        )

        SettingCard(
            title = "深色模式",
            subtitle = "调整界面显示效果，让应用在不同环境下保持清晰可读。",
            icon = { Icon(Icons.Filled.DarkMode, contentDescription = null) }
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeModeChip("跟随系统", ThemeMode.SYSTEM, themeMode, onThemeModeChange)
                ThemeModeChip("浅色", ThemeMode.LIGHT, themeMode, onThemeModeChange)
                ThemeModeChip("深色", ThemeMode.DARK, themeMode, onThemeModeChange)
            }
        }

        SettingCard(
            title = "生成风格",
            subtitle = "用于控制生成应用的整体风格，会参与下一次方案和 HTML 生成。",
            icon = { Icon(Icons.Filled.Tune, contentDescription = null) }
        ) {
            OutlinedTextField(
                value = systemPrompt,
                onValueChange = onSystemPromptChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                maxLines = 9,
                shape = RoundedCornerShape(14.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }

        SettingCard(
            title = "模型服务",
            subtitle = "选择备用生成模型，主模型不可用时自动切换。",
            icon = { Icon(Icons.Filled.Api, contentDescription = null) }
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(activeProviderLabel) })
                AssistChip(onClick = {}, label = { Text("DeepSeek（备用）") })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = apiKeyModeLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "备用模型：$fallbackModelLabel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
            )
            Text(
                text = fallbackModelKeyLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showModelDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加模型")
            }
        }

        Button(
            onClick = onBackHome,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Home, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("返回创造页")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showModelDialog) {
        AddModelDialog(
            initialPresetId = fallbackModelPresetId,
            onDismiss = { showModelDialog = false },
            onSave = { preset, apiKey ->
                onSaveFallbackModel(preset.id, apiKey)
                showModelDialog = false
            }
        )
    }
}

@Composable
private fun ThemeModeChip(
    label: String,
    mode: ThemeMode,
    current: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    FilterChip(
        selected = current == mode,
        onClick = { onThemeModeChange(mode) },
        label = { Text(label) }
    )
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                icon()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddModelDialog(
    initialPresetId: String?,
    onDismiss: () -> Unit,
    onSave: (ModelPreset, String) -> Unit
) {
    val presets = ModelPresets.all
    var expanded by remember { mutableStateOf(false) }
    var selectedPreset by remember {
        mutableStateOf(ModelPresets.find(initialPresetId) ?: presets.first())
    }
    var apiKey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加模型") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedPreset.displayName} · ${selectedPreset.modelId}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("模型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        presets.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text("${preset.displayName} · ${preset.modelId}") },
                                onClick = {
                                    selectedPreset = preset
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    placeholder = { Text("粘贴模型服务 Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Text(
                    text = "保存后仅显示已保存状态，生成时自动使用所选模型。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedPreset, apiKey) },
                enabled = apiKey.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
