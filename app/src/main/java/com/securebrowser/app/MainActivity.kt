package com.securebrowser.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnGo: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnClose: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        progressBar = findViewById(R.id.progressBar)
        btnGo = findViewById(R.id.btnGo)
        btnBack = findViewById(R.id.btnBack)
        btnForward = findViewById(R.id.btnForward)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnClose = findViewById(R.id.btnClose)

        setupWebView()
        setupUrlBar()
        setupButtons()

        if (savedInstanceState == null) {
            webView.loadUrl("https://www.google.com")
        }
    }

    private fun isScreenOff(): Boolean {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        return !pm.isInteractive
    }

    override fun onStop() {
        super.onStop()
        if (isScreenOff()) {
            clearAndClose()
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = false
            databaseEnabled = false
            setGeolocationEnabled(false)
            saveFormData = false
            savePassword = false
            cacheMode = WebSettings.LOAD_NO_CACHE
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            setSupportMultipleWindows(false)
        }

        webView.webViewClient = SecureWebViewClient(this)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }
        }

        webView.clearHistory()
        webView.clearCache(true)
        webView.clearFormData()
    }

    private fun setupUrlBar() {
        urlBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                navigate(urlBar.text.toString().trim())
                true
            } else false
        }
    }

    private fun setupButtons() {
        btnGo.setOnClickListener {
            navigate(urlBar.text.toString().trim())
        }

        btnBack.setOnClickListener {
            if (webView.canGoBack()) webView.goBack()
        }

        btnForward.setOnClickListener {
            if (webView.canGoForward()) webView.goForward()
        }

        btnRefresh.setOnClickListener {
            webView.reload()
        }

        btnClose.setOnClickListener {
            clearAndClose()
        }
    }

    private fun navigate(input: String) {
        if (input.isEmpty()) return
        val url = if (input.startsWith("http://") || input.startsWith("https://")) {
            input
        } else if (input.contains(".") && !input.contains(" ")) {
            "https://$input"
        } else {
            "https://www.google.com/search?q=${java.net.URLEncoder.encode(input, "UTF-8")}"
        }
        webView.loadUrl(url)
        hideKeyboard()
        urlBar.clearFocus()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(urlBar.windowToken, 0)
    }

    private fun clearAndClose() {
        webView.stopLoading()
        webView.clearHistory()
        webView.clearCache(true)
        webView.clearFormData()
        webView.loadUrl("about:blank")

        try {
            cacheDir?.deleteRecursively()
            filesDir?.deleteRecursively()
            externalCacheDir?.deleteRecursively()
            getSharedPreferences("secure_browser_prefs", Context.MODE_PRIVATE)
                .edit().clear().commit()
        } catch (_: Exception) {}

        finishAffinity()
        System.exit(0)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.clearHistory()
        webView.clearCache(true)
        webView.clearFormData()
        webView.destroy()
        super.onDestroy()
    }
}
