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
import com.uwetrottmann.trakt5.entities.CalendarShowEntry;

import org.threeten.bp.format.DateTimeFormatter;

import java.text.MessageFormat;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarListAdapter extends RecyclerView.Adapter<CalendarListAdapter.ViewHolder> {

    private ArrayList<CalendarShowEntry> traktList;
    private ArrayList<TMDBObject> tmdbList;
    private Context parentContext;

    public CalendarListAdapter(ArrayList<CalendarShowEntry> traktListObjects){
        // traktlist = the list of trakt objects
        // tmdblist = the a list mirroring the trakt list, holding images
        traktList = traktListObjects;
        tmdbList = new ArrayList<>(traktList.size());
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarListAdapter.ViewHolder holder, int position) {
        // when the viewholder is bound get the calendar entry from the list
        CalendarShowEntry trakt = traktList.get(position);
        TMDBObject tmdb = new TMDBObject();
        tmdbList.add(position, tmdb);
        Log.d("TRAKT", MessageFormat.format("Show: {0} ID: {1}",trakt.show.title,String.valueOf(trakt.show.ids.tmdb)));

        // viewholder set up
        holder.txtShowTitle.setText(trakt.show.title);
        holder.txtEpisodeTitle.setText(trakt.episode.title);
        holder.txtAirTime.setText(MessageFormat.format("{0}", trakt.first_aired.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM @ hh:mm a"))));
        holder.txtEpisodeNum.setText(MessageFormat.format("Episode {0} | Season {1}", trakt.episode.number, trakt.episode.season));
        BaseShow baseShow = TraktManager.getWatchedList().get(trakt.show.ids.slug);

        // if the show has
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
    public CalendarListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar, parent, false);

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
        public TextView txtEpisodeNum;
        public TextView txtShowCount;

        public ViewHolder(View itemView){
            super(itemView);
            txtShowTitle = itemView.findViewById(R.id.cardShowTitle);
            txtEpisodeTitle = itemView.findViewById(R.id.cardEpisodeTitle);
            imgPoster = itemView.findViewById(R.id.imageView);
            progressBar = itemView.findViewById(R.id.progressBar);
            txtAirTime = itemView.findViewById(R.id.cardEpisodeAirDate);
            cardView = itemView.findViewById(R.id.card_view);
            txtEpisodeNum = itemView.findViewById(R.id.cardEpisodeNumber);
            txtShowCount = itemView.findViewById(R.id.cardShowCount);
        }
    }
}
