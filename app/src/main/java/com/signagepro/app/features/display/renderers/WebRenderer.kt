package com.signagepro.app.features.display.renderers

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import com.signagepro.app.core.utils.Logger
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.signagepro.app.core.data.model.Content

/**
 * Unified renderer for web content (replaces separate HtmlRenderer and WebPageRenderer)
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebRenderer(
    content: Content,
    modifier: Modifier = Modifier,
    onPageFinishedLoading: (() -> Unit)? = null,
    onContentFinished: () -> Unit = {}
) {
    val context = LocalContext.current
    val webView = remember { WebView(context) }
    var webError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(content.id, (content as? Content.Web)?.url ?: (content as? Content.WebPage)?.url ?: (content as? Content.Html)?.htmlContent) {
        webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    webError = null // Clear error on successful page load
                    onPageFinishedLoading?.invoke()
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        val errorMsg = "Error loading page: ${error?.description} (Code: ${error?.errorCode}) - URL: ${request.url}"
                        Logger.e(errorMsg)
                        webError = errorMsg
                    }
                }

                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    if (request?.isForMainFrame == true) {
                        val errorMsg = "HTTP error loading page: ${errorResponse?.statusCode} ${errorResponse?.reasonPhrase} - URL: ${request.url}"
                        Logger.e(errorMsg)
                        webError = errorMsg
                    }
                }
            }
            
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
                // For TV apps
                // initialScale = 100 
            }

            when (content) {
                is Content.Web -> {
                    if (content.url.isNotEmpty()) loadUrl(content.url)
                    else loadData("<html><body><p>No URL provided for Web content.</p></body></html>", "text/html", "UTF-8")
                }
                is Content.Html -> {
                    if (content.htmlContent.startsWith("http://") || content.htmlContent.startsWith("https://")) {
                        // If htmlContent is a URL to an HTML file
                        loadUrl(content.htmlContent)
                    } else {
                        // If htmlContent is inline HTML
                        loadDataWithBaseURL(content.baseUrl, content.htmlContent, "text/html", "UTF-8", null)
                    }
                }
                is Content.WebPage -> {
                    if (content.url.isNotEmpty()) {
                        settings.javaScriptEnabled = content.enableJavaScript
                        content.userAgent?.let { settings.userAgentString = it }
                        loadUrl(content.url)
                    } else {
                        loadData("<html><body><p>No URL provided for WebPage content.</p></body></html>", "text/html", "UTF-8")
                    }
                }
                else -> {
                    // Handle other content types or error, though this renderer should only receive web types
                    loadData("<html><body><p>Unsupported web content type.</p></body></html>", "text/html", "UTF-8")
                }
            }
        }
        
        onDispose {
            // Proper cleanup if needed
            // webView.destroy() 
        }
    }

    Box(
        modifier = modifier.fillMaxSize(), 
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )
        webError?.let {
            Text(
                text = it.take(200), // Show a truncated error message
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}