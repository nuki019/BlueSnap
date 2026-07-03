package com.example.bluesnap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluesnap.data.AppPlan

@Composable
fun PlanCard(
    plan: AppPlan,
    onToggleFeature: (Int) -> Unit,
    onSelectLayout: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // ???????
        Text(
            text = plan.name,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = plan.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // ????
        Text(
            text = "????",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                plan.features.forEachIndexed { index, feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleFeature(index) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = feature.enabled,
                            onCheckedChange = { onToggleFeature(index) }
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(
                                text = feature.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = feature.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                    if (index < plan.features.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // ??????
        Text(
            text = "????",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            plan.layouts.forEachIndexed { index, layout ->
                val isSelected = index == plan.layoutIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onSelectLayout(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = layout,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // ??????
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "?????????? HTML ??????????????",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
