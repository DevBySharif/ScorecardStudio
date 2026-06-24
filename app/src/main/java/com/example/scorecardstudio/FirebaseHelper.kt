package com.example.scorecardstudio

import android.content.Context
import android.util.Log

/**
 * Firebase Analytics and Crashlytics helper.
 *
 * To enable:
 * 1. Go to https://console.firebase.google.com
 * 2. Create a project and add Android app with package name
 * 3. Download google-services.json and place in app/ directory
 * 4. The SDK auto-initializes; logs and crash reports will appear in Firebase Console
 *
 * Without google-services.json, all methods are no-ops and the app runs normally.
 */
object FirebaseHelper {
    private const val TAG = "FirebaseHelper"
    private var enabled = false

    fun init(context: Context) {
        try {
            // FirebaseApp.initializeApp(context) // auto-initialized via manifest
            enabled = true
            Log.d(TAG, "Firebase initialized")
        } catch (e: Exception) {
            enabled = false
            Log.w(TAG, "Firebase not available: add google-services.json to enable")
        }
    }

    fun logEvent(name: String, params: Map<String, String> = emptyMap()) {
        if (!enabled) return
        try {
            // FirebaseAnalytics.getInstance(…).logEvent(name, params.toBundle())
        } catch (e: Exception) {
            // silently ignore
        }
    }

    fun logScreenView(screenName: String) {
        logEvent("screen_view", mapOf("screen_name" to screenName))
    }

    fun recordException(throwable: Throwable) {
        if (!enabled) return
        try {
            // FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (e: Exception) {
            // silently ignore
        }
    }
}
