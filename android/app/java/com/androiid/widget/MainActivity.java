package com.androiid.widget;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends Activity {

    private WebView web;
    private AudioManager audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        audio = (AudioManager) getSystemService(AUDIO_SERVICE);

        try {
            web = new WebView(this);
            web.addJavascriptInterface(new JsBridge(), "Android");
            web.setBackgroundColor(Color.rgb(233, 217, 189));
            web.setOverScrollMode(View.OVER_SCROLL_NEVER);

            WebSettings s = web.getSettings();
            s.setJavaScriptEnabled(true);
            s.setDomStorageEnabled(true);
            s.setAllowFileAccess(true);
            s.setAllowContentAccess(true);
            s.setAllowFileAccessFromFileURLs(true);
            s.setAllowUniversalAccessFromFileURLs(true);
            s.setLoadWithOverviewMode(true);
            s.setUseWideViewPort(true);
            s.setCacheMode(WebSettings.LOAD_NO_CACHE);
            s.setMediaPlaybackRequiresUserGesture(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }

            web.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView v, int code, String desc, String failingUrl) {
                    showFallback(desc);
                }
            });

            setContentView(web);
            web.loadUrl("file:///android_asset/www/index.html");
        } catch (Throwable t) {
            showFallback("WebView unavailable: " + t.getMessage());
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        hideBars();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    /** Safe: the decor view's insets controller is null until attached. */
    private void hideBars() {
        View v = getWindow() == null ? null : getWindow().getDecorView();
        if (v == null || !v.isAttachedToWindow()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController c = v.getWindowInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            v.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    private void showFallback(String message) {
        ViewGroup root = findViewById(android.R.id.content);
        if (root == null) return;
        root.removeAllViews();
        TextView t = new TextView(this);
        t.setText("Could not load the widget.\n" + (message == null ? "" : message));
        t.setTextColor(Color.rgb(79, 64, 48));
        t.setPadding(40, 40, 40, 40);
        root.addView(t);
    }

    /** Called from the page every ~600ms. Call state wins over music. */
    private String currentAudioState() {
        if (audio == null) return "idle";
        int mode = audio.getMode();
        if (mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION) {
            return "call";
        }
        if (audio.isMusicActive()) return "music";
        return "idle";
    }

    public class JsBridge {
        @JavascriptInterface
        public String getState() {
            return currentAudioState();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
