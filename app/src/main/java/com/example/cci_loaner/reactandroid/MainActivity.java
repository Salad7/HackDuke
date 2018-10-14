package com.example.cci_loaner.reactandroid;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.cci_loaner.reactandroid.Auth.LoginActivity;
import com.example.cci_loaner.reactandroid.CustomRecording.AndroidAudioRecorder;
import com.example.cci_loaner.reactandroid.CustomRecording.model.AudioChannel;
import com.example.cci_loaner.reactandroid.CustomRecording.model.AudioSampleRate;
import com.example.cci_loaner.reactandroid.CustomRecording.model.AudioSource;
import com.example.cci_loaner.reactandroid.Dropbox.DropboxVideo;
import com.example.cci_loaner.reactandroid.Models.User;
import com.example.cci_loaner.reactandroid.Models.Video;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    FragmentManager fragmentManager;
    ImageView addUser;
    ImageView settings_iv;
    ImageView calendar_iv;
    ImageView market_iv;
    FragmentChooseUser fragmentChooseUser;
    FragmentBucketStack fragmentBucketStack;
    FirebaseStorage storage;
    StorageReference soundRef;
    String filePath;
    String gsPath;
    String userToAddRecordingTo;
    String currentUser;
    String ts;
    ArrayList<String> bucketTerms;
    ImageView ticket;
    int REQUEST_MICROPHONE = 3;
    int REQUEST_EXTERNAL_READ = 3;
    int REQUEST_EXTERNAL_WRITE = 3;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int ACTIVITY_VIDEO_REQUEST = 2;  // The request code
    StorageReference videoRef;
    FirebaseDatabase database;
    DatabaseReference dbVideoRef;
    ArrayList<Video> videos;
    boolean asyncRecording;
    String asyncTitle;


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "Logged in user is: " + getIntent().getStringExtra("loggedInUser"));
        currentUser = getIntent().getStringExtra("loggedInUser");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            currentUser = user.getEmail().replace("@","").replace("."," ");
        } else {
            // No user is signed in
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);

        }

    }

    @Override // android recommended class to handle permissions
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("permission", "granted");
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.uujm
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();

                    //app cannot function without this permission for now so close it...
                    onDestroy();
                }
                return;
            }

            // other 'case' line to check fosr other
            // permissions this app might request
        }
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    REQUEST_MICROPHONE);

        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_READ);

        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_WRITE);

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        FirebaseApp.initializeApp(this);
        userToAddRecordingTo = "";
        settings_iv = findViewById(R.id.iv_settings);
        ticket = findViewById(R.id.iv_ticket);
        calendar_iv = findViewById(R.id.iv_calendar);
        market_iv = findViewById(R.id.iv_market);
        // Create a storage reference from our app
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        dbVideoRef = database.getReference();
        soundRef = storage.getReference().child("Sound");
        videoRef = storage.getReference().child("Video");
        fragmentManager = getSupportFragmentManager();
        Bundle b = new Bundle();
        b.putString("currentUser", currentUser);
        fragmentChooseUser = new FragmentChooseUser();
        fragmentBucketStack = new FragmentBucketStack();
        fragmentChooseUser.setArguments(b);
        fragmentManager.beginTransaction().add(R.id.container_, fragmentChooseUser).commit();
        addUser = findViewById(R.id.iv_add);
        bucketTerms = new ArrayList<>();
        initDialogCreateUser();
        settings_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });
        ticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentChooseUser.toggleHideUser();
            }
        });
        calendar_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ActivityCalendar.class);
                startActivity(i);
            }
        });
        market_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(MainActivity.this, ActivityMarketPlace.class);
