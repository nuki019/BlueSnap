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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluesnap.data.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    systemPrompt: String,
    activeProviderLabel: String,
    apiKeyModeLabel: String,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSystemPromptChange: (String) -> Unit,
    onBackHome: () -> Unit
) {
    val density = LocalDensity.current
    val safeTopPadding = with(density) {
        WindowInsets.statusBars.getTop(this).toDp().coerceAtMost(44.dp)
    }

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
                text = "控制主题、生成风格和联网模型配置状态。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
            )

            SettingCard(
                title = "深色模式",
                subtitle = "用于答辩录屏和不同系统主题下的可读性检查。",
                icon = { Icon(Icons.Filled.DarkMode, contentDescription = null) }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeModeChip("跟随系统", ThemeMode.SYSTEM, themeMode, onThemeModeChange)
                    ThemeModeChip("浅色", ThemeMode.LIGHT, themeMode, onThemeModeChange)
                    ThemeModeChip("深色", ThemeMode.DARK, themeMode, onThemeModeChange)
                }
            }

            SettingCard(
                title = "System Prompt",
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
                title = "API 与密钥",
                subtitle = "功能占位：展示当前构建配置，不在应用内保存或管理真实密钥。",
                icon = { Icon(Icons.Filled.Api, contentDescription = null) }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(activeProviderLabel) })
                    AssistChip(onClick = {}, label = { Text("Chat Completions") })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = apiKeyModeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "DeepSeek 只作为本机临时评测备用；提交 APK 默认使用蓝心大模型参赛配置，源码和 GitHub 不提交真实 key。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                )
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
