package com.signagepro.app.features.display.renderers

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
    webContent: Content.Web,
    modifier: Modifier = Modifier,
    onPageFinishedLoading: (() -> Unit)? = null,
    onContentFinished: () -> Unit = {}
) {
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    DisposableEffect(webContent.id, webContent.url) {
        webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onPageFinishedLoading?.invoke()
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

            webContent.url.takeIf { it.isNotEmpty() }?.let {
                loadUrl(it)
            } ?: loadData("<html><body><p>No web content available.</p></body></html>", "text/html", "UTF-8")
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
    }
} 