package com.example.bluesnap.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bluesnap.BuildConfig
import com.example.bluesnap.data.GeneratedApp
import com.example.bluesnap.data.GenerationStage
import java.io.ByteArrayInputStream
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    app: GeneratedApp?,
    isGenerating: Boolean,
    generationStage: GenerationStage,
    onFeedback: (String) -> Unit,
    onSharePage: () -> Unit,
    onBack: () -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }
    var showActions by remember { mutableStateOf(false) }
    var reloadToken by remember { mutableIntStateOf(0) }
    var pendingExportApp by remember { mutableStateOf<GeneratedApp?>(null) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val safeTopPadding = with(density) {
        WindowInsets.statusBars.getTop(this).toDp().coerceAtMost(44.dp)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        val exportApp = pendingExportApp
        pendingExportApp = null
        if (uri != null && exportApp != null) {
            exportHtml(context, uri, exportApp)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            app == null -> EmptyPreviewState(onBack = onBack)
            isGenerating -> GeneratingPreview(stage = generationStage)
            else -> AppWebView(app = app, reloadToken = reloadToken, topPadding = safeTopPadding)
        }

        if (app != null) {
            FilledIconButton(
                onClick = { showActions = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(48.dp)
            ) {
                Icon(Icons.Filled.MoreVert, contentDescription = "操作")
            }
        }
    }

    if (showActions && app != null) {
        ModalBottomSheet(
            onDismissRequest = { showActions = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "生成结果以本地 HTML 方式运行，外链和外部资源会被 WebView 沙箱拦截。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                PreviewActionRow(
                    icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) },
                    title = "返回",
                    subtitle = "回到上一页继续调整",
                    onClick = {
                        showActions = false
                        onBack()
                    }
                )
                PreviewActionRow(
                    icon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                    title = "重载预览",
                    subtitle = "重新加载当前 HTML",
                    onClick = {
                        reloadToken++
                        showActions = false
                    }
                )
                PreviewActionRow(
                    icon = { Icon(Icons.Filled.FileDownload, contentDescription = null) },
                    title = "导出 HTML",
                    subtitle = "保存为单文件，可离线打开",
                    onClick = {
                        pendingExportApp = app
                        exportLauncher.launch("${safeHtmlFileName(app.name)}.html")
                        showActions = false
                    }
                )
                PreviewActionRow(
                    icon = { Icon(Icons.Filled.Share, contentDescription = null) },
                    title = "系统分享 HTML",
                    subtitle = "通过系统分享给同学或伙伴",
                    onClick = {
                        shareHtml(context, app)
                        showActions = false
                    }
                )
                PreviewActionRow(
                    icon = { Icon(Icons.Filled.Upgrade, contentDescription = null) },
                    title = "分享成果页",
                    subtitle = "查看生成摘要和媒体增强建议",
                    onClick = {
                        showActions = false
                        onSharePage()
                    }
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Feedback, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "反馈修改",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = feedbackText,
                                onValueChange = { feedbackText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("例如：把按钮改小一点，增加导出文本") },
                                shape = RoundedCornerShape(18.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (feedbackText.isNotBlank()) {
                                        onFeedback(feedbackText)
                                        feedbackText = ""
                                        focusManager.clearFocus()
                                        showActions = false
                                    }
                                })
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledIconButton(
                                onClick = {
                                    if (feedbackText.isNotBlank()) {
                                        onFeedback(feedbackText)
                                        feedbackText = ""
                                        focusManager.clearFocus()
                                        showActions = false
                                    }
                                },
                                enabled = feedbackText.isNotBlank()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送反馈")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun PreviewActionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
    )
}

@Composable
private fun EmptyPreviewState(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "请先生成一个应用",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack, shape = RoundedCornerShape(12.dp)) {
                Text("返回")
            }
        }
    }
}

@Composable
private fun GeneratingPreview(stage: GenerationStage) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stage.label.ifBlank { "正在生成应用" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
            )
        }
    }
}

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
private fun AppWebView(app: GeneratedApp, reloadToken: Int, topPadding: Dp) {
    val baseUrl = "https://${app.id}.localhost/"
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = sandboxWebViewClient()
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
            }
        },
        update = { webView ->
            val loadTag = "${app.id}-${app.htmlContent.hashCode()}-$reloadToken"
            if (webView.tag != loadTag) {
                webView.tag = loadTag
                webView.loadDataWithBaseURL(
                    baseUrl,
                    wrapWithDebugBridgeIfNeeded(app.htmlContent),
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding)
    )
}

private fun sandboxWebViewClient(): WebViewClient {
    return object : WebViewClient() {
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
}

private fun wrapWithDebugBridgeIfNeeded(html: String): String {
    if (!isWebDebugEnabled()) return html
    val headCloseIndex = html.indexOf("</head>", ignoreCase = true)
    return if (headCloseIndex != -1) {
        html.substring(0, headCloseIndex) +
            "\n<script>$DEBUG_JS_BRIDGE</script>\n" +
            html.substring(headCloseIndex)
    } else {
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
