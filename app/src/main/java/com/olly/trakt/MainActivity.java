package com.olly.trakt;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.olly.trakt.Objects.ServerCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    public final String API_URL = "https://api.trakt.tv/";
    public final static String CLIENT_ID = "5cdd2e9c480ee1c879e551fd87359119d58cb17c6721b3780dc27ce5372790f7";
    public final static String CLIENT_SECRET = "6484d929dbaec2741b7a5ec275e0594a35ae07488b8ff2f6a2194098f78dc40c";
    public final static String REDIRECT_URI = "oauth://redirect";
    public static SharedPreferences preferences;

    private TextView testTextView;
    private WebView webView;
    private Button signInButton;
    private JSONObject response;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getApplicationContext().getSharedPreferences("com.olly.trakt", Context.MODE_PRIVATE);
        webView = findViewById(R.id.SignInWebView);

        if(!preferences.contains("access_token")) {
            webView.loadUrl(API_URL + "oauth/authorize?response_type=code&client_id=" + CLIENT_ID + "&redirect_uri=oauth://redirect");

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    Log.d("NEW_PAGE", url);
                    if (url.startsWith(REDIRECT_URI)) {
                        String code = url.substring(22);
                        Log.d("CODE", code);
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("code", code);
                        params.put("client_id", CLIENT_ID);
                        params.put("client_secret", CLIENT_SECRET);
                        params.put("redirect_uri", REDIRECT_URI);
                        params.put("grant_type", "authorization_code");
                        int statusCode = TraktManager.getInstance(getApplicationContext()).postString(API_URL + "oauth/token", params, new ServerCallback() {
                            @Override
                            public void onSuccess(String result) {
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    preferences.edit().putString("access_token", jsonObject.getString("access_token")).apply();
                                    preferences.edit().putString("refresh_token", jsonObject.getString("refresh_token")).apply();
                                } catch (JSONException e) {
                                    Log.d("JSON", e.getLocalizedMessage());
                                }
                            }
                        });
                    }
                }
            });
        }

        Log.d("CHECKING KEY", preferences.getString("access_token", null));

        Map<String, String> params = new HashMap<String, String>();
        params.put("Content-Type", "application/json");
        params.put("Authorization", "Bearer " + preferences.getString("access_token", null));
        params.put("trakt-api-version", "2");
        params.put("trakt-api-key", CLIENT_ID);
        TraktManager.getInstance(getApplicationContext()).getString(API_URL + "calendars/my/shows", params, new ServerCallback() {
            @Override
            public void onSuccess(String result) {
                    Log.d("Result", result);
            }
        });
    }
}
