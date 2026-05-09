package com.example.bluesnap.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluesnap.ui.theme.AiBubble

/**
 * 流式输出中的 AI 气泡，带有动画省略号指示正在生成。
 */
@Composable
fun StreamingBubble(
    content: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // 角色标签
            Text(
                text = "蓝心快搭",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
            // 气泡内容 + 动画省略号
            Column(
                modifier = Modifier
                    .clip(shape)
                    .background(AiBubble)
                    .padding(12.dp)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                // 动画呼吸省略号，表示正在生成
                Text(
                    text = "●●●",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = dotAlpha * 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
