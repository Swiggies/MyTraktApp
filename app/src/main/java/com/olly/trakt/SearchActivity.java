package com.olly.trakt;

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
import android.widget.Toast;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.SearchResult;
import com.uwetrottmann.trakt5.enums.Extended;
import com.uwetrottmann.trakt5.services.Search;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private MenuItem searchItem;
    private android.support.v7.widget.SearchView searchView;
    private ArrayList<SearchResult> searchResults = new ArrayList<>();

    private SearchListAdapter searchListAdapter;
    private RecyclerView searchRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // creation of activity and layout stuff

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        searchRecyclerView = findViewById(R.id.search_view);
        searchListAdapter = new SearchListAdapter(searchResults, getApplicationContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        // Set the layoutmanager
        searchRecyclerView.setLayoutManager(layoutManager);
        // Set the adapter for the recyclerview
        searchRecyclerView.setAdapter(searchListAdapter);
    }

    private void search(String query){
        // clear the search results
        searchResults.clear();

        // call for a search query
        TraktV2 trakt = TraktManager.getTrakt();
        Call<List<SearchResult>> call = trakt.search().textQueryShow(query, "", "", "","", "", "", "", "", "", Extended.FULL, 0, 20);
        call.enqueue(new Callback<List<SearchResult>>() {
            @Override
            public void onResponse(Call<List<SearchResult>> call, Response<List<SearchResult>> response) {
                Log.d("RESPONSE", String.valueOf(response.body().size()));
                if(response.body().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Could not find any results", Toast.LENGTH_LONG).show();
                } else {
                    // if there are results found, add them to the list and notify the data has changed
                    searchResults.addAll(response.body());
                    searchListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<SearchResult>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Could not find any results", Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);

        searchItem = menu.findItem(R.id.search_action_search);
        searchView = (android.support.v7.widget.SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setIconified(false);
        searchView.requestFocus();
        searchView.setMaxWidth( Integer.MAX_VALUE );
        return super.onCreateOptionsMenu(menu);
    }
}
