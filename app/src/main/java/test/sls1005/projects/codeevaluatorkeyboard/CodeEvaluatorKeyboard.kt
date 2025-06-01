package test.sls1005.projects.codeevaluatorkeyboard

import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.text.Html
import android.view.View
import android.view.View.VISIBLE
import android.view.View.INVISIBLE
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebViewDatabase
import android.widget.Button
import test.sls1005.projects.codeevaluatorkeyboard.R

class CodeEvaluatorKeyboard : InputMethodService() {
    override fun onCreateInputView(): View {
        val pageData = Uri.encode("<!DOCTYPE html><html><head></head><body></body></html>")
        val keyboard = layoutInflater.inflate(R.layout.keyboard, null).apply {
            findViewById<WebView>(R.id.js_engine).apply {
                visibility = VISIBLE
                settings.apply {
                    javaScriptEnabled = true
                    blockNetworkImage = true
                    blockNetworkLoads = true
                    builtInZoomControls = false
                    displayZoomControls = false
                    useWideViewPort = true
                    setSupportMultipleWindows(false)
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(v: WebView, req: WebResourceRequest): Boolean {
                        return true
                    }
                }
                loadDataWithBaseURL("data:text/html," + pageData, pageData, "text/html", null, null)
                setNetworkAvailable(false)
                visibility = INVISIBLE
            }.also {
                CookieManager.getInstance().apply {
                    setAcceptCookie(false)
                    setAcceptThirdPartyCookies(it, false)
                }
            }
        }
        val listener = View.OnClickListener { view ->
            when (view.id) {
                R.id.button_html -> run {
                    currentInputConnection.getSelectedText(0).also {
                        if (it?.isNotEmpty() ?: false) {
                            currentInputConnection.commitText(
                                Html.fromHtml(
                                    it.toString(), Html.FROM_HTML_MODE_COMPACT
                                ), 1
                            )
                        }
                    }
                }
                R.id.button_js -> run {
                    currentInputConnection.getSelectedText(0).also {
                        if (it?.isNotEmpty() ?: false) {
                            keyboard.findViewById<WebView>(R.id.js_engine).apply {
                                visibility = VISIBLE
                                evaluateJavascript(it.toString()) { result ->
                                    currentInputConnection.commitText(result, 1)
                                }
                                visibility = INVISIBLE
                            }
                        }
                    }
                }
                R.id.button_change_keyboard -> run {
                    switchKeyboard(this@CodeEvaluatorKeyboard)
                }
            }
        }
        intArrayOf(R.id.button_html, R.id.button_js, R.id.button_change_keyboard).forEach { id ->
            keyboard.findViewById<Button>(id).setOnClickListener(listener)
        }
        return keyboard
    }

    override fun onDestroy() {
        CookieManager.getInstance().removeAllCookies(null)
        WebViewDatabase.getInstance(this@CodeEvaluatorKeyboard).clearHttpAuthUsernamePassword()
        WebView(this@CodeEvaluatorKeyboard).clearCache(true)
        super.onDestroy()
    }
}
