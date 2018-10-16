package com.olly.trakt;

import android.content.Context;
import android.media.Image;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.uwetrottmann.trakt5.entities.BaseEpisode;
import com.uwetrottmann.trakt5.entities.BaseSeason;
import com.uwetrottmann.trakt5.entities.Season;
import com.uwetrottmann.trakt5.entities.Show;
import com.uwetrottmann.trakt5.entities.ShowIds;
import com.uwetrottmann.trakt5.entities.SyncEpisode;
import com.uwetrottmann.trakt5.entities.SyncItems;
import com.uwetrottmann.trakt5.entities.SyncResponse;
import com.uwetrottmann.trakt5.entities.SyncSeason;
import com.uwetrottmann.trakt5.entities.SyncShow;

import java.text.MessageFormat;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowSeasonListAdapter extends RecyclerView.Adapter<ShowSeasonListAdapter.ViewHolder> {

    private ArrayList<BaseSeason> mSeason;
    private Context parentContext;
    private ShowIds showIds;
    private int showPos;

    public ShowSeasonListAdapter(ArrayList<BaseSeason> searchResults, Context context, ShowIds ids){
        mSeason = searchResults;
        parentContext = context;
        showIds = ids;
    }

    @Override
    public void onBindViewHolder(@NonNull ShowSeasonListAdapter.ViewHolder holder, int position) {
        BaseSeason season = mSeason.get(position);

        holder.txtTitle.setText("Season " + (position + 1));

        holder.seasonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        if(TraktManager.getWatchedList().containsKey(showIds.slug)){
            for (int i = 0; i < TraktManager.getWatchedList().get(showIds.slug).seasons.size(); i++) {
                if(TraktManager.getWatchedList().get(showIds.slug).seasons.get(i).number.equals(season.number)){
                    showPos = i;
                    holder.seasonImg.setImageResource(R.drawable.ic_check);
                    break;
                } else {
                    holder.seasonImg.setImageResource(R.drawable.ic_add);
                }
            }
        }

        holder.seasonImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SyncItems items = new SyncItems();
                SyncShow syncShow = new SyncShow();
                syncShow.ids = ShowIds.tvdb(showIds.tvdb);
                SyncSeason syncSeason = new SyncSeason();
                syncSeason.number = position + 1;
                syncSeason.episodes = new ArrayList<>();
                for (int i = 0; i < season.episodes.size(); i++) {
                    SyncEpisode e = new SyncEpisode();
                    e.number = season.episodes.get(i).number;
                    syncSeason.episodes.add(e);
                }

                syncShow.seasons(syncSeason);
                items.shows(syncShow);
                // add the show to a new list and if the user is watching the show, remove it
                // if they aren't watching it, add it to their watched list


                // if the watch list contains the show id
                if(TraktManager.getWatchedList().containsKey(showIds.slug)){
                    boolean notFound = true;
                    // loop through the watched seasons of that show
                    for (int i = 0; i < TraktManager.getWatchedList().get(showIds.slug).seasons.size(); i++) {
                        // if there is a match, delete it from the watched list
                        Log.d("SEASON", MessageFormat.format("Watch: {0} | Season: {1}", TraktManager.getWatchedList().get(showIds.slug).seasons.get(i).number,season.number));
                        if(TraktManager.getWatchedList().get(showIds.slug).seasons.get(i).number.equals(season.number)){
                            Call<SyncResponse> response = TraktManager.getTrakt().sync().deleteItemsFromWatchedHistory(items);
                            removeSeason(response, holder);
                            notFound = false;
                            break;
                        }
                    }
                    // else, add it
                    if(notFound) {
                        Call<SyncResponse> response = TraktManager.getTrakt().sync().addItemsToWatchedHistory(items);
                        addSeason(response, holder);
                    }
                } else {
                    // if the show is not in the watchlist, add the season
                    Call<SyncResponse> response = TraktManager.getTrakt().sync().addItemsToWatchedHistory(items);
                    addSeason(response, holder);
                }
            }
        });
    }

    // call for the season to be added and change layouts and update data accordingly
    private void addSeason(Call<SyncResponse> responseCall, ViewHolder holder){
        responseCall.enqueue(new Callback<SyncResponse>() {
            @Override
            public void onResponse(Call<SyncResponse> call, Response<SyncResponse> response) {
                Toast.makeText(parentContext, "Added season", Toast.LENGTH_SHORT).show();
                holder.seasonImg.setImageResource(R.drawable.ic_check);
                TraktManager.getWatchedShows(parentContext);
            }

            @Override
            public void onFailure(Call<SyncResponse> call, Throwable t) {
                Toast.makeText(parentContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // call for the season to be removed and change layouts and update data accordingly
    private void removeSeason(Call<SyncResponse> responseCall, ViewHolder holder){
        responseCall.enqueue(new Callback<SyncResponse>() {
            @Override
            public void onResponse(Call<SyncResponse> call, Response<SyncResponse> response) {
                Toast.makeText(parentContext, "Removed season", Toast.LENGTH_SHORT).show();
                holder.seasonImg.setImageResource(R.drawable.ic_add);
                TraktManager.getWatchedShows(parentContext);
            }

            @Override
            public void onFailure(Call<SyncResponse> call, Throwable t) {
                Toast.makeText(parentContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSeason.size();
    }

    @NonNull
    @Override
    public ShowSeasonListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_season, parent, false);

        return new ShowSeasonListAdapter.ViewHolder(itemView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTitle;
        public ImageView seasonImg;
        public RelativeLayout seasonLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.season_text);
            seasonLayout = itemView.findViewById(R.id.season_layout);
            seasonImg = itemView.findViewById(R.id.season_button);
        }
    }
}
