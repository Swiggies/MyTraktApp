package com.olly.trakt.Objects;

import android.util.Log;

public class TraktObject {

    String mTraktTitle;

    public TraktObject (String title) {
        mTraktTitle = title;
        Log.d("TRAKT_OBJ", title);
    }

    public String getTitle(){
        return mTraktTitle;
    }

}
