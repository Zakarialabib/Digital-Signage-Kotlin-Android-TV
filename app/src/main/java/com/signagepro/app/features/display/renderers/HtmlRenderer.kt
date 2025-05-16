package com.signagepro.app.features.display.renderers

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

@Composable
fun HtmlRenderer(
    webContent: Content.Web,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit // HTML content might have internal timers or be indefinite
) {
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    DisposableEffect(webContent.id, webContent.url) {
        webView.apply {
            webViewClient = object : WebViewClient() {
                // You might want to override onPageFinished if loading a URL
                // and then call onFinished, but for raw HTML or indefinite duration,
                // onFinished might be triggered by the DisplayViewModel's timer.
            }
            settings.javaScriptEnabled = true // Be cautious with JavaScript from untrusted sources
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.domStorageEnabled = true

            // Load the URL since Content.Web primarily uses URL
            if (webContent.url.isNotEmpty()) {
                loadUrl(webContent.url)
            } else {
                // Handle case where URL is not provided
                loadData("<html><body><p>No HTML content provided.</p></body></html>", "text/html", "UTF-8")
            }
        }
        onDispose {
            // webView.destroy() // Consider lifecycle; might be an issue if remembered across compositions
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize(),
            update = { wv ->
                // This block is called when the view is recomposed with new data.
                // We've handled data loading in DisposableEffect for simplicity here,
                // but complex updates might need to be managed here.
            }
        )
    }
    // For HTML content, onFinished is typically managed by the DisplayViewModel's timer
    // unless the HTML itself has a mechanism to signal completion (e.g., via JavaScript interface).
}