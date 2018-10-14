package com.example.cci_loaner.reactandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cci_loaner.reactandroid.Models.MarketplaceVideo;
import com.example.cci_loaner.reactandroid.Models.Video;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by cci-loaner on 4/19/18.
 */

public class ActivityMarketPlace extends AppCompatActivity {

    ArrayList<MarketplaceVideo> marketplaceVideos;
    FirebaseDatabase database;
    DatabaseReference mRef;
    RecyclerView mRecyclerView;
    CustomMarketPlaceAdapter customMarketPlaceAdapter;
    ProgressBar marketProgress;
    EditText marketSearch;
    ValueEventListener valueEventListener;
    ValueEventListener queryListener;
    ImageView marketFinish;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace);
        mRecyclerView = findViewById(R.id.market_rv);
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference("users");
        marketplaceVideos = new ArrayList<>();
        marketProgress = findViewById(R.id.market_progress);
        marketSearch = findViewById(R.id.market_search_et);
        marketFinish = findViewById(R.id.market_finish);
        marketFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        loadVideos();
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        customMarketPlaceAdapter = new CustomMarketPlaceAdapter(marketplaceVideos);
        mRecyclerView.setAdapter(customMarketPlaceAdapter);


        marketSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                marketProgress.setVisibility(View.VISIBLE);
                if(s.length() == 0){
                    //Remove listener, reload list
                    if(valueEventListener != null) {
                        mRef.removeEventListener(valueEventListener);
                        marketProgress.setVisibility(View.VISIBLE);
                        loadVideos();
                    }
                }
                else if(queryListener != null){
                        mRef.removeEventListener(queryListener);
                    }
                queryVideos(s+"");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }





    public class CustomMarketPlaceAdapter extends RecyclerView.Adapter<CustomMarketPlaceAdapter.ViewHolder>{

        ArrayList<MarketplaceVideo> marketplaceVideosAdapter;

        CustomMarketPlaceAdapter(ArrayList<MarketplaceVideo> marketplaceVideoArrayList){
            marketplaceVideosAdapter = marketplaceVideoArrayList;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_marketplace_video, parent, false);

            ViewHolder vh = new ViewHolder(v);
            vh.title = v.findViewById(R.id.market_title);
            vh.date = v.findViewById(R.id.market_date);
            vh.length = v.findViewById(R.id.market_length);
            vh.thumb = v.findViewById(R.id.market_thumbnail);
            return vh;

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            ((ViewHolder)holder).title.setText(marketplaceVideosAdapter.get(position).getTitle());
            ((ViewHolder)holder).date.setText(marketplaceVideosAdapter.get(position).getDate());
            ((ViewHolder)holder).length.setText(marketplaceVideosAdapter.get(position).getLength());
            try {
                if (!marketplaceVideosAdapter.get(position).getThumbnailURL().equals("")) {
                    Picasso.get().load(marketplaceVideosAdapter.get(position).getThumbnailURL()).into((holder).thumb);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return marketplaceVideosAdapter.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            View v;
            public TextView title;
            public TextView date;
            public TextView length;
            public ImageView thumb;

            public ViewHolder(View v){
                super(v);
                this.v = v;
            }

    }


    }


    public void queryVideos(final String query){
        ValueEventListener queryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                marketplaceVideos.clear();
                for (DataSnapshot hostUser: dataSnapshot.getChildren()
                        ) {
                    for (DataSnapshot subuser: hostUser.getChildren()
                            ) {
                        if(subuser.hasChild("videos")) {
                            for (DataSnapshot videoObject: subuser.child("videos").getChildren()){
                                String title = videoObject.child("title").getValue(String.class);
                                marketProgress.setVisibility(View.INVISIBLE);
                                Log.d("ActivityMarketPlace","Title: "+title+" Contains: "+query+" ?");
                                if(title.contains(query)) {
                                    Log.d("ActivityMarketPlace", "Adding video, title: " + videoObject.child("title").getValue(String.class));
                                    MarketplaceVideo marketplaceVideo = new MarketplaceVideo();
                                    marketplaceVideo.setDate(videoObject.child("date").getValue(String.class));
                                    marketplaceVideo.setLength(videoObject.child("length").getValue(String.class));
                                    marketplaceVideo.setTitle(videoObject.child("title").getValue(String.class));
                                    marketplaceVideo.setVideoURL(videoObject.child("thumbnailURL").getValue(String.class));
                                    marketplaceVideos.add(marketplaceVideo);
                                }
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.addValueEventListener(queryListener);
    }
    public void loadVideos(){
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                marketplaceVideos.clear();
                //int count = 0;
                for (DataSnapshot hostUser: dataSnapshot.getChildren()
                     ) {
                    for (DataSnapshot subuser: hostUser.getChildren()
                         ) {
                        if(subuser.hasChild("videos")) {
                            for (DataSnapshot videoObject: subuser.child("videos").getChildren()){
                                //if(count < 20) {
                                        Log.d("ActivityMarketPlace", "Adding video, title: " + videoObject.child("title").getValue(String.class));
                                        MarketplaceVideo marketplaceVideo = new MarketplaceVideo();
                                        marketplaceVideo.setDate(videoObject.child("date").getValue(String.class));
                                        marketplaceVideo.setLength(videoObject.child("length").getValue(String.class));
                                        marketplaceVideo.setTitle(videoObject.child("title").getValue(String.class));
                                        marketplaceVideo.setVideoURL(videoObject.child("thumbnailURL").getValue(String.class));
                                        marketProgress.setVisibility(View.INVISIBLE);
                                        marketplaceVideos.add(marketplaceVideo);
                                        //count++;
                                    //break;
                                //}
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.addValueEventListener(valueEventListener);

        // marketplaceVideos = videos;
    }
}
