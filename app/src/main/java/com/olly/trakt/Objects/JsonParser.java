package com.olly.trakt.Objects;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JsonParser {

    public JSONObject ParseJson(String json) throws JSONException {
        Map<String, String> jsonMap = new HashMap<String, String>();

        JSONObject jsonObject = new JSONObject(json);
        return jsonObject;
    }

}
