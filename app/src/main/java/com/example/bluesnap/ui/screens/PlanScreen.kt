package com.example.bluesnap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluesnap.data.AppPlan
import com.example.bluesnap.data.GenerationStage
import com.example.bluesnap.ui.components.PlanCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    plan: AppPlan?,
    isGenerating: Boolean,
    generationStage: GenerationStage,
    generationLogs: List<String>,
    activeProviderLabel: String,
    apiKeyModeLabel: String,
    onToggleFeature: (Int) -> Unit,
    onSelectLayout: (Int) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text("方案确认", style = MaterialTheme.typography.titleMedium) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        if (plan == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "暂无方案，请先描述你的需求",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                PlanCard(
                    plan = plan,
                    onToggleFeature = onToggleFeature,
                    onSelectLayout = onSelectLayout
                )
                Spacer(modifier = Modifier.height(14.dp))
                GenerationStageStrip(
                    activeStage = generationStage,
                    providerLabel = activeProviderLabel,
                    keyModeLabel = apiKeyModeLabel
                )
                Spacer(modifier = Modifier.height(14.dp))
                GenerationConsole(
                    isGenerating = isGenerating,
                    stage = generationStage,
                    providerLabel = activeProviderLabel,
                    logs = generationLogs
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Text(generationStage.label.ifBlank { "正在生成应用" })
                        } else {
                            Text(
                                "确认生成",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("调整需求", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerationConsole(
    isGenerating: Boolean,
    stage: GenerationStage,
    providerLabel: String,
    logs: List<String>
) {
    var tick by remember(stage, isGenerating, logs.size) { mutableIntStateOf(0) }
    LaunchedEffect(stage, isGenerating, logs.size) {
        while (isGenerating) {
            delay(900)
            tick++
        }
    }

    val animatedLines = buildCodeLines(
        stage = stage,
        providerLabel = providerLabel,
        logs = logs,
        tick = tick
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(
                text = "实时构建日志",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            animatedLines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.82f),
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

private fun buildCodeLines(
    stage: GenerationStage,
    providerLabel: String,
    logs: List<String>,
    tick: Int
): List<String> {
    val cursor = ".".repeat((tick % 3) + 1)
    val base = mutableListOf(
        "> provider = $providerLabel",
        "> protocol = Chat Completions",
        "> stage = ${stage.label.ifBlank { "等待确认" }}$cursor"
    )
    val recentLogs = logs.takeLast(6).map { "> $it" }
    if (recentLogs.isNotEmpty()) base += recentLogs
    if (stage == GenerationStage.BUILDING) {
        base += "> assembling: html + css + javascript"
        base += "> sandbox: no external links, localStorage enabled"
    }
    if (stage == GenerationStage.CHECKING) {
        base += "> checking: complete html, scripts, mobile layout"
    }
    return base.takeLast(10)
}

@Composable
private fun GenerationStageStrip(
    activeStage: GenerationStage,
    providerLabel: String,
    keyModeLabel: String
) {
    val stages = listOf(
        GenerationStage.UNDERSTANDING,
        GenerationStage.PLANNING,
        GenerationStage.BUILDING,
        GenerationStage.CHECKING,
        GenerationStage.LOADING
    )
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(
                text = "联网生成流程",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "$providerLabel · $keyModeLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            stages.forEachIndexed { index, stage ->
                val active = activeStage == stage
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = stage.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    )
                }
            }
        }
    }
}
