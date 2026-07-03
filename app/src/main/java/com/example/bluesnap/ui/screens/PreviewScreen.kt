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
import com.example.bluesnap.BuildConfig
import com.example.bluesnap.data.GeneratedApp
import java.io.ByteArrayInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    app: GeneratedApp?,
    isGenerating: Boolean,
    onFeedback: (String) -> Unit,
    onSharePage: () -> Unit,
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
        // ?????
        TopAppBar(
            title = {
                Text(
                    app?.name ?: "??",
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
                        Text("??")
                    }
                    IconButton(onClick = { shareHtml(context, app) }) {
                        Icon(Icons.Filled.Share, contentDescription = "?? HTML")
                    }
                    TextButton(onClick = onSharePage) {
                        Text("????")
                    }
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

        if (app == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("?????????", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            // WebView ???
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (isGenerating) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "??????...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    AppWebView(app = app)
                }
            }

            // ???????
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
                        placeholder = { Text("????????????...") },
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
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
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
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "????")
                    }
                }
            }
        }
    }
}

/**
 * ??? WebView ?? JS ?????
 * ?? console.log / console.error / ???????? Android Logcat?
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
                // ?? https://localhost ?? base URL?? WebView ????????
                // ?? localStorage?sessionStorage ? API ??????
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
 * ? HTML ? <head> ?????????????? JS ???
 */
private fun wrapWithDebugBridgeIfNeeded(html: String): String {
    if (!isWebDebugEnabled()) return html
    // ?? HTML ?? </head>???????????
    val headCloseIndex = html.indexOf("</head>", ignoreCase = true)
    return if (headCloseIndex != -1) {
        html.substring(0, headCloseIndex) +
            "\n<script>$DEBUG_JS_BRIDGE</script>\n" +
            html.substring(headCloseIndex)
    } else {
        // ?? </head> ??????????
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
