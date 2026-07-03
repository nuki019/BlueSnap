package com.example.bluesnap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.bluesnap.ui.components.Template
import com.example.bluesnap.ui.components.defaultTemplates

@Composable
fun HomeScreen(
    onSendMessage: (String) -> Unit,
    onTemplateClick: (String) -> Unit,
    onNavigateToChat: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    fun submit() {
        if (inputText.isNotBlank()) {
            onSendMessage(inputText)
            inputText = ""
            focusManager.clearFocus()
            onNavigateToChat()
        }
    }

    val pageBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFEAF4FF),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        HeroPanel()

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "????????",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "????????????????",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submit() }),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { submit() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = inputText.isNotBlank(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("????", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "????",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        Text(
            text = "????????????????????",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            modifier = Modifier.padding(start = 2.dp, bottom = 12.dp)
        )

        val scenarios = campusScenarios()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            scenarios.take(2).forEach { template ->
                ScenarioCard(
                    template = template,
                    onClick = { onTemplateClick(template.prompt) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            scenarios.drop(2).forEach { template ->
                ScenarioCard(
                    template = template,
                    onClick = { onTemplateClick(template.prompt) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "?????",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            defaultTemplates.take(3).forEach { template ->
                SuggestionChip(
                    onClick = { onTemplateClick(template.prompt) },
                    label = { Text(template.name) },
                    icon = {
                        Icon(
                            imageVector = template.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            defaultTemplates.drop(3).forEach { template ->
                SuggestionChip(
                    onClick = { onTemplateClick(template.prompt) },
                    label = { Text(template.name) },
                    icon = {
                        Icon(
                            imageVector = template.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp)
                )
            }
        }

        Text(
            text = "?? vivo ????? ? Demo-safe ???????",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 24.dp)
        )
    }
}

@Composable
private fun HeroPanel() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(182.dp)
            .shadow(10.dp, RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0B7CFF), Color(0xFF0062E6), Color(0xFF3AA7FF))
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(22.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = "CAMPUS TOOLKIT",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "????",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "??????????????????????????????????",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
                modifier = Modifier.widthIn(max = 230.dp)
            )
        }

        DecorativeStack(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
        )
    }
}

@Composable
private fun DecorativeStack(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(82.dp)) {
        repeat(3) { index ->
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = (index * 11).dp, y = (index * 10).dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.16f + index * 0.08f)
            ) {}
        }
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .size(42.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.92f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.DashboardCustomize,
                    contentDescription = null,
                    tint = Color(0xFF0B7CFF),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private fun campusScenarios(): List<Template> = listOf(
    Template(
        name = "????",
        icon = Icons.Filled.EventAvailable,
        description = "???????????",
        prompt = "???????????????????????????????"
    ),
    Template(
        name = "????",
        icon = Icons.Filled.School,
        description = "?? DDL ?????",
        prompt = "?????????????????? DDL??????????"
    ),
    Template(
        name = "????",
        icon = Icons.Filled.WorkOutline,
        description = "??????????",
        prompt = "???????????????????????????????"
    ),
    Template(
        name = "????",
        icon = Icons.Filled.Savings,
        description = "??????????",
        prompt = "???????????????????????????"
    )
)

@Composable
private fun ScenarioCard(
    template: Template,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(132.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(13.dp),
                color = Color(0xFFEAF4FF)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = template.icon,
                        contentDescription = null,
                        modifier = Modifier.size(21.dp),
                        tint = Color(0xFF0B7CFF)
                    )
                }
            }
            Column {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1
                )
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFF0B7CFF)
                ) {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
