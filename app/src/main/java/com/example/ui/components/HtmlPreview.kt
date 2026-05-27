package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

class AndroidConsoleInterface(
    private val onLog: (String) -> Unit,
    private val onClear: () -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @android.webkit.JavascriptInterface
    fun logMessage(message: String) {
        mainHandler.post {
            onLog(message)
        }
    }

    @android.webkit.JavascriptInterface
    fun clearLogs() {
        mainHandler.post {
            onClear()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlPreview(
    combinedHtml: String,
    isLivePreview: Boolean,
    triggerToken: Int,
    onLogReceived: (String) -> Unit,
    onConsoleClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }
    var htmlToLoad by remember { mutableStateOf(combinedHtml) }
    var isInitialLoad by remember { mutableStateOf(true) }

    LaunchedEffect(combinedHtml, isLivePreview) {
        if (isInitialLoad) {
            isInitialLoad = false
            htmlToLoad = combinedHtml
            return@LaunchedEffect
        }

        if (isLivePreview) {
            // Debounce typing to prevent WebView rendering overload / crash
            delay(800)
            htmlToLoad = combinedHtml
        }
    }

    // Separately, if triggerToken changes, we load immediately!
    LaunchedEffect(triggerToken) {
        if (!isInitialLoad) {
            htmlToLoad = combinedHtml
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // WebClient configurations
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            val description = error?.description?.toString() ?: "Loading failed"
                            onLogReceived("NETWORK ERROR: $description")
                        }
                    }

                    // Intercept any classical WebView console messages as fallback
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            if (consoleMessage != null) {
                                val prefix = when (consoleMessage.messageLevel()) {
                                    ConsoleMessage.MessageLevel.ERROR -> "SYSTEM ERROR"
                                    ConsoleMessage.MessageLevel.WARNING -> "SYSTEM WARNING"
                                    else -> "SYSTEM LOG"
                                }
                                val formatted = "$prefix: ${consoleMessage.message()} (line ${consoleMessage.lineNumber()})"
                                onLogReceived(formatted)
                            }
                            return true
                        }
                    }

                    // JavaScript configs
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true

                    // Setup custom logging channel back to Compose UI
                    addJavascriptInterface(
                        AndroidConsoleInterface(onLogReceived, onConsoleClear),
                        "AndroidConsole"
                    )
                }
            },
            update = { webView ->
                // Feed the webView with fresh compiled components
                webView.loadDataWithBaseURL(
                    "https://localpreview.internal/",
                    htmlToLoad,
                    "text/html",
                    "utf-8",
                    null
                )
            }
        )

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}
