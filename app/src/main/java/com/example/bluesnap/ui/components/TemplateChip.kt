package com.example.bluesnap.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Template(
    val name: String,
    val icon: String,
    val description: String
)

val defaultTemplates = listOf(
    Template("番茄钟", "🍅", "25分钟专注工作法"),
    Template("待办清单", "📝", "任务管理与追踪"),
    Template("备忘录", "📒", "快速记录与查找"),
    Template("记账本", "💰", "收支记录与统计"),
    Template("习惯打卡", "🔥", "好习惯养成追踪")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateChip(
    template: Template,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = onClick,
        label = {
            Text(
                text = "${template.icon} ${template.name}",
                style = MaterialTheme.typography.labelLarge
            )
        },
        modifier = modifier.padding(end = 8.dp, bottom = 8.dp)
    )
}
