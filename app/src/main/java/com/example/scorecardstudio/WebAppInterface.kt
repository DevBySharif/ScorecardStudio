package com.example.scorecardstudio

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.scorecardstudio.data.SessionDataStore
import java.io.File
import java.io.FileOutputStream

class WebAppInterface(
    private val context: Context,
    private val onPlayStream: ((url: String, title: String) -> Unit)? = null
) {

    private val sessionStore = SessionDataStore(context)

    @JavascriptInterface
    fun trackEvent(name: String, label: String) {
        FirebaseHelper.logEvent(name, mapOf("label" to label))
    }

    @JavascriptInterface
    fun getFirebaseApiKey(): String {
        return "AIzaSyDeInKnv3pCF4zQHmvqQLwcIAwzS9LKxbc"
    }

    @JavascriptInterface
    fun saveImage(base64Data: String, fileName: String) {
        try {
            val cleanBase64 = if (base64Data.startsWith("data:image")) {
                base64Data.substringAfter("base64,")
            } else {
                base64Data
            }
            val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FootballEon")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                resolver.openOutputStream(imageUri).use { outputStream ->
                    outputStream?.write(imageBytes)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Saved to Gallery: $fileName", Toast.LENGTH_LONG).show()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Scorecard"))
                }
            } else {
                saveAndShareLocalCache(imageBytes, fileName)
            }
        } catch (e: Exception) {
            FirebaseHelper.recordException(e)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Error saving: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveAndShareLocalCache(imageBytes: ByteArray, fileName: String) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, fileName)
            FileOutputStream(file).use { stream ->
                stream.write(imageBytes)
            }
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Handler(Looper.getMainLooper()).post {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Scorecard"))
            }
        } catch (e: Exception) {
            FirebaseHelper.recordException(e)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Fallback share error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    @JavascriptInterface
    fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            FirebaseHelper.recordException(e)
        }
    }

    @JavascriptInterface
    fun fixApp() {
        try {
            Handler(Looper.getMainLooper()).post {
                (context as? Activity)?.recreate()
                Toast.makeText(context, "App Restored & Cache Cleared", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            FirebaseHelper.recordException(e)
        }
    }

    @JavascriptInterface
    fun setOrientation(orientation: String) {
        try {
            Handler(Looper.getMainLooper()).post {
                val activity = context as? Activity
                if (activity != null) {
                    if (orientation == "landscape") {
                        activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    } else if (orientation == "portrait") {
                        activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
            }
        } catch (e: Exception) {
            FirebaseHelper.recordException(e)
        }
    }

    @JavascriptInterface
    fun playNativeStream(url: String, title: String) {
        onPlayStream?.invoke(url, title)
    }

    @JavascriptInterface
    fun getSessionData(key: String): String {
        return try {
            sessionStore.getString(key)
        } catch (e: Exception) {
            FirebaseHelper.recordException(e)
            ""
        }
    }

    @JavascriptInterface
    fun setSessionData(key: String, value: String) {
        try {
            sessionStore.setString(key, value)
        } catch (e: Exception) {
            FirebaseHelper.recordException(e)
        }
    }

    @JavascriptInterface
    fun removeSessionData(key: String) {
        try {
            sessionStore.remove(key)
        } catch (e: Exception) {
            FirebaseHelper.recordException(e)
        }
    }

    @JavascriptInterface
    fun clearSession() {
        try {
            sessionStore.clear()
        } catch (e: Exception) {
            FirebaseHelper.recordException(e)
        }
    }
}

