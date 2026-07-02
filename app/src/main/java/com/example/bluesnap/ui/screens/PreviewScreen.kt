package com.example.bluesnap.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.example.bluesnap.BuildConfig
import com.example.bluesnap.data.GeneratedApp
import com.example.bluesnap.ui.theme.BluePrimary
import java.io.ByteArrayInputStream
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    app: GeneratedApp?,
    isGenerating: Boolean,
    onFeedback: (String) -> Unit,
    onBack: () -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var pendingExportApp by remember { mutableStateOf<GeneratedApp?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        val exportApp = pendingExportApp
        pendingExportApp = null
        if (uri != null && exportApp != null) {
            exportHtml(context, uri, exportApp)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 顶部工具栏
        TopAppBar(
            title = {
                Text(
                    app?.name ?: "预览",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            actions = {
                if (app != null) {
                    TextButton(
                        onClick = {
                            pendingExportApp = app
                            exportLauncher.launch("${safeHtmlFileName(app.name)}.html")
                        }
                    ) {
                        Text("导出")
                    }
                    IconButton(onClick = { shareHtml(context, app) }) {
                        Icon(Icons.Filled.Share, contentDescription = "分享 HTML")
                    }
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
                    AppWebView(app = app)
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
private fun AppWebView(app: GeneratedApp) {
    val baseUrl = "https://${app.id}.localhost/"
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        return !isAllowedWebViewUrl(request?.url)
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request?.url ?: return blockedResponse()
                        return if (isAllowedWebViewUrl(url)) null else blockedResponse()
                    }
                }
                webChromeClient = if (isWebDebugEnabled()) {
                    object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            consoleMessage?.let {
                                Log.d("WebViewJS", "[${it.messageLevel()}] ${it.message()} (${it.sourceId()}:${it.lineNumber()})")
                            }
                            return true
                        }
                    }
                } else {
                    WebChromeClient()
                }
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = false
                    allowContentAccess = false
                    allowFileAccessFromFileURLs = false
                    allowUniversalAccessFromFileURLs = false
                    javaScriptCanOpenWindowsAutomatically = false
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }
                // 使用 https://localhost 作为 base URL，使 WebView 以安全上下文运行
                // 这样 localStorage、sessionStorage 等 API 才能正常工作
                loadDataWithBaseURL(
                    baseUrl,
                    wrapWithDebugBridgeIfNeeded(app.htmlContent),
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                baseUrl,
                wrapWithDebugBridgeIfNeeded(app.htmlContent),
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
private fun wrapWithDebugBridgeIfNeeded(html: String): String {
    if (!isWebDebugEnabled()) return html
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

private fun isWebDebugEnabled(): Boolean = BuildConfig.DEBUG && !BuildConfig.AI_DEMO_MODE

private fun isAllowedWebViewUrl(uri: Uri?): Boolean {
    if (uri == null) return false
    val scheme = uri.scheme?.lowercase() ?: return false
    return when (scheme) {
        "about", "data", "blob" -> true
        "http", "https" -> {
            val host = uri.host?.lowercase()
            host == "localhost" ||
                host?.endsWith(".localhost") == true ||
                host == "127.0.0.1" ||
                host == "::1"
        }
        else -> false
    }
}

private fun blockedResponse(): WebResourceResponse {
    return WebResourceResponse(
        "text/plain",
        "UTF-8",
        ByteArrayInputStream(ByteArray(0))
    )
}

private fun shareHtml(context: Context, app: GeneratedApp) {
    runCatching {
        val dir = File(context.cacheDir, "shared_html").apply { mkdirs() }
        val safeName = safeHtmlFileName(app.name)
        val file = File(dir, "$safeName.html")
        file.writeText(app.htmlContent, Charsets.UTF_8)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, app.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享 HTML"))
    }.onFailure {
        Log.e("PreviewScreen", "分享 HTML 失败", it)
        Toast.makeText(context, "分享失败，请稍后重试", Toast.LENGTH_SHORT).show()
    }
}

private fun exportHtml(context: Context, uri: Uri, app: GeneratedApp) {
    runCatching {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(app.htmlContent.toByteArray(Charsets.UTF_8))
        } ?: error("无法打开导出目标")
        Toast.makeText(context, "HTML 已导出，可离线使用", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Log.e("PreviewScreen", "导出 HTML 失败", it)
        Toast.makeText(context, "导出失败，请重试", Toast.LENGTH_SHORT).show()
    }
}

private fun safeHtmlFileName(name: String): String {
    return name
        .replace(Regex("[^A-Za-z0-9_\\-\\u4e00-\\u9fa5]"), "_")
        .ifBlank { "bluesnap_app" }
}
