package com.homesports.sportfuntilt

import android.content.Context
import android.content.SharedPreferences

class Preferences private constructor() {
    lateinit var preferences: SharedPreferences

    companion object {
        private val fileName = "APP_PREFERENCES"
        fun create(context: Context): Preferences {
            val appPreferences = Preferences()
            appPreferences.preferences = context.getSharedPreferences(fileName, 0)
            return appPreferences
        }
    }
    val url: String? get() = preferences.getString("URL", "no")//no - отсутствует
    fun storeUrl(url: String) {
        preferences.edit().putString("URL", url).apply()
    }
    val deepLink: String? get() = preferences.getString("DEEP", "")
    fun storeDeeplink(link: String) {
        preferences.edit().putString("DEEP", link).apply()
    }
}