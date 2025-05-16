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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPageRenderer(
    webContent: Content.Web,
    modifier: Modifier = Modifier,
    onPageFinishedLoading: (() -> Unit)? = null // Callback when page finishes loading
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
                // You can override other methods like onReceivedError, shouldOverrideUrlLoading etc.
            }
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.domStorageEnabled = true
            // For TV, consider initial scale and other viewport settings
            // settings.initialScale = 100 // Example

            if (webContent.url.isNotEmpty()) {
                loadUrl(webContent.url)
            } else {
                loadData("<html><body><p>No URL provided for web page.</p></body></html>", "text/html", "UTF-8")
            }
        }

        onDispose {
            // webView.destroy() // Consider lifecycle carefully
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize(),
            update = { wv ->
                // If URL changes, reload. This is handled by DisposableEffect keying on url.
            }
        )
    }
    // For WebPage content, onFinished (transition to next item) is typically managed by DisplayViewModel's timer.
    // The onPageFinishedLoading callback is for actions upon the web page itself loading.
}