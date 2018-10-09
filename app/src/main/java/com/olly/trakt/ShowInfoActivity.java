package com.olly.trakt;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.olly.trakt.Objects.LinkListObject;
import com.olly.trakt.Objects.ServerCallback;
import com.olly.trakt.Objects.TMDBObject;
import com.olly.trakt.Objects.TraktExtendedObject;
import com.olly.trakt.Objects.TraktListObject;
import com.olly.trakt.Objects.TraktShow;
import com.olly.trakt.Objects.WatchlistObject;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShowInfoActivity extends AppCompatActivity {

    private TextView txtDescription;
    private TraktExtendedObject trakt;
    private ActionBar actionBar;
    private ImageView toolbarImg;
    private AppBarLayout appBarLayout;
    private RecyclerView showLinkList;
    CollapsingToolbarLayout toolbarLayout;

    private TraktManager traktManager;

    private ShowLinkListAdapter linkListAdapter;

    private String url;

    private ArrayList<LinkListObject> links = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_bar);

        traktManager = TraktManager.getInstance(getApplicationContext());

        Intent intent = getIntent();
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        toolbarLayout = findViewById(R.id.toolbar_layout);
        txtDescription = findViewById(R.id.txtDescription);
        toolbarImg = findViewById(R.id.toolbarImage);
        showLinkList = findViewById(R.id.showList);

        actionBar.setTitle(intent.getStringExtra("showName"));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        Map<String, String> params = new HashMap<>();
        params.put("trakt-api-version", "2");
        params.put("trakt-api-key", MainActivity.CLIENT_ID);
        traktManager.getString(MessageFormat.format("{0}shows/{1}?extended=full", MainActivity.API_URL, intent.getStringExtra("showSlug")), params, new ServerCallback() {
                    @Override
                    public void onSuccess(String result) {
                        trakt = new Gson().fromJson(result, TraktExtendedObject.class);
                        txtDescription.setText(trakt.overview);
                        if(traktManager.watchlist.containsKey(trakt.ids.slug)){
                            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.watching)));
                        }
                    }
                });

        try {
            url = "https://image.tmdb.org/t/p/w50" + intent.getStringExtra("backdrop_path");
            Picasso.get().load("https://image.tmdb.org/t/p/w500" + intent.getStringExtra("backdrop_path")).into(toolbarImg);
        } catch (NullPointerException e){
            Log.d("NULL_POINTER", "There was a null pointer exception." + e.getLocalizedMessage());
            String url = MessageFormat.format("https://api.themoviedb.org/3/tv/{0}?api_key={1}", String.valueOf(trakt.ids.tmdb), MainActivity.TMDB_KEY);
            traktManager.getString(url, params, new ServerCallback() {
                @Override
                public void onSuccess(String result) {
                    try {
                        TMDBObject tmdb = new Gson().fromJson(result, TMDBObject.class);
                        Picasso.get().load("https://image.tmdb.org/t/p/w500" + tmdb.backdrop_path).into(toolbarImg);
                    }catch (IndexOutOfBoundsException e){
                        Log.d("OUT OF BOUNDS", e.getLocalizedMessage());
                    }
                }
            });
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TraktShow traktShow = new TraktShow(trakt.title, trakt.year, trakt.ids);
                String traktShowJson = new Gson().toJson(traktShow);


                String url;
                if(traktManager.watchlist.containsKey(trakt.ids.slug))
                    url = MainActivity.API_URL + "sync/history/remove";
                else
                    url = MainActivity.API_URL + "sync/history";

                Log.d("POSTING", traktShowJson + " | " + url);

                traktManager.postString(url, traktManager.TRAKT_PARAMS, traktShowJson, new ServerCallback() {
                    @Override
                    public void onSuccess(String result) {
                        if(url.contains("/remove"))
                            Toast.makeText(getApplicationContext(), "Removed show.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), "Added show.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        links.add(new LinkListObject(R.mipmap.imdb_fg, "", "IMDB"));

        // Create the adapter and give it the list to follow
        linkListAdapter = new ShowLinkListAdapter(links);
        // more RecyclerView set up
        // Create the layout manager for the recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        // Set the layoutmanager
        showLinkList.setLayoutManager(layoutManager);
        // Set the adapter for the recyclerview
        showLinkList.setAdapter(linkListAdapter);
    }
}
