package com.example.bluesnap.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bluesnap.data.GeneratedApp
import com.example.bluesnap.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    app: GeneratedApp?,
    isGenerating: Boolean,
    onFeedback: (String) -> Unit,
    onBack: () -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 顶部工具栏
        TopAppBar(
            title = {
                Text(
                    app?.name ?: "预览",
                    style = MaterialTheme.typography.titleMedium
                )
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

        if (app == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无应用，请先生成", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            // WebView 预览区
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (isGenerating) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = BluePrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "正在生成应用...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    AppWebView(htmlContent = app.htmlContent)
                }
            }

            // 底部反馈输入栏
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("不满意？描述你想要的修改...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (feedbackText.isNotBlank()) {
                                onFeedback(feedbackText)
                                feedbackText = ""
                                focusManager.clearFocus()
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (feedbackText.isNotBlank()) {
                                onFeedback(feedbackText)
                                feedbackText = ""
                                focusManager.clearFocus()
                            }
                        },
                        enabled = feedbackText.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送反馈")
                    }
                }
            }
        }
    }
}

/**
 * 注入到 WebView 中的 JS 调试脚本。
 * 捕获 console.log / console.error / 全局异常，桥接到 Android Logcat。
 */
private const val DEBUG_JS_BRIDGE = """
(function() {
    function sendToAndroid(level, args) {
        try {
            var msg = Array.prototype.slice.call(args).map(String).join(' ');
            if (window.AndroidConsole) {
                window.AndroidConsole.log(level, msg);
            }
        } catch(e) {}
    }
    var origLog = console.log;
    var origError = console.error;
    var origWarn = console.warn;
    console.log = function() { origLog.apply(console, arguments); sendToAndroid('LOG', arguments); };
    console.error = function() { origError.apply(console, arguments); sendToAndroid('ERROR', arguments); };
    console.warn = function() { origWarn.apply(console, arguments); sendToAndroid('WARN', arguments); };
    window.onerror = function(msg, url, line, col, error) {
        sendToAndroid('ERROR', ['JS Error: ' + msg + ' at line ' + line]);
        return false;
    };
    window.addEventListener('unhandledrejection', function(event) {
        sendToAndroid('ERROR', ['Unhandled Promise: ' + event.reason]);
    });
})();
"""

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun AppWebView(htmlContent: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            Log.d("WebViewJS", "[${it.messageLevel()}] ${it.message()} (${it.sourceId()}:${it.lineNumber()})")
                        }
                        return true
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = false
                    allowContentAccess = false
                    javaScriptCanOpenWindowsAutomatically = false
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    // 允许混合内容（本地生成的 HTML 可能包含 data: URI）
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                // 使用 https://localhost 作为 base URL，使 WebView 以安全上下文运行
                // 这样 localStorage、sessionStorage 等 API 才能正常工作
                loadDataWithBaseURL(
                    "https://localhost",
                    wrapWithDebugBridge(htmlContent),
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                "https://localhost",
                wrapWithDebugBridge(htmlContent),
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * 在 HTML 的 <head> 中注入调试桥接脚本，用于捕获 JS 错误。
 */
private fun wrapWithDebugBridge(html: String): String {
    // 如果 HTML 包含 </head>，在其前面注入调试脚本
    val headCloseIndex = html.indexOf("</head>", ignoreCase = true)
    return if (headCloseIndex != -1) {
        html.substring(0, headCloseIndex) +
            "\n<script>$DEBUG_JS_BRIDGE</script>\n" +
            html.substring(headCloseIndex)
    } else {
        // 没有 </head> 标签，直接在开头注入
        "<script>$DEBUG_JS_BRIDGE</script>$html"
    }
}
