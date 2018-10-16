package com.olly.trakt;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.olly.trakt.Objects.GetTMDB;
import com.olly.trakt.Objects.RetrofitClientInstance;
import com.olly.trakt.Objects.TMDBObject;
import com.squareup.picasso.Picasso;
import com.uwetrottmann.trakt5.entities.BaseShow;

import java.text.MessageFormat;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllShowsListAdapter extends RecyclerView.Adapter<AllShowsListAdapter.ViewHolder> {

    private ArrayList<BaseShow> traktList;
    private ArrayList<TMDBObject> tmdbList;
    private Context parentContext;

    public AllShowsListAdapter(ArrayList<BaseShow> traktListObjects){
        traktList = traktListObjects;
        tmdbList = new ArrayList<>(traktList.size());
    }

    @Override
    public void onBindViewHolder(@NonNull AllShowsListAdapter.ViewHolder holder, int position) {
        BaseShow trakt = traktList.get(position);
        TMDBObject tmdb = new TMDBObject();
        tmdbList.add(position, tmdb);
        Log.d("TRAKT", MessageFormat.format("Show: {0} ID: {1}",trakt.show.title,String.valueOf(trakt.show.ids.tmdb)));

        holder.txtShowTitle.setText(trakt.show.title);
        BaseShow baseShow = TraktManager.getWatchedList().get(trakt.show.ids.slug);
        if(baseShow != null && baseShow.completed != null)
            holder.txtShowCount.setText(MessageFormat.format("Watched: {0}/{1}", baseShow.completed, baseShow.aired));

        if(trakt.show.ids.tmdb != null) {
            GetTMDB service = RetrofitClientInstance.getRetrofit().create(GetTMDB.class);
            Call<TMDBObject> call = service.tv(trakt.show.ids.tmdb, MainActivity.TMDB_KEY);
            call.enqueue(new Callback<TMDBObject>() {
                @Override
                public void onResponse(Call<TMDBObject> call, Response<TMDBObject> response) {
                    Log.d("RESPONSE", response.body().poster_path);
                    tmdbList.add(position, response.body());
                    Picasso.get().load(Constants.TMDB_URL + response.body().poster_path).into(holder.imgPoster);
                    holder.imgPoster.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(Call<TMDBObject> call, Throwable t) {
                    Picasso.get().load(R.drawable.question_mark_poster).into(holder.imgPoster);
                    holder.imgPoster.setVisibility(View.VISIBLE);
                }
            });
        } else {
            Picasso.get().load(R.drawable.question_mark_poster).into(holder.imgPoster);
            holder.imgPoster.setVisibility(View.VISIBLE);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(parentContext, ShowInfoActivity.class);
                intent.putExtra(Constants.SHOW_NAME, trakt.show.title);
                intent.putExtra(Constants.SHOW_SLUG, trakt.show.ids.slug);
                if(position < tmdbList.size() && tmdbList.get(position) != null)
                    intent.putExtra(Constants.BACKDROP, tmdbList.get(position).backdrop_path);
                parentContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return traktList.size();
    }



    @NonNull
    @Override
    public AllShowsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_all_shows, parent, false);

        parentContext = parent.getContext();

        return new ViewHolder(itemView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtShowTitle;
        public ImageView imgPoster;
        public ProgressBar progressBar;
        public CardView cardView;
        public TextView txtShowCount;

        public ViewHolder(View itemView){
            super(itemView);
            txtShowTitle = itemView.findViewById(R.id.cardShowTitle);
            imgPoster = itemView.findViewById(R.id.imageView);
            progressBar = itemView.findViewById(R.id.progressBar);
            cardView = itemView.findViewById(R.id.card_view);
            txtShowCount = itemView.findViewById(R.id.cardShowCount);
        }
    }
}
