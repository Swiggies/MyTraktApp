package com.olly.trakt.Objects;

import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.olly.trakt.R;

import java.util.ArrayList;

public class LinkListObject {

    public int listImg;
    public String listURL;
    public String listTitle;

    public LinkListObject(int img, String url, String title){
        listImg = img;
        listURL = url;
        listTitle = title;
    }
}
