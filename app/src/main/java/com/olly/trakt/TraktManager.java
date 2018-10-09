package com.olly.trakt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.olly.trakt.Objects.JsonParser;
import com.olly.trakt.Objects.ServerCallback;
import com.olly.trakt.Objects.TraktExtendedObject;
import com.olly.trakt.Objects.WatchlistObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.olly.trakt.MainActivity.CLIENT_ID;

public class TraktManager {

    public static TraktManager mTraktManager;
    private static Context mCtx;
    private RequestQueue mQueue;
    private JSONObject mResponseObject;
    private int mStatusCode = 0;

    public Map<String, String> TRAKT_PARAMS = new HashMap<>();
    public Map<String, String> CONTENT_TYPE = new HashMap<>();

    public  Map<String, WatchlistObject> watchlist = new HashMap<>();

    public SharedPreferences preferences;

    private TraktManager (Context context){
        mCtx = context;
        mQueue = getRequestQueue();

        preferences = mCtx.getSharedPreferences("com.olly.trakt", Context.MODE_PRIVATE);
        TRAKT_PARAMS.put("Content-type", "application/json");
        TRAKT_PARAMS.put("Authorization", "Bearer " + preferences.getString("access_token",null));
        TRAKT_PARAMS.put("trakt-api-version", "2");
        TRAKT_PARAMS.put("trakt-api-key", MainActivity.CLIENT_ID);
        Log.d("Holy shit am i an idiot", MainActivity.CLIENT_ID);
    }

    public static synchronized TraktManager getInstance(Context context) {
        if (mTraktManager == null) {
            mTraktManager = new TraktManager(context);
        }
        return mTraktManager;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public RequestQueue getRequestQueue(){
        if (mQueue == null){
            mQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mQueue;
    }

    public void getString(String urlString, Map<String, String> headerParams, final ServerCallback callback){
        StringRequest getRequest = new StringRequest(Request.Method.GET, urlString,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // display response
                        //mResponseObject = response;
                        callback.onSuccess(response);
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headerParams;
            }
        };
        mQueue.add(getRequest);
    }

    public int postString(String urlString, Map<String, String> headerParams, String body, final ServerCallback callback){
        try {
            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, urlString, new JSONObject(headerParams),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // response
                            mResponseObject = response;
                            callback.onSuccess(response.toString());
                            Log.d("Response", response.toString());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.d("Error.Response", String.valueOf(error.networkResponse.statusCode) + error.networkResponse.headers.toString());
                        }
                    }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return headerParams;
                }

                @Override
                public byte[] getBody() {
                    return body.getBytes();
                }

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    mStatusCode = response.statusCode;
                    return super.parseNetworkResponse(response);
                }
            };
            mQueue.add(postRequest);
            return mStatusCode;
        }
        catch (Exception e){
            Log.d("EXCEPTION", e.getMessage());
            return 0;
        }
    }

    public void getWatchlist(){
        getString(MainActivity.API_URL + "sync/watched/shows?extended=noseasons", TRAKT_PARAMS, new ServerCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONArray traktJson = new JSONArray(result);
                    for (int i = 0; i < traktJson.length(); i++) {
                        WatchlistObject watchlistObject = new Gson().fromJson(traktJson.getJSONObject(i).toString(), WatchlistObject.class);
                        watchlist.put(watchlistObject.show.ids.slug, watchlistObject);
                        Log.d("WATCH", watchlist.toString());
                    }
                }
                catch (JSONException e){
                    Log.d("JSON",e.getLocalizedMessage());
                }
            }
        });
    }
}


