package com.signagepro.app.features.display.renderers

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.signagepro.app.core.data.local.model.MediaItemEntity
import com.signagepro.app.core.utils.Logger

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebRenderer(mediaItem: MediaItemEntity) {
    val context = LocalContext.current
    val urlToLoad = mediaItem.url

    // Keep track of the WebView instance to manage its lifecycle
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = {
                WebView(it).apply {
                    webViewInstance = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            Logger.d("WebRenderer: Page loading started: $url for item ${mediaItem.id}")
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Logger.d("WebRenderer: Page loading finished: $url for item ${mediaItem.id}")
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            Logger.e("WebRenderer: Error loading page $failingUrl: $errorCode - $description")
                            // TODO: Handle webview errors, maybe show a placeholder or message
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(false)
                    // For autoplaying videos in WebView, these might be needed:
                    settings.mediaPlaybackRequiresUserGesture = false

                    Logger.i("WebRenderer: Attempting to load URL: $urlToLoad for media item ${mediaItem.id}")
                    loadUrl(urlToLoad)
                }
            },
            update = { webView ->
                // This will be called if mediaItem.url changes while the same WebRenderer is on screen.
                // This ensures the WebView loads the new URL.
                if (webView.originalUrl != urlToLoad && urlToLoad.isNotBlank()) {
                    Logger.i("WebRenderer: Updating WebView to load new URL: $urlToLoad for item ${mediaItem.id}")
                    webView.loadUrl(urlToLoad)
                } else if (urlToLoad.isBlank()){
                    Logger.w("WebRenderer: Attempted to load a blank URL for item ${mediaItem.id}")
                    webView.loadData("<html><body><h1>Invalid URL</h1></body></html>", "text/html", "UTF-8")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    DisposableEffect(urlToLoad) { // Re-run if urlToLoad changes, to re-setup
        onDispose {
            webViewInstance?.let {
                Logger.d("WebRenderer: Disposing WebView for item ${mediaItem.id}, URL: ${it.originalUrl}")
                it.stopLoading() // Stop any ongoing loading
                it.loadUrl("about:blank") // Load a blank page to release resources
                it.clearHistory()
                it.clearCache(true)
                // it.removeAllViews() // Not always necessary, can cause issues if not handled well
                it.destroy() // Crucial for preventing memory leaks
                webViewInstance = null
            }
        }
    }
    // Debug overlay (optional)
    // Text("Web: ${mediaItem.id}", color = Color.Black.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))

} 