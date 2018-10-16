package com.olly.trakt;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.olly.trakt.Objects.GetTMDB;
import com.olly.trakt.Objects.LinkListObject;
import com.olly.trakt.Objects.RetrofitClientInstance;
import com.olly.trakt.Objects.TMDBObject;
import com.squareup.picasso.Picasso;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.BaseSeason;
import com.uwetrottmann.trakt5.entities.BaseShow;
import com.uwetrottmann.trakt5.entities.Season;
import com.uwetrottmann.trakt5.entities.Show;
import com.uwetrottmann.trakt5.entities.ShowIds;
import com.uwetrottmann.trakt5.entities.SyncItems;
import com.uwetrottmann.trakt5.entities.SyncResponse;
import com.uwetrottmann.trakt5.entities.SyncShow;
import com.uwetrottmann.trakt5.enums.Extended;
import com.uwetrottmann.trakt5.services.Seasons;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowInfoActivity extends AppCompatActivity {


    // all the layout stuff
    private TextView txtDescription;
    private ActionBar actionBar;
    private ImageView toolbarImg;
    private AppBarLayout appBarLayout;
    private RecyclerView showLinkList;
    private RecyclerView seasonList;
    CollapsingToolbarLayout toolbarLayout;

    // a reference to the static trakt instance
    private TraktV2 trakt;
    // a reference to the show being displayed
    private Show show;

    // adapter for the link list
    private ShowLinkListAdapter linkListAdapter;
    // list for all the links shon
    private ArrayList<LinkListObject> links = new ArrayList<>();

    private ArrayList<BaseSeason> seasons = new ArrayList<>();
    private ShowSeasonListAdapter seasonListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_bar);
        // get the static Trakt from TraktManager
        trakt = TraktManager.getTrakt();

        // get the intent from the previous activity
        Intent intent = getIntent();
        setSupportActionBar(toolbar);

        // more layout stuff
        actionBar = getSupportActionBar();
        toolbarLayout = findViewById(R.id.toolbar_layout);
        txtDescription = findViewById(R.id.txtDescription);
        toolbarImg = findViewById(R.id.toolbarImage);
        showLinkList = findViewById(R.id.showLinks);
        seasonList = findViewById(R.id.showSeasons);

        // set the title from the intent
        actionBar.setTitle(intent.getStringExtra(Constants.SHOW_NAME));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);




        // get more stuff about the show using information from the last activity
        Call<Show> traktCall = trakt.shows().summary(intent.getStringExtra(Constants.SHOW_SLUG), Extended.FULL);
        // do it asynchronously
        traktCall.enqueue(new Callback<Show>() {
            @Override
            public void onResponse(Call<Show> call, Response<Show> response) {
                // set the current show to the response
                show = response.body();
                // set the description of the show
                txtDescription.setText(show.overview);

                // Create the adapter and give it the list to follow
                seasonListAdapter = new ShowSeasonListAdapter(seasons, getApplicationContext(), show.ids);
                // more RecyclerView set up
                // Create the layout manager for the recyclerview
                RecyclerView.LayoutManager seasonLayoutManager = new LinearLayoutManager(getApplicationContext());
                // Set the layoutmanager
                seasonList.setLayoutManager(seasonLayoutManager);
                // Set the adapter for the recyclerview
                seasonList.setAdapter(seasonListAdapter);

                Call<BaseShow> seasonsCall = trakt.shows().watchedProgress(intent.getStringExtra(Constants.SHOW_SLUG), false, false, Extended.FULL);
                seasonsCall.enqueue(new Callback<BaseShow>() {
                    @Override
                    public void onResponse(Call<BaseShow> call, Response<BaseShow> response) {
                        seasons.addAll(response.body().seasons);
                        Log.d("EPISODE", String.valueOf(response.body().seasons.get(0).episodes.get(0).number));
                        seasonListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<BaseShow> call, Throwable t) {

                    }
                });

                // if the show is already being watched then allow the user to remove it
                if(TraktManager.getWatchedList().containsKey(show.ids.slug)){
                    fab.setImageResource(R.drawable.ic_check);
                }

                Log.d("LINK", Constants.IMDB_URL + show.ids.imdb);
                // create the IMDB link list object and add it to the list
                links.add(new LinkListObject(R.mipmap.imdb_fg, Constants.IMDB_URL + show.ids.imdb, "IMDB"));

                // Create the adapter and give it the list to follow
                linkListAdapter = new ShowLinkListAdapter(getApplicationContext(), links);
                // more RecyclerView set up
                // Create the layout manager for the recyclerview
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                // Set the layoutmanager
                showLinkList.setLayoutManager(layoutManager);
                // Set the adapter for the recyclerview
                showLinkList.setAdapter(linkListAdapter);

                // if the backdrop does not exist get a new one
                if(intent.getStringExtra(Constants.BACKDROP) == null) {
                    // create the TMDB service
                    GetTMDB service = RetrofitClientInstance.getRetrofit().create(GetTMDB.class);
                    // create the call
                    Call<TMDBObject> tmdbCall = service.tv(show.ids.tmdb, MainActivity.TMDB_KEY);
                    // do the call asynchronously
                    tmdbCall.enqueue(new Callback<TMDBObject>() {
                        @Override
                        public void onResponse(Call<TMDBObject> call, Response<TMDBObject> response) {
                            Log.d("RESPONSE", response.body().poster_path);
                            // if the image is found successfully, load it
                            Picasso.get().load(Constants.TMDB_URL + response.body().backdrop_path).fit().into(toolbarImg);
                        }

                        @Override
                        public void onFailure(Call<TMDBObject> call, Throwable t) {
                            // if its not found, load a placeholder
                            Picasso.get().load(R.drawable.question_mark_backdrop).fit().into(toolbarImg);
                        }
                    });
                } else {
                    if (show.ids.tmdb != null) {
                        // if the backdrop path exists and the tmdb id is not null, load the backdrop from the path in the intent
                        Picasso.get().load(Constants.TMDB_URL + intent.getStringExtra(Constants.BACKDROP)).fit().into(toolbarImg);
                    } else {
                        // if all else fails, load a placeholder
                        Picasso.get().load(R.drawable.question_mark_backdrop).fit().into(toolbarImg);
                    }
                }
            }

            // if something goes wrong while trying to load the data, let the user know
            @Override
            public void onFailure(Call<Show> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed to retrieve info", Toast.LENGTH_SHORT);
            }
        });

        // if the user clicks on the FAB
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SyncShow syncShow = new SyncShow();
                syncShow.ids = ShowIds.tvdb(show.ids.tvdb);

                // add the show to a new list and if the user is watching the show, remove it
                // if they aren't watching it, add it to their watched list
                SyncItems items = new SyncItems().shows(syncShow);
                if(TraktManager.getWatchedList().containsKey(show.ids.slug)){
                    Call<SyncResponse> response = trakt.sync().deleteItemsFromWatchedHistory(items);
                    response.enqueue(new Callback<SyncResponse>() {
                        @Override
                        public void onResponse(Call<SyncResponse> call, Response<SyncResponse> response) {
                            Toast.makeText(getApplicationContext(), "Removed show", Toast.LENGTH_SHORT).show();
                            fab.setImageResource(R.drawable.ic_add);
                            TraktManager.getWatchedShows(getApplicationContext());
                        }

                        @Override
                        public void onFailure(Call<SyncResponse> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Call<SyncResponse> response = trakt.sync().addItemsToWatchedHistory(items);
                    response.enqueue(new Callback<SyncResponse>() {
                        @Override
                        public void onResponse(Call<SyncResponse> call, Response<SyncResponse> response) {
                            Toast.makeText(getApplicationContext(), "Added show", Toast.LENGTH_SHORT).show();
                            fab.setImageResource(R.drawable.ic_check);
                            TraktManager.getWatchedShows(getApplicationContext());
                        }

                        @Override
                        public void onFailure(Call<SyncResponse> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
