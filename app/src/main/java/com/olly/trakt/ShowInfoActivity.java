package com.olly.trakt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.olly.trakt.Objects.ServerCallback;
import com.olly.trakt.Objects.TMDBObject;
import com.olly.trakt.Objects.TraktExtendedObject;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class ShowInfoActivity extends AppCompatActivity {

    private TextView txtDescription;
    private TraktExtendedObject trakt;
    private ActionBar actionBar;
    private ImageView toolbarImg;
    private AppBarLayout appBarLayout;
    CollapsingToolbarLayout toolbarLayout;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_bar);

        Intent intent = getIntent();
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        toolbarLayout = findViewById(R.id.toolbar_layout);
        txtDescription = findViewById(R.id.txtDescription);
        toolbarImg = findViewById(R.id.toolbarImage);

        actionBar.setTitle(intent.getStringExtra("showName"));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Map<String, String> params = new HashMap<>();
        params.put("trakt-api-version", "2");
        params.put("trakt-api-key", MainActivity.CLIENT_ID);
        TraktManager.getInstance(getApplicationContext()).getString(MessageFormat.format("{0}shows/{1}?extended=full", MainActivity.API_URL, intent.getStringExtra("showSlug")), params, new ServerCallback() {
                    @Override
                    public void onSuccess(String result) {
                        trakt = new Gson().fromJson(result, TraktExtendedObject.class);
                        txtDescription.setText(trakt.overview);
                    }
                });

        url = "https://image.tmdb.org/t/p/w50" + intent.getStringExtra("backdrop_path");
        Picasso.get().load("https://image.tmdb.org/t/p/w500" + intent.getStringExtra("backdrop_path")).into(toolbarImg);
//        Bitmap bitmap = ((BitmapDrawable)toolbarImg.getDrawable()).getBitmap();
//        int pixel = bitmap.getPixel(1,1);
//        appBarLayout.setBackgroundColor(Color.rgb(255,0,0));

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }
}
