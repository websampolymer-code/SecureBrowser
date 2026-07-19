package com.securebrowser.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.webkit.WebView

class ScreenOffReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_OFF) {
            clearAllData(context)
            killApp()
        }
    }

    private fun clearAllData(context: Context?) {
        context?.let { ctx ->
            // Clear WebView cache
            try {
                val webView = WebView(ctx)
                webView.clearHistory()
                webView.clearCache(true)
                webView.clearFormData()
                webView.clearSslPreferences()
                webView.destroy()
            } catch (_: Exception) {}

            // Clear shared preferences
            val prefs = ctx.getSharedPreferences("secure_browser_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            // Clear all app data
            try {
                ctx.cacheDir?.deleteRecursively()
                ctx.filesDir?.deleteRecursively()
                ctx.getExternalFilesDir(null)?.deleteRecursively()
                ctx.externalCacheDir?.deleteRecursively()
            } catch (_: Exception) {}
        }
    }

    private fun killApp() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
