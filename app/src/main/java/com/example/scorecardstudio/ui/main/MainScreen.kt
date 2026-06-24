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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  var isFullscreen by remember { mutableStateOf(false) }
  var pendingCid by remember { mutableStateOf<String?>(null) }
  var overlayShowing by remember { mutableStateOf(false) }
  val context = LocalContext.current

  val exoPlayer = remember {
    ExoPlayer.Builder(context).build().apply {
      playWhenReady = true
      repeatMode = Player.REPEAT_MODE_OFF
      addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
          if (state == Player.STATE_READY) {
            pendingCid?.let { cid ->
              webViewRef?.evaluateJavascript("markStreamWorking('$cid')", null)
            }
            pendingCid = null
          }
        }
      })
    }
  }

  DisposableEffect(Unit) {
    onDispose { exoPlayer.release() }
  }

  LaunchedEffect(playerUrl) {
    if (playerUrl != null) {
      android.util.Log.d("ExoPlayer", "Playing: $playerUrl")
      val mediaItem = MediaItem.fromUri(playerUrl!!)
      exoPlayer.setMediaItem(mediaItem)
      exoPlayer.prepare()
      webViewRef?.evaluateJavascript("hidePlayerContainer()", null)
    } else {
      exoPlayer.stop()
      exoPlayer.clearMediaItems()
      webViewRef?.evaluateJavascript("showPlayerContainer()", null)
    }
  }

  LaunchedEffect(isFullscreen) {
    val activity = context as? androidx.activity.ComponentActivity
    if (isFullscreen) {
      activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
      activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
      activity?.window?.decorView?.systemUiVisibility = (
        android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
        android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
        android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
      )
    } else {
      activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
      activity?.window?.decorView?.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
    }
  }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    val results = if (uri != null) arrayOf(uri) else null
    uploadCallback?.onReceiveValue(results)
    uploadCallback = null
  }

  androidx.activity.compose.BackHandler(playerUrl != null || isFullscreen || overlayShowing) {
    if (overlayShowing) {
      webViewRef?.evaluateJavascript("closeMatchDetail()", null)
      overlayShowing = false
    } else if (isFullscreen) {
      isFullscreen = false
    } else {
      playerUrl = null
    }
  }

  Column(modifier = modifier.fillMaxSize()) {
    if (playerUrl != null) {
      if (isFullscreen) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
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
          IconButton(
            onClick = { isFullscreen = false },
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
          ) {
            Text("←", color = Color.White, fontSize = 22.sp)
          }
        }
      } else {
        Column(
          modifier = Modifier.fillMaxWidth().background(Color.Black)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(onClick = { playerUrl = null }) {
              Text("✕", color = Color.White, fontSize = 16.sp)
            }
            Spacer(Modifier.width(4.dp))
            Text(text = playerTitle, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { isFullscreen = true }) {
              Text("⛶", color = Color.White, fontSize = 18.sp)
            }
          }
          AndroidView(
            factory = { ctx ->
              PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setBackgroundColor(android.graphics.Color.BLACK)
              }
            },
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
          )
        }
      }
    }

    AndroidView(
      factory = { ctx ->
        WebView(ctx).apply {
          webViewRef = this
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
          addJavascriptInterface(WebAppInterface(context) { url, title, cid ->
            playerUrl = url
            playerTitle = title
            pendingCid = cid
          }, "AndroidBridge")

          isFocusable = true
          isFocusableInTouchMode = true
          requestFocus()

          loadUrl("file:///android_asset/index.html")
        }
      },
      modifier = if (playerUrl != null) Modifier.weight(1f) else Modifier.fillMaxSize()
    )
  }
}
