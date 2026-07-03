package com.example.bluesnap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluesnap.data.ChatMessage
import com.example.bluesnap.data.Role

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == Role.USER
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val alignment = if (isUser) Arrangement.End else Arrangement.Start
    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = alignment
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // ????
            Text(
                text = if (isUser) "?" else "????",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(
                    start = if (isUser) 0.dp else 4.dp,
                    end = if (isUser) 4.dp else 0.dp,
                    bottom = 2.dp
                )
            )
            // ????
            Text(
                text = message.content,
                modifier = Modifier
                    .clip(shape)
                    .background(bubbleColor)
                    .padding(12.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                color = textColor
            )
        }
    }
}
