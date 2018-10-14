package com.example.cci_loaner.reactandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cci_loaner.reactandroid.Auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by msalad on 3/20/2018.
 */

public class SettingsActivity extends AppCompatActivity {

    ImageView back_iv;
    Button logout_btn;
    private FirebaseAuth mAuth;
    private TextView total_recording_tv;
    private Switch useful_phrases;
    private Spinner sort_by;
    private TextView interval_tv;
    private SeekBar interval_seekbar;
    private CheckBox notificationBox;
    FirebaseDatabase database;
    DatabaseReference usersRef;
    private String currentUser;
    int recordingTimeMS = 0;
    SharedPreferences sharedPrefs;
    AppCompatActivity context;
    public void wireViews(){
        back_iv = findViewById(R.id.back_home_iv);
        logout_btn = findViewById(R.id.logout_btn_settings);
        total_recording_tv = findViewById(R.id.tv_recording_time);
        useful_phrases = findViewById(R.id.switch_useful_phrases);
        sort_by = findViewById(R.id.spinner_sort);
        interval_tv = findViewById(R.id.tv_recording_intervals);
        interval_seekbar = findViewById(R.id.spinner_recording_interval);
        notificationBox = findViewById(R.id.cb_notifications);
        useful_phrases.setChecked(sharedPrefs.getBoolean("use_phrases",false));
        try {
            interval_seekbar.setProgress(sharedPrefs.getInt("interval", 0));
            sort_by.setSelection(sharedPrefs.getInt("sortby",0));
            if(sharedPrefs.getInt("interval",0) == 0){
                interval_tv.setText("Recording Intervals | 1 minute");
            }
            else{
                interval_tv.setText("Recording Intervals | "+(sharedPrefs.getInt("interval",0)+1)+" minutes");

            }
            Log.d("SettingsActivity","Interval = "+sharedPrefs.getInt("interval", 0));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        interval_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt("interval",progress).commit();
                if(sharedPrefs.getInt("interval",0) == 0){
                    interval_tv.setText("Recording Intervals | 1 minute");
                }
                else{
                    interval_tv.setText("Recording Intervals | "+(sharedPrefs.getInt("interval",0)+1)+" minutes");

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        sort_by.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt("sortby",position).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        notificationBox.setChecked(sharedPrefs.getBoolean("notification",false));
        notificationBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("notification",isChecked).commit();
            }
        });


    }

    public void addListeners(){
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(SettingsActivity.this,LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
        back_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingsActivity.this,MainActivity.class);
                startActivity(i);
                finish();

            }
        });
        useful_phrases.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("use_phrases",isChecked).commit();
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    public void setTotalRecordingTime(){
        ValueEventListener singleListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Visit each user that has a recording
                for (DataSnapshot subSnap: dataSnapshot.getChildren()
                        ) {
                    try{
                        //Visit all recordings inside that user
                        for (DataSnapshot timeSnap : subSnap.child("records").getChildren()
                                ) {
                            Log.d("SettingsActivity",timeSnap.getRef().toString());
                            recordingTimeMS += timeSnap.child("length").getValue(Integer.class);
                            total_recording_tv.setText(recordingTimeMS+"");

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        usersRef.addListenerForSingleValueEvent(singleListener);


    }


    public void initFirebaseAuth(){
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Toast.makeText(this,"No user is logged in",Toast.LENGTH_SHORT).show();
        }
        Log.d("SettingsActivity","Username is "+currentUser.getEmail());
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;
        sharedPrefs = context.getSharedPreferences(
                getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        initFirebaseAuth();
        wireViews();
        addListeners();
        try {
            currentUser = convertEmailToParseable(mAuth.getCurrentUser().getEmail());
            database = FirebaseDatabase.getInstance();
            usersRef = database.getReference("users");
            setTotalRecordingTime();
        }
        catch (Exception e){
            e.printStackTrace();
            Intent i = new Intent(SettingsActivity.this,MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    }

    public String convertEmailToParseable(String email){
        Pattern pt = Pattern.compile("[^a-zA-Z0-9]");
        Matcher match= pt.matcher(email);
        while(match.find())
        {
            String s= match.group();
            email=email.replace(match.group(), "");        }
        return email;
    }
}
