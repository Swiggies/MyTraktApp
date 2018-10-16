package com.olly.trakt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import com.uwetrottmann.trakt5.entities.BaseShow;
import com.uwetrottmann.trakt5.entities.CalendarShowEntry;
import com.uwetrottmann.trakt5.services.Calendars;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // Constants used throughout project
    // API URL is the URL for the API that will be communicated with
    public final static String API_URL = "https://api.trakt.tv/";
    // Client ID is the ID that was given by Trakt that will represent this app
    public final static String CLIENT_ID = "5cdd2e9c480ee1c879e551fd87359119d58cb17c6721b3780dc27ce5372790f7";
    // Client Secret is that secret auth code that was given to represent this app
    public final static String CLIENT_SECRET = "6484d929dbaec2741b7a5ec275e0594a35ae07488b8ff2f6a2194098f78dc40c";
    // The redirect URI is where the Trakt API will redirect to. Since this is a native app this is an arbitrary address
    public final static String REDIRECT_URI = "oauth://redirect";

    public final static String ACCESS_TOKEN = "access_token";
    public final static String REFRESH_TOKEN = "refresh_token";
    public final static String RESULT_STRING = "result";
    public final static String BROWSER_URL = "browser_URL";

    public final static String TMDB_KEY = "e5b0f61beeccd1f8940012f58a5928b2";
    // This is where basic settings such as the client code (given when authenticated by Trakt) will be stored
    private SharedPreferences preferences;

    // reference to the progress bar
    private ProgressBar progressBar;

    // RecycleView stuff here
    // a list of the calendar objects used to display the calendar
    ArrayList<CalendarShowEntry> traktListCalendar = new ArrayList<>();

    // list of all the shows the user has watched
    ArrayList<BaseShow> traktListAll = new ArrayList<>();
    // A reference to the RecycerView
    private RecyclerView recyclerViewCalendar;
    // A reference to the adapter class that handles adapter functions for the RecyclerView
    private CalendarListAdapter adapterCalendar;

    // A reference to the RecycerView
    private RecyclerView recyclerViewAll;
    // A reference to the adapter class that handles adapter functions for the RecyclerView
    private AllShowsListAdapter adapterAll;

    private Spinner spinnerShows;

    // Create all the things and set the Activity up for usage
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // get the shared preferences for this app
        preferences = getApplicationContext().getSharedPreferences("com.olly.trakt", Context.MODE_PRIVATE);

        TraktManager.setTrakt(new TraktV2(CLIENT_ID));

        // if an access token does not exist, start the authentication process
        if(!preferences.contains(ACCESS_TOKEN)){
            Intent intent = new Intent(getApplicationContext(), BrowserActivity.class);
            // start the browseractivity class and put the link used for the OAuth process in the intent
            intent.putExtra(BROWSER_URL,API_URL + "oauth/authorize?response_type=code&client_id=" + CLIENT_ID + "&redirect_uri= " + REDIRECT_URI);
            startActivityForResult(intent, 200);
        }

        progressBar = findViewById(R.id.main_progress_bar);

        // find the recycler view and store the reference
        recyclerViewCalendar = findViewById(R.id.RecyclerViewCalendar);
        // Create the adapter and give it the list to follow
        adapterCalendar = new CalendarListAdapter(traktListCalendar);
        // more RecyclerView set up
        // Create the layout manager for the recyclerview
        RecyclerView.LayoutManager layoutManagerCal = new LinearLayoutManager(getApplicationContext());
        // Set the layoutmanager
        recyclerViewCalendar.setLayoutManager(layoutManagerCal);
        // Set the adapter for the recyclerview
        recyclerViewCalendar.setAdapter(adapterCalendar);

        // find the recycler view and store the reference
        recyclerViewAll = findViewById(R.id.RecyclerViewAllShows);
        // Create the adapter and give it the list to follow
        adapterAll = new AllShowsListAdapter(traktListAll);
        // more RecyclerView set up
        // Create the layout manager for the recyclerview
        RecyclerView.LayoutManager layoutManagerAll = new LinearLayoutManager(getApplicationContext());
        // Set the layoutmanager
        recyclerViewAll.setLayoutManager(layoutManagerAll);
        // Set the adapter for the recyclerview
        recyclerViewAll.setAdapter(adapterAll);

        // the spinner in the toolbar
        spinnerShows = findViewById(R.id.spinnerShows);
        // if an item in the spinner is selected
        spinnerShows.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                switch(pos){
                    case 0: // calendar is selected
                        getMyCalendar(7); // get calendar
                        break;
                    case 1: // all shows is selected
                        getMyShows(); // get all shows
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // debugging
        Log.d("CHECKING KEY", preferences.getString(ACCESS_TOKEN, null));

        // set up the rest of the app
        setUp();
    }

    private void setUp(){
        // set the trakt instance up and define it
        TraktManager.setTrakt(new TraktV2(CLIENT_ID));
        // feed the access token to the new trakt instance
        TraktManager.getTrakt().accessToken(preferences.getString(ACCESS_TOKEN, null));
        // start loading the shows the user has wathced in the background
        TraktManager.getWatchedShows(getApplicationContext());
        // get the calendar
        getMyCalendar(7);
    }

    // this is used for authentication when the browser has finished and send a result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 200){ // the request code for the browseractivity
            if(resultCode == Activity.RESULT_OK){ // if everything is okay
                String code = data.getStringExtra(RESULT_STRING); // extract the string from the intent
                // exchange the code from the url for an access token
                Call<AccessToken> call = TraktManager.getTrakt().authentication().exchangeCodeForAccessToken("authorization_code", code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);
                // call for method asyncrhonously
                call.enqueue(new retrofit2.Callback<AccessToken>() {
                    @Override // if the call gets a response
                    public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                        Log.d("RESPONSE", response.body().access_token);
                        // get the acess token and store it in preferences
                        preferences.edit().putString(ACCESS_TOKEN, response.body().access_token).apply();
                        // retrieve the refresh token from the string and store it in the app preferences
                        preferences.edit().putString(REFRESH_TOKEN, response.body().refresh_token).apply();

                        Toast.makeText(getApplicationContext(), "Sucessfully authenticated", Toast.LENGTH_SHORT).show();
                        // if evertything is okay, set up the rest of the app
                        setUp();
                    }

                    @Override
                    public void onFailure(Call<AccessToken> call, Throwable t) {
                        // somethign went wrong, let the user know
                        Toast.makeText(getApplicationContext(), "Authentication failure", Toast.LENGTH_SHORT).show();
                    }
                });
                //Log.d("RESPONSE", response.body().access_token);
            }
        }
    }


    // get and display my upcoming shows
    private void getMyCalendar(int days) {

        progressBar.setVisibility(View.VISIBLE);
        // hide the calendar recyclerview and show a progress bar so the user knows there is activity
        recyclerViewCalendar.setVisibility(View.GONE);
        // hide the all shows view if it's showing
        recyclerViewAll.setVisibility(View.GONE);

        // get the date
        Calendars calendar = TraktManager.getTrakt().calendars();
        // format the date in a specific way
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Log.d("DATE", date);

        // call for the calendar from Trakt
        Call<List<CalendarShowEntry>> call = calendar.myShows(date ,days);
        // do the call asynchronously
        call.enqueue(new retrofit2.Callback<List<CalendarShowEntry>>() {
            @Override
            public void onResponse(Call<List<CalendarShowEntry>> call, Response<List<CalendarShowEntry>> response) {
                // if the calendar list is not empty, clear it
                if (!traktListCalendar.isEmpty())
                    traktListCalendar.clear();

                // add all the shows from the response to the calendar list
                traktListCalendar.addAll(response.body());
                // notify the adapter the list has changed
                adapterCalendar.notifyDataSetChanged();
                // hide the progress bar and show the recycler view
                progressBar.setVisibility(View.GONE);
                recyclerViewCalendar.setVisibility(View.VISIBLE);
            }

            // somethign went wrong, let the user know
            @Override
            public void onFailure(Call<List<CalendarShowEntry>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Could not get calendar", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // most of this is done in the background by the Trakt Manager so it's mostly a matter of show/hiding things
    private void getMyShows() {

        // get all the shows
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewCalendar.setVisibility(View.GONE);
        traktListAll.clear();

        // add the shows from the TraktManager watchedlist to the MainAcitivty list
        traktListAll.addAll(TraktManager.getWatchedList().values());
        adapterAll.notifyDataSetChanged();

        recyclerViewAll.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_action_refresh:
                // refresh different views depending on which recycler view is currently shows
                    switch(spinnerShows.getSelectedItemPosition()){
                        case 0: // if the calendar is shown
                            getMyCalendar(7);
                            break;
                        case 1: // if all shows are shown
                            getMyShows();
                            break;
                    }

                break;
                    // start the search activity
            case R.id.main_action_search:
                    Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                    startActivity(intent);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
