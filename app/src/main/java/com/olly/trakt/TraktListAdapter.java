package com.olly.trakt;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DebugUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.olly.trakt.Objects.TMDBObject;
import com.olly.trakt.Objects.ServerCallback;
import com.olly.trakt.Objects.TraktListObject;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TraktListAdapter extends RecyclerView.Adapter<TraktListAdapter.ViewHolder> {

    private ArrayList<TraktListObject> traktList;
    private TraktListObject trakt;
    private TMDBObject tmdb;
    private Context parentContext;
    private String showName;
    public View view;

    public TraktListAdapter(ArrayList<TraktListObject> traktListObjects){
        traktList = traktListObjects;
    }

    @Override
    public void onBindViewHolder(@NonNull TraktListAdapter.ViewHolder holder, int position) {
        trakt = traktList.get(position);
         Log.d("TRAKT", MessageFormat.format("Show: {0} ID: {1}",trakt.show.title,String.valueOf(trakt.show.ids.tmdb)));

        holder.txtShowTitle.setText(trakt.show.title);
        holder.txtEpisodeTitle.setText(trakt.episode.title);
        holder.txtAirTime.setText(MessageFormat.format("{0}", trakt.first_aired.toLocaleString()));

        if(trakt.show.ids.tmdb != 0){
            Map<String, String> params = new HashMap<>();
            String url = MessageFormat.format("https://api.themoviedb.org/3/tv/{0}?api_key={1}", String.valueOf(trakt.show.ids.tmdb), MainActivity.TMDB_KEY);
            Log.d("URL", url);
            TraktManager.getInstance(parentContext).getString(url, params, new ServerCallback() {
                @Override
                public void onSuccess(String result) {
                    tmdb = new Gson().fromJson(result, TMDBObject.class);
                    Picasso.get().load("https://image.tmdb.org/t/p/w500" + tmdb.poster_path).into(holder.imgPoster);
                    holder.imgPoster.setVisibility(View.VISIBLE);
                }
            });
        } else if (trakt.show.ids.tmdb == 0) {
            Picasso.get().load(R.drawable.ic_launcher_background).into(holder.imgPoster);
            holder.imgPoster.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return traktList.size();
    }

    @NonNull
    @Override
    public TraktListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trakt_card, parent, false);

        parentContext = parent.getContext();

        return new ViewHolder(itemView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtShowTitle;
        public TextView txtEpisodeTitle;
        public ImageView imgPoster;
        public ProgressBar progressBar;
        public TextView txtAirTime;
        public CardView cardView;
        public TextView txtDescription;

        public ViewHolder(View itemView){
            super(itemView);
            view = itemView;
            txtShowTitle = itemView.findViewById(R.id.cardShowTitle);
            txtEpisodeTitle = itemView.findViewById(R.id.cardEpisodeTitle);
            imgPoster = itemView.findViewById(R.id.imageView);
            progressBar = itemView.findViewById(R.id.progressBar);
            txtAirTime = itemView.findViewById(R.id.cardEpisodeAirDate);
            cardView = itemView.findViewById(R.id.card_view);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(parentContext, ShowInfoActivity.class);
                    intent.putExtra("showName", trakt.show.title);
                    intent.putExtra("showSlug", trakt.show.ids.slug);
                    intent.putExtra("backdrop_path", tmdb.backdrop_path);
                    parentContext.startActivity(intent);
                }
            });
        }
    }
}
