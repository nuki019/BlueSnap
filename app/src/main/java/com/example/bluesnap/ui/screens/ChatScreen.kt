package com.example.bluesnap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.bluesnap.data.ChatMessage
import com.example.bluesnap.data.Role
import com.example.bluesnap.ui.components.MessageBubble
import com.example.bluesnap.ui.components.StreamingBubble
import com.example.bluesnap.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    streamingContent: String,
    onSendMessage: (String) -> Unit,
    onViewPlan: () -> Unit,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val inputFocusRequester = remember { FocusRequester() }

    // 自动滚动到底部（包括流式输出时）
    LaunchedEffect(messages.size, isGenerating, streamingContent.length) {
        val totalItems = messages.size + if (streamingContent.isNotEmpty()) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    // 流式输出结束后恢复输入框焦点
    LaunchedEffect(isGenerating) {
        if (!isGenerating) {
            kotlinx.coroutines.delay(100)
            try {
                inputFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    fun submit() {
        if (inputText.isNotBlank() && !isGenerating) {
            onSendMessage(inputText)
            inputText = ""
        }
    }

    // Box 布局：消息列表铺满全屏（不动），输入框浮层叠加（跟随键盘）
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── 消息列表：铺满全屏，不响应键盘 ──
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部栏
            TopAppBar(
                title = {
                    Column {
                        Text("蓝心快搭", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "AI 应用生成助手",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                },
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

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                // 底部留出输入栏高度的空间，避免最后一条消息被遮挡
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = 72.dp
                )
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }

                // 流式输出中的实时气泡
                if (streamingContent.isNotEmpty()) {
                    item(key = "streaming") {
                        StreamingBubble(content = streamingContent)
                    }
                }

                // 生成中指示器（非流式阶段，如方案生成）
                if (isGenerating && streamingContent.isEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = BluePrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "蓝心快搭正在构思中...",
                                style = MaterialTheme.typography.bodySmall,
                                color = BluePrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // 查看方案按钮
                val lastAiMessage = messages.lastOrNull { it.role == Role.ASSISTANT }
                if (lastAiMessage != null && lastAiMessage.content.contains("方案") && !isGenerating) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = onViewPlan,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BluePrimary
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text("查看方案 →", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }

        // ── 输入栏：浮层，固定在底部，imePadding 仅作用于此 ──
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(inputFocusRequester),
                    placeholder = { Text("描述你的需求...") },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submit() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = { submit() },
                    enabled = inputText.isNotBlank() && !isGenerating
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "发送"
                    )
                }
            }
        }
    }
}
