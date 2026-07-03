package com.example.bluesnap.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.bluesnap.data.GeneratedApp
import java.io.File

internal fun shareHtml(context: Context, app: GeneratedApp) {
    runCatching {
        val dir = File(context.cacheDir, "shared_html").apply { mkdirs() }
        val file = File(dir, "${safeHtmlFileName(app.name)}.html")
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
        context.startActivity(Intent.createChooser(intent, "?? HTML"))
    }.onFailure {
        Log.e("GeneratedAppFileActions", "?? HTML ??", it)
        Toast.makeText(context, "??????????", Toast.LENGTH_SHORT).show()
    }
}

internal fun exportHtml(context: Context, uri: Uri, app: GeneratedApp) {
    runCatching {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(app.htmlContent.toByteArray(Charsets.UTF_8))
        } ?: error("????????")
        Toast.makeText(context, "HTML ?????????", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Log.e("GeneratedAppFileActions", "?? HTML ??", it)
        Toast.makeText(context, "????????", Toast.LENGTH_SHORT).show()
    }
}

internal fun safeHtmlFileName(name: String): String {
    return name
        .replace(Regex("[^A-Za-z0-9_\\-\\u4e00-\\u9fa5]"), "_")
        .ifBlank { "bluesnap_app" }
}
