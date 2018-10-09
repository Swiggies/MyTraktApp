package com.olly.trakt.Objects;

import android.util.Log;

import java.util.Date;

public class TraktListObject {

    public Date first_aired;
    public TraktListEpisode episode;
    public TraktShow show;

    public class TraktListEpisode {
        public int season;
        public int number;
        public String title;
        public TraktID ids;
    }

}
