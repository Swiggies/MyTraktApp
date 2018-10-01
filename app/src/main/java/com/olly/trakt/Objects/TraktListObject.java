package com.olly.trakt.Objects;

import android.util.Log;

import java.util.Date;

public class TraktListObject {

    public Date first_aired;
    public TraktListEpisode episode;
    public TraktListShow show;

    public class TraktListEpisode {
        public int season;
        public int number;
        public String title;
        public TraktID ids;
    }

    public class TraktListShow {
        public String title;
        public int year;
        public TraktID ids;
    }

    public class TraktID {
        public int trakt;
        public String slug;
        public int tvdb;
        public String imdb;
        public int tmdb;
    }


}
