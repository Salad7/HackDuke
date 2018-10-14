package com.example.cci_loaner.reactandroid;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cci_loaner.reactandroid.Dropbox.DropboxVideo;
import com.example.cci_loaner.reactandroid.Models.Bucket;
import com.example.cci_loaner.reactandroid.Models.Video;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.arjsna.swipecardlib.SwipeCardView;

import static android.content.ContentValues.TAG;

/**
 * Created by cci-loaner on 3/1/18.
 */


@SuppressWarnings("ALL")
public class FragmentBucketStack extends Fragment {

    ArrayList<Bucket> buckets;
    ArrayList<String> bucketTerms = new ArrayList<>();
    SwipeCardView stackView;
    StackViewAdapter stackViewAdapter;
    ProgressBar progressBar;
    FirebaseDatabase database;
    DatabaseReference usersRef;
    String currentUser;
    String userToAddRecordingTo;
    LoadRecordingsAsyncTask loadRecordingsAsyncTask;
    FloatingActionButton fab_record;
    FloatingActionButton fab_startChoose;
    FloatingActionButton fab_viewTranscripts;
    TextView currentRecordingViewPosition;
    ViewPager videoPager;
    Spinner mediaOptions;
    MediaPlayer mp;
    int recordingThatsViewedRightNow;
    boolean isPlaying = false;
    private FirebaseAuth mAuth;
    DatabaseReference dbVideoRef;
    ArrayList<Video> videos;
    CustomVideoPagerAdapter customVideoPagerAdapter;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stack,container,false);
        fab_viewTranscripts = v.findViewById(R.id.view_transcript);
        mp = new MediaPlayer();
        mediaOptions = v.findViewById(R.id.mediaOptions);
        videoPager = v.findViewById(R.id.videoPager);
        recordingThatsViewedRightNow = 0;
        mAuth = FirebaseAuth.getInstance();
        currentUser = convertEmailToParseable(mAuth.getCurrentUser().getEmail());
        userToAddRecordingTo = getArguments().getString("recordingsOfUser","");
        currentRecordingViewPosition = v.findViewById(R.id.current_record_viewed);
        fab_record = v.findViewById(R.id.stack_recording);
        fab_startChoose = v.findViewById(R.id.stack_finish);
        stackView = v.findViewById(R.id.stack);
        progressBar = v.findViewById(R.id.stack_progress);
        fab_viewTranscripts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDialogCreateUser();
            }
        });
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        dbVideoRef  = database.getReference();
        videos =new ArrayList<>();
        loadVideos();
        buckets = new ArrayList<>();
        bucketTerms = new ArrayList<>();
        if(getArguments().getStringArrayList("bucketTerms") != null){
            bucketTerms.addAll(getArguments().getStringArrayList("bucketTerms"));
        }
        //Add the users custom words
        bucketTerms.addAll(loadBucketPhrases());
        stackView.setFlingListener(new SwipeCardView.OnCardFlingListener() {
            @Override
            public void onCardExitLeft(Object dataObject) {
                Log.i(TAG, "Left Exit");
                try {
                    if (mp != null && mp.isPlaying()) {
                        mp.reset();

                    }
                    recordingThatsViewedRightNow++;
                }
                catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCardExitRight(Object dataObject) {
                Log.i(TAG, "Right Exit");
                try {
                    if (mp != null && mp.isPlaying()) {
                        mp.reset();

                    }
                    recordingThatsViewedRightNow++;
                }
                catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                Log.i(TAG, "Adapter to be empty");
                //add more items to adapter and call notifydatasetchanged
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                Log.i(TAG, "Scroll");
            }

            @Override
            public void onCardExitTop(Object dataObject) {
                Log.i(TAG, "Top Exit");
                try {
                    if (mp != null && mp.isPlaying()) {
                        mp.reset();

                    }
                    recordingThatsViewedRightNow++;
                }
                catch (IllegalStateException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCardExitBottom(Object dataObject) {
                Log.i(TAG, "Bottom Exit");
                try {
                    if (mp != null && mp.isPlaying()) {
                        mp.reset();

                    }
                    recordingThatsViewedRightNow++;
                }
                catch (IllegalStateException e){
                    e.printStackTrace();
                }

            }
        });

        fab_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).initRecording(userToAddRecordingTo.replace(" ",""),getSpeechContexts(),null);
                loadBucketPhrases();

            }
        });

        fab_startChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (mp.isPlaying()) {
                        mp.reset();
                    }
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
                ((MainActivity) getActivity()).startChooseUserFrag();
            }
        });
        loadVideoOrMedia();
        return v;
    }

    public void loadVideoOrMedia(){
        if(mediaOptions.getSelectedItem().toString().equals("Audio")){
            stackView.setVisibility(View.VISIBLE);
            videoPager.setVisibility(View.INVISIBLE);
        }
        else{
            stackView.setVisibility(View.INVISIBLE);
            videoPager.setVisibility(View.VISIBLE);
        }
        mediaOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    stackView.setVisibility(View.VISIBLE);
                    videoPager.setVisibility(View.INVISIBLE);
                }
                else {
                    stackView.setVisibility(View.INVISIBLE);
                    videoPager.setVisibility(View.VISIBLE);
                    if(videos.size() > 1) {
                        Toast.makeText(getContext(), "Swipe left or right to transition videos",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void initDialogCreateUser(){
        if( buckets != null && (recordingThatsViewedRightNow < buckets.size())) {
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_view_transcripts, null);
            dialogBuilder.setView(dialogView)
                    .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create();
            final ListView transcriptList= dialogView.findViewById(R.id.transcript_list);
            transcriptList.setAdapter(new DialogTranscriptAdapater(getActivity(),R.layout.dialog_transcript_list_item,buckets.get(recordingThatsViewedRightNow).getTranscripts()));
            final AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.setTitle("Full Transcript of conversation");
            alertDialog.show();
        }


    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("FragmentBucketStack","Fragment Bucket Stack stopped!");
        if (mp != null) {
            mp.release();
        }
    }

    public void loadBuckets(ArrayList<Bucket> loadedBuckets){
        buckets = loadedBuckets;
        //stackViewAdapter.notifyDataSetChanged();
        currentRecordingViewPosition.setText("1/"+loadedBuckets.size());
        stackViewAdapter = new StackViewAdapter(loadedBuckets,R.layout.custom_recording,getContext());
        stackView.setAdapter(stackViewAdapter);
        Log.d("loadBuckets size",stackViewAdapter.getCount()+"");
        stackViewAdapter.notifyDataSetChanged();
    }

    public ArrayList<String> getKeyPartsOfAudio(String fullText, ArrayList<String> phr){

        ArrayList<String> splitStringBySentence = new ArrayList<>();
        //Here we just add the phrases one by one
        //In the adapter we will modify the sentences so that if it contains a keyword we'll underline the text
        for (String sentence: fullText.split("\n")
             ) {
            splitStringBySentence.add(sentence);
        }
        return splitStringBySentence;
    }

    public ArrayList<String> loadBucketPhrases(){
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.d("FragmentBucketStack","Current user is "+currentUser);
                    Log.d("FragmentBucketStack","user to add recording to is "+userToAddRecordingTo);
                    ArrayList<String> temp = (ArrayList<String>) dataSnapshot.child(currentUser).child(userToAddRecordingTo).child("bucketTerms").getValue();
                    if(temp != null)
                    {
                        String[] splitPhrases = temp.get(0).split(";");
                        for (String phrase: splitPhrases
                                ) {
                            Log.d("FragmentBucketStack: ","Key Phrase: "+phrase);
                            bucketTerms.add(phrase.toLowerCase());

                        }
                    }
                    if(bucketTerms.size() == 0){
                        Toast.makeText(getContext(),"No bucket terms found",Toast.LENGTH_SHORT).show();
                    }
                    //After terms are loaded, run async task
                    loadRecordingsAsyncTask = new LoadRecordingsAsyncTask();
                    loadRecordingsAsyncTask.execute("BLAH");

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return bucketTerms;
    }


    public class StackViewAdapter extends ArrayAdapter {
        private ArrayList<Bucket> stackViewBuckets;
        private Context context;
        private int itemLayout;
        ProgressBar loadingRecordingProgress;

        public StackViewAdapter(ArrayList<Bucket> bucks,int resource, Context ctx) {
            super(ctx, resource, bucks);
            this.stackViewBuckets = bucks;
            context = ctx;
            itemLayout = resource;
        }



        @Override
        public int getCount() {
            return stackViewBuckets.size();
        }
        @Override
        public Bucket getItem(int position) {
            return stackViewBuckets.get(position);
        }

        public void startPlaying(String fileName, int currentProgress){
            if(!isPlaying){
                try {
                    mp.setDataSource(fileName);
                    mp.prepare();
                    mp.seekTo(currentProgress*1000);
                    mp.start();
                } catch (IOException e) {
                    Log.e("FragmentBucketStack", "prepare() failed");
                }
                isPlaying = true;
            }
        }


        private String getClockFormat(int secs){
            int hours = secs / 3600;
            int minutes = (secs % 3600) / 60;
            int seconds = secs % 60;
            String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            return timeString;
        }





        @Override
        public View getView(final int position, View view, @NonNull final ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(itemLayout,parent,false);
            final SeekBar seekBar  = v.findViewById(R.id.seek_play_recording);
            TextView rec = (TextView)  v.findViewById(R.id.tv_current_rec);
            ImageView deleteItem = v.findViewById(R.id.iv_delete_stack_item);
            final TextView timer = v.findViewById(R.id.timer);
            loadingRecordingProgress = v.findViewById(R.id.loading_recording_spinner);
            seekBar.setMax((int)(stackViewBuckets.get(position).getLength()/1000));
            try{
                rec.setText(stackViewBuckets.get(position).getBucketName()+" | "+getClockFormat((int)(stackViewBuckets.get(position).getLength()/1000)));
            }
            catch (NullPointerException e){
                e.printStackTrace();
                rec.setText("Recording "+(position+1)+" | "+getClockFormat((int)(stackViewBuckets.get(position).getLength()/1000)));
            }
            final ListView listView = v.findViewById(R.id.bucket_list);
            final ImageView play = v.findViewById(R.id.iv_play);
            final ImageView pause = v.findViewById(R.id.iv_pause);
            final TextView recordingDate = v.findViewById(R.id.date);
            try {
                recordingDate.setText(stackViewBuckets.get(position).getDate());

            } catch (Exception e){
                e.printStackTrace();
            }
            pause.setVisibility(View.INVISIBLE);
            ImageView restart = v.findViewById(R.id.iv_restart);
            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    usersRef.child(currentUser).child(userToAddRecordingTo).child("records").child(stackViewBuckets.get(position).getFbKey()).removeValue();
                    Toast.makeText(getContext(),"Deleted record!",Toast.LENGTH_SHORT).show();
                    stackViewAdapter.notifyDataSetChanged();
                }
            });
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar sb, int i, boolean b) {

                    if(mp != null && b){
                        mp.seekTo(i*1000);
                        Log.d("SoundTesting","Seeking to: "+i);
                    }
                    timer.setText(getClockFormat(i));

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(final SeekBar seekBar) {
                    Log.d("FragmentBucketStack","User has stopped dragging");
                    if(!isPlaying){
                        Toast.makeText(getContext(),"Loading recording!",Toast.LENGTH_SHORT).show();
                        startPlaying(stackViewBuckets.get(position).getRecordingURL(),seekBar.getProgress());
                        final Handler mHandler = new Handler();
                        //Make sure you update Seekbar on UI thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (isPlaying) {
                                        int mCurrentPosition = mp.getCurrentPosition() / 1000;
                                        seekBar.setProgress(mCurrentPosition);
                                        Log.d("SoundTesting", "Setting seekBar position");

                                    }
                                    mHandler.postDelayed(this, 1000);
                                } catch (IllegalStateException e){
                                    e.printStackTrace();
                                }
                            }
                        });
                        Log.d("SoundTesting","Called startPlaying()");
                    }
                    else {
                        mp.seekTo(mp.getCurrentPosition());
                        mp.start();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            play.setVisibility(View.INVISIBLE);
                            pause.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
                //Make sure you update Seekbar on UI thread
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Log.d("FragmentBucketStack", "Completed audio recording");
                        AppCompatActivity act = (AppCompatActivity) context;
                        act.runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                play.setVisibility(View.VISIBLE);
                                pause.setVisibility(View.INVISIBLE);
                            } });
                        mp.reset();
                        isPlaying = false;

                    }
                });
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isPlaying) {
                        Toast.makeText(getContext(),"Loading recording!",Toast.LENGTH_SHORT).show();
                        startPlaying(stackViewBuckets.get(position).getRecordingURL(),0);
                        //loadingRecordingProgress.setVisibility(View.INVISIBLE);
                        final Handler mHandler = new Handler();
                        //Make sure you update Seekbar on UI thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (isPlaying) {
                                        int mCurrentPosition = mp.getCurrentPosition() / 1000;
                                        seekBar.setProgress(mCurrentPosition);
                                        Log.d("SoundTesting", "Setting seekBar position");

                                    }
                                    mHandler.postDelayed(this, 1000);
                                } catch (IllegalStateException e){
                                    e.printStackTrace();
                                }
                            }
                        });
                        Log.d("SoundTesting","Called startPlaying()");
                    }
                    else {
                        mp.seekTo(mp.getCurrentPosition());
                        mp.start();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            play.setVisibility(View.INVISIBLE);
                            pause.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
            restart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        if(mp.isPlaying()) {
                            mp.seekTo(0);
                        }
                        else {

                        }
                    }
                    catch(Exception e){e.printStackTrace();

                    }
                }
            });
            pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mp.pause();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pause.setVisibility(View.INVISIBLE);
                            play.setVisibility(View.VISIBLE);
                        }
                    });

                }
            });
            final ArrayList<String> transcripts = stackViewBuckets.get(position).getTranscripts();
            TextView sampleText = v.findViewById(R.id.sample_text);
            if(stackViewBuckets.get(position).getSampleText().length() == 0) {
                sampleText.setText("No transcript available");
            }

            listView.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, transcripts) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    String transcript = transcripts.get(position);
                    for (String phrase: bucketTerms
                         ) {
                        String transLowerCae = transcript;
                        transLowerCae.toLowerCase();
                        if(transLowerCae.contains(phrase.toLowerCase())){
                            textView.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                        }
                    }
                    textView.setText(transcript);

                    return textView;
                }
            });
            return v;
        }
    }

    public void loadVideos(){
        videos.clear();
        dbVideoRef.child("users").child(currentUser).child(userToAddRecordingTo.replace(" ","")).child("videos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    for (DataSnapshot videoSnap : dataSnapshot.getChildren()
                            ) {
                        Video video = new Video();
                        video.setUserToSaveUnder(videoSnap.child("userToSaveUnder").getValue(String.class));
                        video.setUrl(videoSnap.child("url").getValue(String.class));
                        video.setTitle(videoSnap.child("title").getValue(String.class));
                        video.setSize(videoSnap.child("size").getValue(String.class));
                        video.setLength(videoSnap.child("length").getValue(String.class));
                        video.setDate(videoSnap.child("date").getValue(String.class));
                        video.setVideoStoragePath(videoSnap.child("videoStoragePath").getValue(String.class));
                        videos.add(video);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                customVideoPagerAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        customVideoPagerAdapter = new CustomVideoPagerAdapter(getContext(),videos,R.layout.custom_video_recording_item);
        videoPager.setAdapter(customVideoPagerAdapter);

    }

    public class LoadRecordingsAsyncTask extends AsyncTask<String,Integer,ArrayList<Bucket>>{
        ArrayList<Bucket> AsyncBuckets;
        LoadRecordingsAsyncTask(){
            AsyncBuckets = new ArrayList<>();
        }
        @Override
        protected void onPostExecute(ArrayList<Bucket> buckets) {
            super.onPostExecute(buckets);
            Log.d("LoadRecordingAsyncTask","OnPostExecute called");
            //loadBuckets(buckets);//
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar.setMax(100);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
            //progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected ArrayList<Bucket> doInBackground(String... strings) {
            Log.d("LoadRecordingAsyncTask","doInBackground called");

            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    AsyncBuckets.clear();
                    int publishProgressVal = 1;
                    progressBar.setMax((int)dataSnapshot.getChildrenCount());
                    // Get Post object and use the values to update the UI
                    for (DataSnapshot recording : dataSnapshot.getChildren()) {
                        //Here you can access the child.getKey()
                        //Log.d("FragmentBucketStack: ","Record #: "+publishProgressVal);
                        try {
                            Bucket bucket = new Bucket();
                            bucket.setFbKey(recording.getKey());
                            bucket.setLength(recording.child("length").getValue(Integer.class));
                            bucket.setFullText(recording.child("fullText").getValue(String.class));
                            bucket.setSampleText(recording.child("sampleText").getValue(String.class));
                            bucket.setRecordingURL(recording.child("recordingURL").getValue(String.class));
                            bucket.setGsURl(recording.child("gsURL").getValue(String.class));
                            if(recording.child("bucketName").getValue(String.class) != null){
                                bucket.setBucketName(recording.child("bucketName").getValue(String.class));
                            }
                            if(recording.hasChild("date")){
                                String dateToText = recording.child("date").getValue(String.class);
                                Log.d("FragmentBucketStack","dateToText: "+dateToText);
                                bucket.setDateInText(dateToText);
                            }
                            //Log.d("FragmentBucketStack","bucketKeyWords size: "+bucketKeyWords.size()+"");
                            //Log.d("FragmentBucketStack","bucketTerms size: "+bucketTerms.size()+"");
                            bucket.addTranscript(getKeyPartsOfAudio(recording.child("fullText").getValue(String.class),bucketTerms));
                            AsyncBuckets.add(bucket);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        publishProgressVal+=1;
                        publishProgress(publishProgressVal);
                    }
                    Collections.reverse(AsyncBuckets);
                    loadBuckets(AsyncBuckets);
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            };
            try {
                usersRef.child(currentUser+"/"+userToAddRecordingTo+"/records").addValueEventListener(postListener);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            Log.d("LoadRecordingAsyncTask","doInBackground complete");
            return AsyncBuckets;
        }
    }

    class DialogTranscriptAdapater extends ArrayAdapter {
        ArrayList<String> trans;
        public DialogTranscriptAdapater(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
            super(context, resource, objects);
            trans = objects;
        }


        @Nullable
        @Override
        public Object getItem(int position) {
            return trans.get(position);
        }

        @Override
        public int getCount() {
            Log.d("Trans count: ",""+trans.size());
            return trans.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(R.layout.dialog_transcript_list_item,parent,false);
            final TextView transcriptTV = v.findViewById(R.id.textView8);
            transcriptTV.setText(trans.get(position));


            return v;
        }
    }

    public ArrayList<String> getSpeechContexts(){
        return bucketTerms;
    }


    public String convertEmailToParseable(String email){
        Pattern pt = Pattern.compile("[^a-zA-Z0-9]");
        Matcher match= pt.matcher(email);
        while(match.find())
        {
            String s= match.group();
            email=email.replace(match.group(), "");        }
            email.replace(" ","");
        return email;
    }

    public class CustomVideoPagerAdapter extends PagerAdapter{

        private Context mContext;
        int res;
        private ArrayList<Video> mVideos;

        public CustomVideoPagerAdapter(Context context, ArrayList<Video> vids, int res) {
            mContext = context;
            mVideos = vids;
            Log.d("FragmentBucketStack","Num Videos: "+mVideos.size());
            this.res = res;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, final int position) {
            Video vid = mVideos.get(position);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(res, collection, false);
            collection.addView(layout);
            TextView mTitle = layout.findViewById(R.id.custom_vid_title);
            TextView mSize = layout.findViewById(R.id.custom_vid_size);
            TextView mDate = layout.findViewById(R.id.custom_vid_date);
            TextView mLength = layout.findViewById(R.id.custom_vid_length);
            Button mPlay = layout.findViewById(R.id.custom_video_play);
            mTitle.setText(vid.getTitle());
            mDate.setText(vid.getDate());
            mLength.setText(getDurationBreakdown(Long.parseLong(vid.getLength())));
            mSize.setText(vid.getSize());

            mPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getContext(),ActivityVideoPlayer.class);
                    i.putExtra("video_url",mVideos.get(position).getUrl());
                    startActivity(i);
                }
            });

            return layout;
        }



        public String getDurationBreakdown(long millis) {
            if(millis < 0) {
                throw new IllegalArgumentException("Duration must be greater than zero!");
            }

            long days = TimeUnit.MILLISECONDS.toDays(millis);
            millis -= TimeUnit.DAYS.toMillis(days);
            long hours = TimeUnit.MILLISECONDS.toHours(millis);
            millis -= TimeUnit.HOURS.toMillis(hours);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            millis -= TimeUnit.MINUTES.toMillis(minutes);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

            StringBuilder sb = new StringBuilder(64);
            //sb.append(days);
            //sb.append(" Days ");
            //sb.append(hours);
            //sb.append(" Hours ");
            if(hours > 0 && hours < 9){
                sb.append("0"+hours+":");
            }
            else if(hours == 0){
                sb.append("00:");
            }
            else {
                sb.append(hours+":");
            }

            if(minutes > 0 && minutes < 9){
                sb.append("0"+minutes+":");
            }
            else if(minutes == 0){
                sb.append("00:");
        }
            else {
                sb.append(minutes+":");
            }

            if(seconds > 0 && seconds < 9){
                sb.append("0"+seconds);
            }
            else if(seconds == 0){
                sb.append("00");
            }
            else {
                sb.append(seconds);
            }
            return(sb.toString());
        }

        @Override
        public int getCount() {
            return videos.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


    }




}
