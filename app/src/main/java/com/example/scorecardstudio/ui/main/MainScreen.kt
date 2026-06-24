package com.example.scorecardstudio.ui.main

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.view.KeyEvent
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation3.runtime.NavKey
import com.example.scorecardstudio.WebAppInterface

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  var uploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    val results = if (uri != null) arrayOf(uri) else null
    uploadCallback?.onReceiveValue(results)
    uploadCallback = null
  }

  AndroidView(
    factory = { context ->
      var overlayShowing = false

      WebView(context).apply {
        settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          allowFileAccess = false
          allowContentAccess = false
          allowUniversalAccessFromFileURLs = true
          allowFileAccessFromFileURLs = true
          mediaPlaybackRequiresUserGesture = false
          cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
          mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
          saveFormData = true
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            safeBrowsingEnabled = false
          }
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        webViewClient = object : WebViewClient() {
          override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:///android_asset/")) {
              return false
            }
            return true
          }
          override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            handler?.proceed()
          }
          override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
          }
        }
        webChromeClient = object : WebChromeClient() {
          override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
          ): Boolean {
            uploadCallback?.onReceiveValue(null)
            uploadCallback = filePathCallback
            try {
              filePickerLauncher.launch("image/*")
            } catch (e: Exception) {
              uploadCallback?.onReceiveValue(null)
              uploadCallback = null
              return false
            }
            return true
          }
        }
        addJavascriptInterface(object {
          @JavascriptInterface
          fun setOverlayState(visible: Boolean) {
            overlayShowing = visible
          }
        }, "NativeBridge")
        addJavascriptInterface(WebAppInterface(context), "AndroidBridge")

        setOnKeyListener { _, keyCode, event ->
          if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            if (overlayShowing) {
              evaluateJavascript("closeMatchDetail()", null)
              overlayShowing = false
              return@setOnKeyListener true
            }
          }
          false
        }
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()

        loadUrl("file:///android_asset/index.html")
      }
    },
    modifier = modifier.fillMaxSize()
  )
}
