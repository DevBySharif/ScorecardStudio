package com.example.scorecardstudio

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {

  private const val VERSION_URL = "https://footballeon.netlify.app/version.json"
  private const val BASE_URL = "https://footballeon.netlify.app"

  data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String
  )

  suspend fun check(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
    try {
      val localCode = context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
      val url = URL(VERSION_URL)
      val conn = url.openConnection() as HttpURLConnection
      conn.connectTimeout = 5000
      conn.readTimeout = 5000
      val json = conn.inputStream.bufferedReader().readText()
      conn.disconnect()
      val obj = JSONObject(json)
      val remoteCode = obj.getInt("versionCode")
      if (remoteCode > localCode) {
        UpdateInfo(
          versionCode = remoteCode,
          versionName = obj.getString("versionName"),
          apkUrl = obj.getString("apkUrl"),
          releaseNotes = obj.optString("releaseNotes", "Bug fixes and performance improvements")
        )
      } else null
    } catch (_: Exception) { null }
  }

  fun openDownload(context: Context, info: UpdateInfo) {
    val url = if (info.apkUrl.startsWith("http")) info.apkUrl
      else "$BASE_URL${info.apkUrl}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  }
}
