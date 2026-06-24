package com.example.scorecardstudio

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.scorecardstudio.theme.ScorecardStudioTheme
import com.footballeon.scorecardstudio.R

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_ScorecardStudio)
    super.onCreate(savedInstanceState)

    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

    FirebaseHelper.init(this)

    if (RootDetector.isDeviceRooted()) {
      FirebaseHelper.logEvent("root_detected", mapOf("label" to "rooted_device"))
    }

    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      FirebaseHelper.recordException(throwable)
    }

    enableEdgeToEdge()
    setContent {
      ScorecardStudioTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }
}
