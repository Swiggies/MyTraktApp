package com.olly.trakt;

import android.content.Context;
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
import com.olly.trakt.Objects.JsonParser;
import com.olly.trakt.Objects.ServerCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class TraktManager {

    public static TraktManager mTraktManager;
    private static Context mCtx;
    private RequestQueue mQueue;
    private JSONObject mResponseObject;
    private int mStatusCode = 0;

    private TraktManager (Context context){
        mCtx = context;
        mQueue = getRequestQueue();
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

    public int postString(String urlString, Map<String, String> headerParams, final ServerCallback callback){
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
                            Log.d("Error.Response", error.getMessage());
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    return headerParams;
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
}


