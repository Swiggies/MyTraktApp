package com.olly.trakt;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;


import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.BaseShow;
import com.uwetrottmann.trakt5.enums.Extended;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class TraktManager {

    // a static instance of the TraktV2 object, used throughout the project
    private static TraktV2 trakt;
    // a map of the watched shows of the user, also used throughout the project
    private static Map<String, BaseShow> watchedShows = new HashMap<>();

    // a static method to set the trakt object
    public static void setTrakt(TraktV2 trakt) {
        TraktManager.trakt = trakt;
    }

    // a static method to get the trakt object
    public static TraktV2 getTrakt(){
        return trakt;
    }

    // a static method to get and return the watched list of the user
    public static Map<String, BaseShow> getWatchedList(){
        return watchedShows;
    }


    // a static method to download the watched shows of the user
    public static void getWatchedShows(Context ctx){
        // clear it if its full
        watchedShows.clear();
        Call<List<BaseShow>> call = trakt.sync().watchedShows(Extended.FULL);
        call.enqueue(new Callback<List<BaseShow>>() {
            @Override
            public void onResponse(Call<List<BaseShow>> call, retrofit2.Response<List<BaseShow>> response) { ;
                try {
                    // for each show found, do another download to see whate episdoes the user has watched
                    for (BaseShow show : response.body()) {
                        Call<BaseShow> extraCall = trakt.shows().watchedProgress(show.show.ids.slug, false, false ,Extended.FULLEPISODES);
                        extraCall.enqueue(new Callback<BaseShow>() {
                            @Override
                            public void onResponse(Call<BaseShow> call, retrofit2.Response<BaseShow> response) {
                                try {
                                    // add this to the original show that we downloaded
                                    show.completed = response.body().completed;
                                    show.plays = response.body().plays;
                                    show.aired = response.body().aired;
                                } catch ( NullPointerException e){
                                    Log.d("NULL POINTER", e.getLocalizedMessage());
                                    // let the user know if something went wrong
                                    Toast.makeText(ctx, "Could not get watch count for " + show.show.title, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<BaseShow> call, Throwable t) {
                                // let the user know if something went wrong
                                Toast.makeText(ctx, "Could not get watch count for " + show.show.title, Toast.LENGTH_SHORT).show();
                            }
                        });
                        // after loading extra info for the show, add it to the list
                        watchedShows.put(show.show.ids.slug, show);
                    }
                }catch (NullPointerException e){
                    // let the user know if something went wrong
                    Toast.makeText(ctx, "Could not get watched shows", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BaseShow>> call, Throwable t) {
                // let the user know if something went wrong
                Toast.makeText(ctx, "Could not get watched shows", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


