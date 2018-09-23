package com.olly.trakt;

import android.content.Context;
import android.content.SharedPreferences;

public class TraktPreferences {

    private SharedPreferences mPrefs;

    public TraktPreferences(Context context){
       mPrefs = context.getSharedPreferences("com.olly.trakt", Context.MODE_PRIVATE);
    }

    public String getPreference(String key){
        return mPrefs.getString(key, null);
    }

    public void saveString(String key, String value){
        mPrefs.edit().putString(key, value).apply();
    }

}
