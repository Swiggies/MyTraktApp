package com.olly.trakt;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.olly.trakt.Objects.LinkListObject;
import com.olly.trakt.R;

import java.util.ArrayList;

public class ShowLinkListAdapter extends RecyclerView.Adapter<ShowLinkListAdapter.ViewHolder> {

    private ArrayList<LinkListObject> mLinkListObjects;

    public ShowLinkListAdapter(ArrayList<LinkListObject> linkListObjects){
        mLinkListObjects = linkListObjects;
    }

    @NonNull
    @Override
    public ShowLinkListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.links, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LinkListObject listObject = mLinkListObjects.get(position);

        holder.linkListImg.setBackgroundResource(listObject.listImg);
        holder.linkListTitle.setText(listObject.listTitle);
    }

    @Override
    public int getItemCount() {
        return mLinkListObjects.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView linkListTitle;
        public ImageView linkListImg;

        public ViewHolder(View itemView) {
            super(itemView);

            linkListTitle = itemView.findViewById(R.id.listTitle);
            linkListImg = itemView.findViewById(R.id.listImg);
        }
    }
}