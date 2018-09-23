package com.olly.trakt;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    public final String API_URL = "https://api-staging.trakt.tv/";
    public final String CLIENT_ID = "3b1b8f5edeeea738201dd9a3ae8bb25e294e0b21830552944cc2bc24ac18cd71";

    Trakt trakt;

    private TextView testTextView;
    private WebView webView;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        trakt = new Trakt();

        webView = findViewById(R.id.webView);
        signInButton = findViewById(R.id.button);
        testTextView = findViewById(R.id.TestText);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
        });
        //SignIn();
        //testTextView.setText(trakt.PostString(API_URL + "oauth/authorize?response_type=code&client_id=9b36d8c0db59eff5038aea7a417d73e69aea75b41aac771816d2ef1b3109cc2f&redirect_uri=urn:ietf:wg:oauth:2.0:oob", this));
    }

    public void SignIn(){
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(API_URL + "oauth/authorize?response_type=code&client_id=9b36d8c0db59eff5038aea7a417d73e69aea75b41aac771816d2ef1b3109cc2f&redirect_uri=urn:ietf:wg:oauth:2.0:oob");
    }
}
