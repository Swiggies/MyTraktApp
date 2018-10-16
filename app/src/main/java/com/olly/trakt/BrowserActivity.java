package com.olly.trakt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BrowserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        Intent intent = getIntent();
        WebView webView = findViewById(R.id.browser_WebView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                if(url.startsWith(MainActivity.REDIRECT_URI)){
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(MainActivity.RESULT_STRING, url.substring(22));
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
        webView.loadUrl(intent.getStringExtra(MainActivity.BROWSER_URL));
    }
}
