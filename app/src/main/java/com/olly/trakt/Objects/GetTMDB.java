package com.olly.trakt.Objects;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GetTMDB {

    @GET("tv/{id}")
    Call<TMDBObject> tv(
            @Path("id") Integer showId,
            @Query("api_key") String apiKey
    );

}
