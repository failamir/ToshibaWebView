package com.example.toshibawebview

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
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
    private val KEY_WIDTH = "last_width"
    private val KEY_HEIGHT = "last_height"

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
        settings.mediaPlaybackRequiresUserGesture = false
        
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        
        // Desktop user agent to encourage 4K assets if possible
        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
        
        webView.webChromeClient = WebChromeClient()

        showSetupDialog()
    }

    private fun showSetupDialog() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastUrl = prefs.getString(KEY_LAST_URL, "https://google.com")
        val lastWidth = prefs.getInt(KEY_WIDTH, 3840) // Default Toshiba 4K width
        val lastHeight = prefs.getInt(KEY_HEIGHT, 2160) // Default Toshiba 4K height

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_setup, null)
        val urlInput = dialogView.findViewById<EditText>(R.id.input_url)
        val widthInput = dialogView.findViewById<EditText>(R.id.input_width)
        val heightInput = dialogView.findViewById<EditText>(R.id.input_height)

        urlInput.setText(lastUrl)
        widthInput.setText(lastWidth.toString())
        heightInput.setText(lastHeight.toString())
        
        // Select URL text for easy editing
        urlInput.setSelection(urlInput.text.length)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.enter_url_title))
        builder.setView(dialogView)

        builder.setPositiveButton(getString(R.string.go_button)) { dialog, _ ->
            var url = urlInput.text.toString().trim()
            val widthStr = widthInput.text.toString().trim()
            val heightStr = heightInput.text.toString().trim()

            if (url.isNotEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }

                val width = widthStr.toIntOrNull() ?: 3840
                val height = heightStr.toIntOrNull() ?: 2160
                
                // Save Prefs
                prefs.edit()
                    .putString(KEY_LAST_URL, url)
                    .putInt(KEY_WIDTH, width)
                    .putInt(KEY_HEIGHT, height)
                    .apply()
                
                // Apply Resolution and Load
                resizeWebView(width, height)
                loadUrl(url)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
            dialog.cancel()
            if (webView.url == null) finish()
        }
        
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
        
        urlInput.requestFocus()
    }

    private fun resizeWebView(width: Int, height: Int) {
        // We cannot easily change the physical screen resolution, but we can 
        // force the WebView layout params to match the requested size.
        // If the screen is smaller (e.g. 1080p UI), this will make the WebView huge 
        // and potentially scrollable or scaled down depending on the parent.
        // For 'fitting' the screen, usually we want match_parent.
        // BUT, if the user explicitly wants to simulate 4K on a 1080p UI rendering,
        // we might set fixed size.
        // However, usually "cocok ukuran layar" means "fills the screen".
        // Let's stick to match_parent for safety unless the user *really* wants to force bounds.
        // Ref: Toshiba 43C350NP is 4K.
        
        // If we want to force the content viewport:
        // resizing the view itself won't change the hardware density.
        // But let's try to honor the input by setting LayoutParams if it's different.
        
        val params = webView.layoutParams
        // If values are 0 or standard 4K on a 4K TV, match_parent is best.
        // If we set exact pixels, it might be bigger than the viewroot if the UI is running at 1080p.
        // Let's set it to match_parent to ensure it FITS the Toshiba screen perfectly.
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        webView.layoutParams = params
        
        // NOTE: If the intent strictly was to "emulate" 4K resolution on a lower res display,
        // we might use webView.setInitialScale(...) but standard WebView behavior is best left to match_parent.
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
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Menu")
                .setMessage("Select an option")
                .setPositiveButton("Change Settings") { _, _ -> showSetupDialog() }
                .setNegativeButton("Exit") { _, _ -> super.onBackPressed() }
                .show()
        }
    }
}
