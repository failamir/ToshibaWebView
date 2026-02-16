package com.example.toshibawebview

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val PREFS_NAME = "WebViewPrefs"
    private val KEY_LAST_URL = "last_url"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        // Configure WebView for TV
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.mediaPlaybackRequiresUserGesture = false // Important for autoplay videos
        
        // Improve rendering performance
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        
        // Mixed content (HTTP & HTTPS)
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
        
        webView.webChromeClient = WebChromeClient()

        // Show popup to enter URL
        showUrlDialog()
    }

    private fun showUrlDialog() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastUrl = prefs.getString(KEY_LAST_URL, "https://google.com")

        val input = EditText(this)
        input.inputType = InputType.TYPE_TEXT_VARIATION_URI
        input.setText(lastUrl)
        input.hint = getString(R.string.enter_url_hint)
        input.setSelection(input.text.length) // Cursor at end

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.enter_url_title))
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.go_button)) { dialog, _ ->
            var url = input.text.toString().trim()
            if (url.isNotEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }
                
                // Save URL
                prefs.edit().putString(KEY_LAST_URL, url).apply()
                
                // Load URL
                loadUrl(url)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
            dialog.cancel()
            // If cancelled on first launch and no URL loaded, maybe exit or show empty
            if (webView.url == null) {
                finish() 
            }
        }
        
        // Make sure dialog is cancellable but we handle back press
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()
        
        // Request focus on the input field for TV remote
        input.requestFocus()
    }

    private fun loadUrl(url: String) {
        webView.loadUrl(url)
        webView.requestFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    
    // Handle back button specifically for TV remotes if onKeyDown doesn't catch it
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            // Option: Show dialog again on exit? Or just exit.
            // Let's show the dialog again in case they want to change URL
            AlertDialog.Builder(this)
                .setTitle("Exit or Change URL?")
                .setMessage("Do you want to exit the app or change the URL?")
                .setPositiveButton("Change URL") { _, _ -> showUrlDialog() }
                .setNegativeButton("Exit") { _, _ -> super.onBackPressed() }
                .show()
        }
    }
}
