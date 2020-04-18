package com.cmpe277.lens

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.AlarmClock
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_webview.*

class WebviewActivity : AppCompatActivity() {

    companion object {
        const val TAG = "WebviewActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        val message = intent
        val searchword : String = message.getStringExtra(AlarmClock.EXTRA_MESSAGE).toString()
        println("$TAG Obtained Messages : " + message.getStringExtra(AlarmClock.EXTRA_MESSAGE))

        if(webview != null) {
            val webSettings = webview!!.settings
            webSettings.javaScriptEnabled = true
            webview!!.webChromeClient = WebChromeClient()
            webview!!.webViewClient = WebViewClient()
            webview!!.loadUrl("http://www.google.com/search?q=${searchword}")

            webview!!.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
        }
    }

    override fun onBackPressed() {
        if(webview!!.canGoBack()) {
            webview!!.goBack()
        }
        else {
            super.onBackPressed()
        }
    }
}