//                startActivity(i);

                Intent i = new Intent(MainActivity.this, com.example.cci_loaner.reactandroid.OCR.MainActivity.class);
                startActivity(i);

            }
        });


    }


    public void initRecording(String userToAddRecordingTo, ArrayList<String> speeches, Intent data) {
        Long tsLong = System.currentTimeMillis() / 1000;
        ts = tsLong.toString();
        gsPath = "/recorded_audio" + ts + currentUser + ".wav";
        filePath = Environment.getExternalStorageDirectory() + gsPath;
        int color = getResources().getColor(R.color.colorPrimaryDark);
        int requestCode = 0;
        String recordingTitle = "";
        if (data != null) {
            if (data.hasExtra("recordingTitle")) {
                recordingTitle = data.getStringExtra("recordingTitle");
            }
        }
        AndroidAudioRecorder.with(this)
                // Required
                .setFilePath(filePath)
                .setColor(color)
                .setRequestCode(requestCode)
                // Optional
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.MONO)
                .setSampleRate(AudioSampleRate.HZ_16000)
                .setAutoStart(true)
                .setKeepDisplayOn(false)
                .setRecordingTitle(recordingTitle)
                // Start recording
                .record();
        this.userToAddRecordingTo = userToAddRecordingTo;
        this.bucketTerms = speeches;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                // Great! User has recorded and saved the audio file
                Uri file = Uri.fromFile(new File(filePath));
                StorageReference riversRef = soundRef.child(file.getLastPathSegment());
                UploadTask uploadTask = riversRef.putFile(file);
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        final long recordingLength = getMediaRecordingLength(new File((filePath)));
                        Toast.makeText(MainActivity.this, "Making OkHTTP Request", Toast.LENGTH_SHORT).show();
                        Log.d("MainActivity", "Making OkHTTP Request");
                        final String storageURL = "gs://react-932c4.appspot.com/Sound";
                        final int SIXTY_SECS_IN_MS = 60000;
                        OkHttpClient.Builder builder = new OkHttpClient.Builder();
                        builder.connectTimeout(5, TimeUnit.MINUTES)
                                .writeTimeout(5, TimeUnit.MINUTES)
                                .readTimeout(5, TimeUnit.MINUTES);
                        final OkHttpClient client = builder.build();
                        Request request = null;


                        //If the recording lasts more then 60 seconds, call the longrunning
                        if (recordingLength > SIXTY_SECS_IN_MS) {
                            request = new Request.Builder()
                                    .url("https://react-932c4.firebaseapp.com/longrun/" + ts + "/" + currentUser)
                                    .build();
                        }
                        //Else call regular
                        else {
                            request = new Request.Builder()
                                    .url("https://react-932c4.firebaseapp.com/test/" + ts + "/" + currentUser)
                                    .build();
                        }
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful())
                                    throw new IOException("Unexpected code " + response);

                                Headers responseHeaders = response.headers();
                                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                                    Log.e(responseHeaders.name(i), responseHeaders.value(i));
                                }
                                //Log.e("response",response.body().string());
                                Log.d("MainActivity", response.toString());
                                String finalEdit = response.body().string().replace("Transcription: ", "");
                                fragmentChooseUser.updateRecording(data.getStringExtra("recordingTitle"), finalEdit, downloadUrl.toString(), storageURL + gsPath, userToAddRecordingTo, recordingLength);
                            }
                        });

                    }
                });
            } else if (resultCode == RESULT_CANCELED) {
                // Oops! User has canceled the recording
            } else if (resultCode == 0x04) {
                Log.d("MainActivity", "resultCode == RESULT_FIRST_USER");
                // Great! User has recorded and saved the audio file
                Uri file = Uri.fromFile(new File(filePath));
                StorageReference riversRef = soundRef.child(file.getLastPathSegment());
                UploadTask uploadTask = riversRef.putFile(file);
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        final long recordingLength = getMediaRecordingLength(new File((filePath)));
                        Toast.makeText(MainActivity.this, "Making OkHTTP Request", Toast.LENGTH_SHORT).show();
                        Log.d("MainActivity", "Making OkHTTP Request");
                        final String storageURL = "gs://react-932c4.appspot.com/Sound";
                        final int SIXTY_SECS_IN_MS = 60000;
                        OkHttpClient.Builder builder = new OkHttpClient.Builder();
                        builder.connectTimeout(5, TimeUnit.MINUTES)
                                .writeTimeout(5, TimeUnit.MINUTES)
                                .readTimeout(5, TimeUnit.MINUTES);
                        final OkHttpClient client = builder.build();
                        Request request = null;
                        //If the recording lasts more then 60 seconds, call the longrunning
                        if (recordingLength > SIXTY_SECS_IN_MS) {
                            request = new Request.Builder()
                                    .url("https://react-932c4.firebaseapp.com/longrun/" + ts + "/" + currentUser)
                                    .build();
                        }
                        //Else call regular
                        else {
                            request = new Request.Builder()
                                    .url("https://react-932c4.firebaseapp.com/test/" + ts + "/" + currentUser)
                                    .build();
                        }
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful())
                                    throw new IOException("Unexpected code " + response);
                                Headers responseHeaders = response.headers();
                                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                                    Log.e(responseHeaders.name(i), responseHeaders.value(i));
                                }
                                Log.d("MainActivity", response.toString());
                                String finalEdit = response.body().string().replace("Transcription: ", "");
                                fragmentChooseUser.updateRecording(data.getStringExtra("recordingTitle"), finalEdit, downloadUrl.toString(), storageURL + gsPath, userToAddRecordingTo, recordingLength);
                            }
                        });
                        initRecording(userToAddRecordingTo, bucketTerms, data);

                    }

                });
            }
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            if (asyncRecording) {
                try {
                   //dispatchTakeVideoIntent();
                    //startVideoFrag(userToAddRecordingTo);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this,"Failed to start recording dispatch",Toast.LENGTH_LONG).show();
                }
            } else {
                //showUploadDialog();
                //videoAlertDialog();


            }
            Log.d("MainActivity", "Video URI: " + videoUri);
        }
        // Check which request it is that we're responding to
        else if (requestCode == ACTIVITY_VIDEO_REQUEST && resultCode == RESULT_OK) {
//                String videoPath = data.getStringExtra("video_path");
//                final String title = data.getStringExtra("video_title");
//                String length = data.getStringExtra("video_length");
//                new DropboxVideo(videoPath, getContentResolver(), title, MainActivity.this, currentUser,length);
//                Toast.makeText(this, "Uploading to dropbox", Toast.LENGTH_LONG).show();
//                if(data.hasExtra("auto-async")){
//                    Log.d("Returning Auto-Async",data.getBooleanExtra("auto-async",false)+"");
//                    asyncRecording = data.getBooleanExtra("auto-async",false);
//                    final int asyncTimer = data.getIntExtra("async-timer",0);
//                    if(asyncTimer == 0){
//                        //Toast.makeText(this,"Could not get Async timer",Toast.LENGTH_SHORT).show();
//                    }
//                    else{
//                        new CountDownTimer(1000, 1000) {
//                            public void onTick(long millisUntilFinished) {
//                            }
//
//                            public void onFinish() {
//                                Log.d("MainActivity", "Starting FragmentVideo");
//                                Intent i = new Intent(MainActivity.this, ActivityVideoRecording.class);
//                                i.putExtra("auto-async",asyncRecording);
//                                i.putExtra("async-timer",asyncTimer);
//                                i.putExtra("async-title",title);
//                                startActivityForResult(i, ACTIVITY_VIDEO_REQUEST);
//                                Handler handler = new Handler();
//                                handler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Log.d("ActivityVideoRecording","Recording stopped @ ");
//
//
//                                    }
//                                }, 1);
//                            }
//
//                        }.start();
//                        //Start recording again
//
//                    }
//                }
        }


    }



    public void videoAlertDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_video, null);
        final EditText mVideoTitle = dialogView.findViewById(R.id.video_title);
        final CheckBox isAsyncRecording = dialogView.findViewById(R.id.cb_loops);
        final Spinner timer = dialogView.findViewById(R.id.video_spinner);
        dialogBuilder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mVideoTitle.getText().toString().length() > 0) {
                    Log.d("MainActivity", "Starting FragmentVideo");
                    Log.d("isAsyncRecording",isAsyncRecording.isChecked()+"");
                    Intent i = new Intent(MainActivity.this, ActivityVideoRecording.class);
                    i.putExtra("auto-async",isAsyncRecording.isChecked());
                    i.putExtra("async-timer",Integer.parseInt(timer.getSelectedItem().toString().replaceAll("\\D+","")));
                    i.putExtra("async-title",mVideoTitle.getText().toString());
                    i.putExtra("userToAddRecordingTo",userToAddRecordingTo);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this, "Enter a title for the video", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    public void showUploadDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_uploading_to_dropbox, null);
        dialogBuilder.setTitle("File preparing to upload!");

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    public void uploadVideo(final Uri videoURI, final String mTitle) {
        Toast.makeText(this, "Uploading Video", Toast.LENGTH_SHORT).show();
        UploadTask uploadTask = videoRef.child(new Timestamp(System.currentTimeMillis()).toString()).putFile(videoURI);
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(MainActivity.this, "Unsuccessful upload", Toast.LENGTH_SHORT).show();
                exception.printStackTrace();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                ContentResolver cr = getContentResolver();
                int size = 0;
                try {
                    InputStream is = cr.openInputStream(videoURI);
                    size = is.available();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //updateUserVideo(downloadUrl.toString(), mTitle, getMediaRecordingLength(videoURI) + "", formatSize(size));
                Toast.makeText(MainActivity.this, "Successful upload", Toast.LENGTH_SHORT).show();

            }
        });
    }



    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    public long getMediaRecordingLength(File mediaFile) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(this, Uri.fromFile(mediaFile));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        retriever.release();
        return timeInMillisec;
    }
    public long getMediaRecordingLength(Uri mediaFile) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(this, mediaFile);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        retriever.release();
        return timeInMillisec;
    }
    public void initDialogCreateUser() {
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
// ...Irrelevant code for customizing the buttons and title
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_create_user, null);
                dialogBuilder.setView(dialogView);
                final EditText fname = (EditText) dialogView.findViewById(R.id.dialog_fname);
                final EditText lname = (EditText) dialogView.findViewById(R.id.dialog_lname);
                final EditText buckets = dialogView.findViewById(R.id.dialog_bucket_items);
                final Spinner languageET = dialogView.findViewById(R.id.language_spinner);
                Button createBtn = dialogView.findViewById(R.id.dialog_create);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                        (getApplicationContext(), android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.languages)); //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                        .simple_spinner_dropdown_item);
                languageET.setAdapter(spinnerArrayAdapter);
                final AlertDialog alertDialog = dialogBuilder.create();
                createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String firstName = fname.getText().toString();
                            String lastName = lname.getText().toString();
                            String bucketPhrases = buckets.getText().toString();
                            ArrayList<String> usersBucketPhrases = new ArrayList<>();
                            for (String term : bucketPhrases.split(";")
                                    ) {
                                if (!term.equals("")) {
                                    usersBucketPhrases.add(term);
                                }
                            }
                            User u = new User();
                            u.setName(firstName + " " + lastName);
                            u.setDate("March 24th 2018");
                            u.setSpeechContexts(usersBucketPhrases);
                            u.setLanguageCode(languageET.getSelectedItem().toString());
                            //parcelableSpeechContexts = u.getParcelableSpeechContext();
                            fragmentChooseUser.addUser(u);
                            alertDialog.dismiss();
                            Intent i = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(i);

                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Enter a first and last name", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertDialog.show();
                alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
    }
    public void startStackFrag(String userToGetRecordings) {
        ArrayList<String> batchWords = new ArrayList<>();
        batchWords.add("Money");
        batchWords.add("Stock");
        batchWords.add("Cash");
        batchWords.add("Success");
        batchWords.add("School");
        batchWords.add("Hard");
        batchWords.add("Work");
        Bundle b = new Bundle();
        b.putString("currentUser", currentUser);
        b.putString("recordingsOfUser", userToGetRecordings);
        b.putStringArrayList("bucketTerms", batchWords);
        Log.d("MainActivity", "Current User: " + currentUser);
        Log.d("MainActivity", "User whos recordings will be viewed: " + userToGetRecordings);
        fragmentBucketStack.setArguments(b);
        fragmentManager.beginTransaction().replace(R.id.container_, fragmentBucketStack).commit();
    }

    public void startChooseUserFrag() {
        Bundle b = new Bundle();
        b.putString("currentUser", currentUser);
        fragmentChooseUser.setArguments(b);
        fragmentManager.beginTransaction().replace(R.id.container_, fragmentChooseUser).commit();
    }

    public void startVideoFrag(String clickedUser) {
        userToAddRecordingTo = clickedUser;
        videoAlertDialog();

    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            takeVideoIntent.putExtra("android.intent.extra.durationLimit", 1800000); //5 minutes
            takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    public String getCurrentDate() {
        Calendar myCal = new GregorianCalendar();
        System.out.println("Day: " + myCal.get(Calendar.DAY_OF_MONTH));
        System.out.println("Month: " + myCal.get(Calendar.MONTH) + 1);
        System.out.println("Year: " + myCal.get(Calendar.YEAR));
        //Log.d("FragmentChooseUser","Day: " + myCal.get(Calendar.DAY_OF_MONTH)+ " Month: " + (myCal.get(Calendar.MONTH)+1) + " "+"Year: " + myCal.get(Calendar.YEAR));
        return (getMonthName(myCal.get(Calendar.MONTH))) + " " + ordinal(myCal.get(Calendar.DAY_OF_MONTH)) + " " + myCal.get(Calendar.YEAR);
    }

    public String getMonthName(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month);
        java.text.SimpleDateFormat month_date = new java.text.SimpleDateFormat("MMMM");
        String month_name = month_date.format(calendar.getTime());

        return month_name;
    }

    public String ordinal(int i) {
        String[] sufixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }


}
