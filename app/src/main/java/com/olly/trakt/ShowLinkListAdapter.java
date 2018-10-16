package com.olly.trakt;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.olly.trakt.Objects.LinkListObject;
import com.olly.trakt.R;

import java.util.ArrayList;

public class ShowLinkListAdapter extends RecyclerView.Adapter<ShowLinkListAdapter.ViewHolder> {

    private ArrayList<LinkListObject> mLinkListObjects;
    private Context mCtx;

    public ShowLinkListAdapter(Context ctx, ArrayList<LinkListObject> linkListObjects){
        mLinkListObjects = linkListObjects;
        mCtx = ctx;
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

        holder.linkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(mLinkListObjects.get(position).listURL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                mCtx.startActivity(intent);
            }
        });

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
        public RelativeLayout linkLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            linkLayout = itemView.findViewById(R.id.linkLayout);
            linkListTitle = itemView.findViewById(R.id.listTitle);
            linkListImg = itemView.findViewById(R.id.listImg);
        }
    }
}