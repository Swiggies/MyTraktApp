package com.olly.trakt;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.olly.trakt.Objects.ServerCallback;
import com.olly.trakt.Objects.TraktListObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Constants used throughout project
    // API URL is the URL for the API that will be communicated with
    public final static String API_URL = "https://api.trakt.tv/";
    // Client ID is the ID that was given by Trakt that will represent this app
    public final static String CLIENT_ID = "5cdd2e9c480ee1c879e551fd87359119d58cb17c6721b3780dc27ce5372790f7";
    // Client Secret is that secret auth code that was given to represent this app
    public final static String CLIENT_SECRET = "6484d929dbaec2741b7a5ec275e0594a35ae07488b8ff2f6a2194098f78dc40c";
    // The redirect URI is where the Trakt API will redirect to. Since this is a native app this is an arbitrary address
    public final static String REDIRECT_URI = "oauth://redirect";

    public final static String TMDB_KEY = "e5b0f61beeccd1f8940012f58a5928b2";
    // This is where basic settings such as the client code (given when authenticated by Trakt) will be stored
    public static SharedPreferences preferences;

    //The WebView that displays the authentication process
    private WebView webView;

    // RecycleView stuff here
    // traktList is the list of Trakt Objects displayed in the RecyclerView
    ArrayList<TraktListObject> traktList = new ArrayList<>();
    // A reference to the RecycerView
    private RecyclerView mRecyclerView;
    // A reference to the adapter class that handles adapter functions for the RecyclerView
    private TraktListAdapter mAdapter;

    // Create all the things and set the Activity up for usage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get the shared preferences for this app
        preferences = getApplicationContext().getSharedPreferences("com.olly.trakt", Context.MODE_PRIVATE);
        // find the webview
        webView = findViewById(R.id.SignInWebView);

        // find the recycler view and store the reference
        mRecyclerView = findViewById(R.id.RecyclerViewMain);
        // Create the adapter and give it the list to follow
        mAdapter = new TraktListAdapter(traktList);

        // more RecyclerView set up
        // Create the layout manager for the recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        // Set the layoutmanager
        mRecyclerView.setLayoutManager(layoutManager);
        // Set the adapter for the recyclerview
        mRecyclerView.setAdapter(mAdapter);

        // if the access token is not found in the preferences
        if(!preferences.contains("access_token")) {
            // the webview will load the authentication page
            webView.loadUrl(API_URL + "oauth/authorize?response_type=code&client_id=" + CLIENT_ID + "&redirect_uri=oauth://redirect");

            // This sets up the webview to do things when a new page is loaded
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    Log.d("NEW_PAGE", url);
                    // if the URL of the new loaded page is the same as REDIRECT_URI
                    if (url.startsWith(REDIRECT_URI)) {
                        // retrieve the code from the end of the URI
                        String code = url.substring(22);
                        Log.d("CODE", code);

                        // Set up for a POST request using the code we received and other parameters
                        Map<String, String> params = new HashMap<String, String>();
                        // code = the code we received from Trakt
                        params.put("code", code);
                        // CLIENT_ID used to represent this app
                        params.put("client_id", CLIENT_ID);
                        // CLIENT_SECRET another code checked to see if this is this app
                        params.put("client_secret", CLIENT_SECRET);
                        // The Redirect URI used from before
                        params.put("redirect_uri", REDIRECT_URI);
                        // The type of code we want to receive back from Trakt
                        params.put("grant_type", "authorization_code");
                        // This is custom designed method to send a POST request to a server.
                        // Using an Interface called "ServerCallback" I implemented a way for the POST to say when it has finished its job
                        int statusCode = TraktManager.getInstance(getApplicationContext()).postString(API_URL + "oauth/token", params, new ServerCallback() {
                            @Override
                            // When the POST request has finished
                            public void onSuccess(String result) {
                                try {
                                    // Get the result string and convert it to a JSON Object
                                    JSONObject jsonObject = new JSONObject(result);
                                    // retrieve the access token from the string and store it in the app preferences
                                    preferences.edit().putString("access_token", jsonObject.getString("access_token")).apply();
                                    // retrieve the refresh token from the string and store it in the app preferences
                                    preferences.edit().putString("refresh_token", jsonObject.getString("refresh_token")).apply();
                                } catch (JSONException e) {
                                    // If any of the json tasks fail, print an error
                                    Log.d("JSON", e.getLocalizedMessage());
                                }
                            }
                        });
                    }
                }
            });
        }

        Log.d("CHECKING KEY", preferences.getString("access_token", null));
        // If the above finished successfully then hide WebView
        webView.setVisibility(View.GONE);

        // Set up a GET request to get the users upcoming/current shows
        Map<String, String> params = new HashMap<String, String>();
        params.put("Content-Type", "application/json");
        params.put("Authorization", "Bearer " + preferences.getString("access_token", null));
        params.put("trakt-api-version", "2");
        params.put("trakt-api-key", CLIENT_ID);
        // Send and waits for a response from Trakt
        TraktManager.getInstance(getApplicationContext()).getString(API_URL + "calendars/my/shows", params, new ServerCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d("Result", result);
                try {
                    // Conver the string from the reply and convert it to a JSON array
                    JSONArray traktJson = new JSONArray(result);
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    // Iterate through the array and create objects and add them to the RecyclerView array
                    for (int i = 0; i < traktJson.length(); i++) {
                        //TraktListObject traktObj = new TraktListObject(traktJson.getJSONObject(i).getJSONObject("show").getString("title"));
                        TraktListObject traktObj = gson.fromJson(traktJson.getJSONObject(i).toString(), TraktListObject.class);
                        traktList.add(traktObj);
                    }
                    // notify the adapter that the dataset changed
                    mAdapter.notifyDataSetChanged();
                }catch (JSONException e){
                    Log.e("JSON_EXCEPTION", e.getLocalizedMessage());
                }
            }
        });
    }
}
