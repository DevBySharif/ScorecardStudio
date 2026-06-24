package com.example.scorecardstudio.data

import android.content.Context
import android.content.SharedPreferences

class SessionDataStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getString(key: String, default: String = ""): String {
        return prefs.getString(key, default) ?: default
    }

    fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getStringSet(key: String, default: Set<String> = emptySet()): Set<String> {
        return prefs.getStringSet(key, default) ?: default
    }

    fun setStringSet(key: String, value: Set<String>) {
        prefs.edit().putStringSet(key, value).apply()
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "scorecard_session"
        const val KEY_TV_PINNED = "tvPinned"
        const val KEY_LAST_TV_CHANNEL = "lastTVChannel"
        const val KEY_THEME = "appTheme"
        const val KEY_LANGUAGE = "appLanguage"
        const val KEY_LAST_TAB = "lastActiveTab"
    }
}
