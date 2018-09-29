package com.olly.trakt;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.olly.trakt.Objects.TraktListObject;

import java.util.ArrayList;

public class TraktListAdapter extends RecyclerView.Adapter<TraktListAdapter.ViewHolder> {

    private ArrayList<TraktListObject> traktList;

    public TraktListAdapter(ArrayList<TraktListObject> traktListObjects){
        traktList = traktListObjects;
    }

    @Override
    public void onBindViewHolder(@NonNull TraktListAdapter.ViewHolder holder, int position) {
        TraktListObject trakt = traktList.get(position);
        holder.txtTitle.setText(trakt.episode.title);

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

        return new ViewHolder(itemView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTitle;

        public ViewHolder(View itemView){
            super(itemView);
            txtTitle = itemView.findViewById(R.id.cardText);
        }
    }
}
