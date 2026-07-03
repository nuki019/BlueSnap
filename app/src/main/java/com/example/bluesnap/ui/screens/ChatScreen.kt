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

    // ????????????????
    LaunchedEffect(messages.size, isGenerating, streamingContent.length) {
        val totalItems = messages.size + if (streamingContent.isNotEmpty()) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    // ??????????????
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

    // Box ?????????????????????????????
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ?? ??????????????? ??
        Column(modifier = Modifier.fillMaxSize()) {
            // ???
            TopAppBar(
                title = {
                    Column {
                        Text("????", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "AI ??????",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
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

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                // ????????????????????????
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = 72.dp
                )
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }

                // ??????????
                if (streamingContent.isNotEmpty()) {
                    item(key = "streaming") {
                        StreamingBubble(content = streamingContent)
                    }
                }

                // ???????????????????
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
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "?????????...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // ??????
                val lastAiMessage = messages.lastOrNull { it.role == Role.ASSISTANT }
                if (lastAiMessage != null && lastAiMessage.content.contains("??") && !isGenerating) {
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
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text("???? ?", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }

        // ?? ?????????????imePadding ????? ??
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
                    placeholder = { Text("??????...") },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submit() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
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
                        contentDescription = "??"
                    )
                }
            }
        }
    }
}
