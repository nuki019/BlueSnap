package com.example.bluesnap.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluesnap.data.AppPlan
import com.example.bluesnap.ui.components.PlanCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    plan: AppPlan?,
    isGenerating: Boolean,
    onToggleFeature: (Int) -> Unit,
    onSelectLayout: (Int) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ???
        TopAppBar(
            title = {
                Text("????", style = MaterialTheme.typography.titleMedium)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "??")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        if (plan == null) {
            // ?????
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "?????????????",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            // ????
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ????
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
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
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("??????...", style = MaterialTheme.typography.labelLarge)
                        } else {
                            Text(
                                "????",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("????", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
