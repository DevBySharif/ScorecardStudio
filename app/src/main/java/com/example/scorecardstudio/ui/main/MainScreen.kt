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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation3.runtime.NavKey
import com.example.scorecardstudio.WebAppInterface

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  var uploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
  var playerUrl by remember { mutableStateOf<String?>(null) }
  var playerTitle by remember { mutableStateOf("") }
  val context = LocalContext.current

  val exoPlayer = remember {
    ExoPlayer.Builder(context).build().apply {
      playWhenReady = true
      repeatMode = Player.REPEAT_MODE_OFF
    }
  }

  DisposableEffect(Unit) {
    onDispose { exoPlayer.release() }
  }

  LaunchedEffect(playerUrl) {
    if (playerUrl != null) {
      val mediaItem = MediaItem.fromUri(playerUrl!!)
      exoPlayer.setMediaItem(mediaItem)
      exoPlayer.prepare()
    } else {
      exoPlayer.stop()
      exoPlayer.clearMediaItems()
    }
  }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    val results = if (uri != null) arrayOf(uri) else null
    uploadCallback?.onReceiveValue(results)
    uploadCallback = null
  }

  Box(modifier = modifier.fillMaxSize()) {
    AndroidView(
      factory = { ctx ->
        var overlayShowing = false

        WebView(ctx).apply {
          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            allowUniversalAccessFromFileURLs = true
            allowFileAccessFromFileURLs = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            saveFormData = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              safeBrowsingEnabled = false
            }
            userAgentString = "Mozilla/5.0 (Linux; Android 14; SM-A146U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.200 Mobile Safari/537.36"
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
          addJavascriptInterface(WebAppInterface(context) { url, title ->
            playerUrl = url
            playerTitle = title
          }, "AndroidBridge")

          setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
              if (overlayShowing) {
                evaluateJavascript("closeMatchDetail()", null)
                overlayShowing = false
                return@setOnKeyListener true
              }
              if (playerUrl != null) {
                playerUrl = null
                return@setOnKeyListener true
              }
            }
            false
          }
          isFocusable = true
          isFocusableInTouchMode = true
          requestFocus()

          val htmlContent = ctx.assets.open("index.html").bufferedReader().use { it.readText() }
          loadDataWithBaseURL("https://app.scorecardstudio.local/", htmlContent, "text/html", "UTF-8", null)
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    if (playerUrl != null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black)
      ) {
        AndroidView(
          factory = { ctx ->
            PlayerView(ctx).apply {
              player = exoPlayer
              useController = true
              resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
              setBackgroundColor(android.graphics.Color.BLACK)
            }
          },
          modifier = Modifier.fillMaxSize()
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = { playerUrl = null }) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Close",
              tint = Color.White
            )
          }
          Spacer(Modifier.width(8.dp))
          Text(
            text = playerTitle,
            color = Color.White,
            fontSize = 16.sp
          )
        }
      }
    }
  }
}
