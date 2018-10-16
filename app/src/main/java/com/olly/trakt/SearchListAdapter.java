package com.olly.trakt;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uwetrottmann.trakt5.entities.SearchResult;

import java.util.ArrayList;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ViewHolder> {

    private ArrayList<SearchResult> mSearchResults;
    private Context parentContext;

    public SearchListAdapter(ArrayList<SearchResult> searchResults, Context context){
        mSearchResults = searchResults;
        parentContext = context;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchListAdapter.ViewHolder holder, int position) {
        SearchResult searchResult = mSearchResults.get(position);

        Log.d("POSTION", searchResult.show.title);

        holder.txtTitle.setText(searchResult.show.title);

        holder.searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(parentContext, ShowInfoActivity.class);
                    intent.putExtra(Constants.SHOW_NAME, searchResult.show.title);
                    intent.putExtra(Constants.SHOW_SLUG, searchResult.show.ids.slug);
//                    if(position < tmdbList.size() && tmdbList.get(position) != null)
//                        intent.putExtra("backdrop_path", tmdbList.get(position).backdrop_path);
                    parentContext.startActivity(intent);
                }
        });
    }

    @Override
    public int getItemCount() {
        return mSearchResults.size();
    }

    @NonNull
    @Override
    public SearchListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search, parent, false);

        return new ViewHolder(itemView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTitle;
        public RelativeLayout searchLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.search_text);
            searchLayout = itemView.findViewById(R.id.search_layout);
        }
    }
}
