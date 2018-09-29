package com.olly.trakt.Objects;

import android.util.Log;

public class TraktListObject {

    public String first_aired;
    public TraktListEpisode episode;
    public TraktListShow show;

    public class TraktListEpisode {
        public int season;
        public int number;
        public String title;
    }

    public class TraktListShow {
        public String title;
        public int year;
    }


}
