package com.example.cci_loaner.reactandroid;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.cci_loaner.reactandroid.Auth.LoginActivity;
import com.example.cci_loaner.reactandroid.Dropbox.DropboxVideo;
import com.example.cci_loaner.reactandroid.Models.Video;
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
import com.squareup.haha.perflib.Main;

import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
/**
 * Created by cci-loaner on 4/11/18.
 */

@SuppressWarnings("MagicConstant")
public class ActivityVideoRecording extends AppCompatActivity {

        private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
        private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
        private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
        private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

        private static final String TAG = "ActivityVideoRecording";
        private static final int REQUEST_VIDEO_PERMISSIONS = 1;
        private static final String FRAGMENT_DIALOG = "dialog";

        private static final String[] VIDEO_PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
        };

        static {
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
        }

        static {
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
        }

        /**
         */
        private TextureView mTextureView;

        /**
         * Button to record video
         */
        private ImageView mStartWhite;
        private ImageView mStartRed;
        private ImageView mBack;
        private ImageView mStop;
        private Button mTimer;
        private String videoURI;
        int recordOnce;
        CountDownTimer countDownTimer;
        private String currentUser;
        private String userToAddRecordingTo;
        ArrayList<Video> videos;
    StorageReference videoRef;
    FirebaseDatabase database;
    DatabaseReference dbVideoRef;




    /**
         * A reference to the opened {@link android.hardware.camera2.CameraDevice}.
         */
        private CameraDevice mCameraDevice;

        /**
         * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
         * preview.
         */
        private CameraCaptureSession mPreviewSession;

        /**
         * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
         * {@link TextureView}.
         */
        private TextureView.SurfaceTextureListener mSurfaceTextureListener
                = new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                                  int width, int height) {
                openCamera(width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                    int width, int height) {
                configureTransform(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }

        };

        /**
         * The {@link android.util.Size} of camera preview.
         */
        private Size mPreviewSize;

        /**
         * The {@link android.util.Size} of video recording.
         */
        private Size mVideoSize;

        /**
         * MediaRecorder
         */
        private MediaRecorder mMediaRecorder;

        /**
         * Whether the app is recording video now
         */
        private boolean mIsRecordingVideo;

        /**
         * An additional thread for running tasks that shouldn't block the UI.
         */
        private HandlerThread mBackgroundThread;

        /**
         * A {@link Handler} for running tasks in the background.
         */
        private Handler mBackgroundHandler;

        /**
         * A {@link Semaphore} to prevent the app from exiting before closing the camera.
         */
        private Semaphore mCameraOpenCloseLock = new Semaphore(1);

        /**
         * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
         */
        private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                mCameraDevice = cameraDevice;
                startPreview();
                mCameraOpenCloseLock.release();
                if (null != mTextureView) {
                    configureTransform(mTextureView.getWidth(), mTextureView.getHeight());


                }
            }


            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                mCameraOpenCloseLock.release();
                cameraDevice.close();
                mCameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int error) {
                mCameraOpenCloseLock.release();
                cameraDevice.close();
                mCameraDevice = null;
                Activity activity = ActivityVideoRecording.this;
                if (null != activity) {
                    Log.d("ActivityVideoRecording","onError Finishing Activity");
                    if(countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    Intent i = new Intent(ActivityVideoRecording.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            }

        };
        private Integer mSensorOrientation;
        private String mNextVideoAbsolutePath;
        private CaptureRequest.Builder mPreviewBuilder;
        boolean isSupportAutoFocus = false;

        public static ActivityVideoRecording newInstance() {
            return new ActivityVideoRecording();
        }

        /**
         * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
         * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
         *
         * @param choices The list of available sizes
         * @return The video size
         */
        private static Size chooseVideoSize(Size[] choices) {
            for (Size size : choices) {
                if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                    return size;
                }
            }
            Log.e(TAG, "Couldn't find any suitable video size");
            return choices[choices.length - 1];
        }

        /**
         * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
         * width and height are at least as large as the respective requested values, and whose aspect
         * ratio matches with the specified value.
         *
         * @param choices     The list of sizes that the camera supports for the intended output class
         * @param width       The minimum desired width
         * @param height      The minimum desired height
         * @param aspectRatio The aspect ratio
         * @return The optimal {@code Size}, or an arbitrary one if none were big enough
         */
        private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
            // Collect the supported resolutions that are at least as big as the preview Surface
            List<Size> bigEnough = new ArrayList<>();
            int w = aspectRatio.getWidth();
            int h = aspectRatio.getHeight();
            for (Size option : choices) {
                if (option.getHeight() == option.getWidth() * h / w &&
                        option.getWidth() >= width && option.getHeight() >= height) {
                    bigEnough.add(option);
                }
            }

            // Pick the smallest of those, assuming we found any
            if (bigEnough.size() > 0) {
                return Collections.min(bigEnough, new CompareSizesByArea());
            } else {
                Log.e(TAG, "Couldn't find any suitable preview size");
                return choices[0];
            }
        }

    Timer timer;
    int asyncTimer = 0;
    boolean asyncAuto = false;
    String asyncTitle = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_recording);
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        dbVideoRef = database.getReference();
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mStartWhite = (ImageView) findViewById(R.id.record_start_white);
        mStartRed = (ImageView) findViewById(R.id.record_start_red);
        mStop = findViewById(R.id.record_stop_red);
        mTimer = findViewById(R.id.video_timer);
        mBack = findViewById(R.id.video_back);
        userToAddRecordingTo = getIntent().getStringExtra("userToAddRecordingTo");
        if(getIntent().hasExtra("auto-async")){
            asyncAuto = getIntent().getBooleanExtra("auto-async",false);
            asyncTimer = getIntent().getIntExtra("async-timer",0);
            asyncTitle = getIntent().getStringExtra("async-title");
            Log.d("ActivityVideoRecording","AsyncAuto: "+asyncAuto);
        }
        else{
            Toast.makeText(this,"Failed to load async auto or async timer",Toast.LENGTH_SHORT).show();
        }
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("async-timer",0);
                returnIntent.putExtra("auto-async",false);
                returnIntent.putExtra("async-title","");
                returnIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                setResult(Activity.RESULT_CANCELED,returnIntent);
                Log.d("ActivityVideoRecording","mBack Finishing Activity");
                Intent i = new Intent(ActivityVideoRecording.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        });
        mStartWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecordingVideo();
                mTimer.setVisibility(View.VISIBLE);
                timer = new Timer();
                TimerTask t = new TimerTask() {
                    int sec = 0;
                    @Override
                    public void run() {
                        sec+=1;
                        String readable = String.format("%d:%02d", sec/60, sec%60);
                        mTimer.setText(readable);
                    }
                };
                timer.scheduleAtFixedRate(t,1000,1000);
                stopRecordingVisible();
            }
        });
        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startRecordingVisible();
                stopRecordingVideo();
                showVideoSaver();
                mTimer.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            currentUser = user.getEmail().replace("@","").replace(".","");
        } else {
            // No user is signed in
            Intent i = new Intent(ActivityVideoRecording.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("async-timer",0);
        returnIntent.putExtra("auto-async",false);
        returnIntent.putExtra("async-title","");
        setResult(Activity.RESULT_CANCELED,returnIntent);
    }

    public void tryAutoAsync(){
        if(asyncAuto){
            Log.d("ActivityVideo","tryAutoAsync called");
            startRecordingVideo();
            stopRecordingVisible();
            mTimer.setVisibility(View.VISIBLE);
            countDownTimer = new CountDownTimer(asyncTimer*60*1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                    //here you can have your logic to set text to edittext
                    long sec = millisUntilFinished/1000;
                    String readable = String.format("%d:%02d", sec/60, sec%60);
                    Log.d("ActivityVideoRecording","Seconds left: "+sec);
                    mTimer.setText(readable);
                }

                public void onFinish() {

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ActivityVideoRecording","Recording stopped @ ");
                            stopRecordingVideo();
                            startRecordingVisible();
                            File f = new File(videoURI);

                            new DropboxVideo(videoURI, getContentResolver(), asyncTitle, ActivityVideoRecording.this,currentUser,getMediaRecordingLength(f)+"");
                            Log.d("ActivityVideoRecording","auto-async Finishing Activity");
                            //finish();
                            tryAutoAsync();
                        }
                    }, 500);
                     //ERROR -> HERE
                }

            }.start();

        }
        recordOnce+=1;
    }

    public void updateUserVideo(String videoLink, String title, String length, String size, String storagePath, String thumbnailURL) {
        final Video videoToAdd = new Video();
        videoToAdd.setLength(length);
        videoToAdd.setSize(size);
        videoToAdd.setTitle(title);
        videoToAdd.setUrl(videoLink);
        videoToAdd.setDate(getCurrentDate());
        videoToAdd.setUserToSaveUnder(userToAddRecordingTo);
        videoToAdd.setVideoStoragePath(storagePath);
        videoToAdd.setThumbnailURL(thumbnailURL);
        Log.d("ActivityVideoRecording","Thumb URL: "+thumbnailURL);
        videos = new ArrayList<>();
        dbVideoRef.child("users").child(currentUser).child(userToAddRecordingTo.replace(" ", "")).child("videos").addListenerForSingleValueEvent(new ValueEventListener() {
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
                        if(videoSnap.hasChild("thumbnailURL")) {
                            video.setThumbnailURL(videoSnap.child("thumbnailURL").getValue(String.class));
                        }
                        videos.add(video);
                        Log.d("MainActivity", "Added previous video");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                videos.add(videoToAdd);
                HashMap<String, ArrayList<Video>> videoHash = new HashMap<>();
                videoHash.put("videos", videos);
                dbVideoRef.child("users").child(currentUser).child(userToAddRecordingTo.replace(" ", "")).updateChildren((HashMap) videoHash);
                Toast.makeText(ActivityVideoRecording.this, "Added video to user", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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




    public void showVideoSaver(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_save_or_delete_video, null);
        final EditText mVideoTitle = dialogView.findViewById(R.id.save_video_title);
        final TextView mVideoLength = dialogView.findViewById(R.id.save_video_length);
        final TextView mVideoSize = dialogView.findViewById(R.id.save_video_size);
        final TextView mVideoUploadTime = dialogView.findViewById(R.id.save_estimated_time);
        if(!asyncTitle.equals("")){
            mVideoTitle.setText(asyncTitle);
        }
        final File f = new File(videoURI);
        mVideoLength.setText(getMediaRecordingLength(f)+"");
        mVideoSize.setText(getStringSizeLengthFile(f.length()));
        mVideoUploadTime.setText("Estimated upload time: "+(getMediaRecordingLength(f)*.9)+"");

        dialogBuilder.setPositiveButton("Save & Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mVideoTitle.getText().toString().length() > 0) {
                    Log.d("ActivityVideoRecording","showVideoSaver() Finishing Activity");
                    if(countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    new DropboxVideo(videoURI, getContentResolver(), asyncTitle, ActivityVideoRecording.this,currentUser,getMediaRecordingLength(f)+"");

                    //finish();
                }
                else{
                    Toast.makeText(ActivityVideoRecording.this,"Enter a title for the video",Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogBuilder.setNeutralButton("Play Video", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        Toast.makeText(this,"Video saved",Toast.LENGTH_SHORT).show();
    }



    public void stopRecordingVisible(){
        mStop.setVisibility(View.VISIBLE);
        mStartRed.setVisibility(View.INVISIBLE);
        mStartWhite.setVisibility(View.INVISIBLE);
    }
    public void startRecordingVisible(){
        mStop.setVisibility(View.INVISIBLE);
        mStartRed.setVisibility(View.VISIBLE);
        mStartWhite.setVisibility(View.VISIBLE);
    }



        @Override
        public void onResume() {
            super.onResume();
            startBackgroundThread();
            if (mTextureView.isAvailable()) {
                openCamera(mTextureView.getWidth(), mTextureView.getHeight());

            } else {
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        }

        @Override
        public void onPause() {
            closeCamera();
            stopBackgroundThread();
            Log.d("ActivityVideoRecording","onPause Hit, not closing camera");
            if(countDownTimer != null) {
                countDownTimer.cancel();
            }
            //Intent i = new Intent(this, MainActivity.class);
            //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //startActivity(i);
            //finish();
            super.onPause();

        }


        /**
         * Starts a background thread and its {@link Handler}.
         */
        private void startBackgroundThread() {
            mBackgroundThread = new HandlerThread("CameraBackground");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }

        /**
         * Stops the background thread and its {@link Handler}.
         */
        private void stopBackgroundThread() {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Gets whether you should show UI with rationale for requesting permissions.
         *
         * @param permissions The permissions your app wants to request.
         * @return Whether you can show permission rationale UI.
         */
        private boolean shouldShowRequestPermissionRationale(String[] permissions) {
            for (String permission : permissions) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Requests permissions needed for recording video.
         */
        private void requestVideoPermissions() {
            if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
                //new ConfirmationDialog().show(this, FRAGMENT_DIALOG);
            } else {
                requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            Log.d(TAG, "onRequestPermissionsResult");
            if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
                if (grantResults.length == VIDEO_PERMISSIONS.length) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            //ErrorDialog.newInstance("Request").show(this, FRAGMENT_DIALOG);
                            break;
                        }
                    }
                } else {
//                    ErrorDialog.newInstance(getString(R.string.permission_request))
//                            .show(getChildFragmentManager(), FRAGMENT_DIALOG);
                }
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }

        private boolean hasPermissionsGranted(String[] permissions) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(ActivityVideoRecording.this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
         */
        @SuppressWarnings("MissingPermission")
        private void openCamera(int width, int height) {
            if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
                requestVideoPermissions();
                return;
            }
            final Activity activity = ActivityVideoRecording.this;
            if (null == activity || activity.isFinishing()) {
                return;
            }
            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            try {
                Log.d(TAG, "tryAcquire");
                if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                String cameraId = manager.getCameraIdList()[0];

                // Choose the sizes for camera preview and video recording
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                if (map == null) {
                    throw new RuntimeException("Cannot get available preview/video sizes");
                }
                mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        width, height, mVideoSize);

                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    //mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    //mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
                configureTransform(width, height);
                mMediaRecorder = new MediaRecorder();
                manager.openCamera(cameraId, mStateCallback, null);

            } catch (CameraAccessException e) {
                Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            } catch (NullPointerException e) {
                // Currently an NPE is thrown when the Camera2API is used but not supported on the
                // device this code runs.
//                ErrorDialog.newInstance(getString(R.string.camera_error))
//                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera opening.");
            }
        }

        private void closeCamera() {
            try {
                mCameraOpenCloseLock.acquire();
                closePreviewSession();
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
                if (null != mMediaRecorder) {
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera closing.");
            } finally {
                mCameraOpenCloseLock.release();
            }
        }

        /**
         * Start the camera preview.
         */
        private void startPreview() {
            Log.d("ActivityVideoRecording","Preview started!");
            if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
                return;
            }
            try {
                closePreviewSession();
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                assert texture != null;
                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

                Surface previewSurface = new Surface(texture);
                mPreviewBuilder.addTarget(previewSurface);

                mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                        new CameraCaptureSession.StateCallback() {

                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                mPreviewSession = session;
                                updatePreview();
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                Activity activity = ActivityVideoRecording.this;
                                if (null != activity) {
                                    Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onReady(@NonNull CameraCaptureSession session) {
                                super.onReady(session);
                                Log.d("ActivityVideoRecording","CameraCaptureSession ready");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(recordOnce == 0){
                                            tryAutoAsync();
                                        }

//stuff that updates ui

                                    }
                                });
                            }

                            @Override
                            public void onSurfacePrepared(@NonNull CameraCaptureSession session, @NonNull Surface surface) {
                                super.onSurfacePrepared(session, surface);
                                Log.d("ActivityVideoRecording","CameraCaptureSession surface prepared");

                                //tryAutoAsync();
                            }
                        }, mBackgroundHandler);


            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }
        private void updatePreview() {
            if (null == mCameraDevice) {
                return;
            }
            try {
                setUpCaptureRequestBuilder(mPreviewBuilder);
                HandlerThread thread = new HandlerThread("CameraPreview");
                thread.start();
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        }

    private boolean isAutoFocusSupported() {
        return  isHardwareLevelSupported(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) || getMinimumFocusDistance() > 0;
    }
    // Returns true if the device supports the required hardware level, or better.
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean isHardwareLevelSupported(int requiredLevel) {
        boolean res = false;

        try {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = manager.getCameraIdList()[0];

        if (cameraId == null)
            return res;

            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);

            int deviceLevel = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            switch (deviceLevel) {
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                    Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_3");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                    Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_FULL");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                    Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                    Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED");
                    break;
                default:
                    Log.d(TAG, "Unknown INFO_SUPPORTED_HARDWARE_LEVEL: " + deviceLevel);
                    break;
            }


            if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                res = requiredLevel == deviceLevel;
            } else {
                // deviceLevel is not LEGACY, can use numerical sort
                res = requiredLevel <= deviceLevel;
            }

        } catch (Exception e) {
            Log.e(TAG, "isHardwareLevelSupported Error", e);
        }
        return res;
    }

    private float getMinimumFocusDistance() {
        try {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId = manager.getCameraIdList()[0];
            if (cameraId == null)
                return 0;

            Float minimumLens = null;
            try {
                CameraCharacteristics c = manager.getCameraCharacteristics(cameraId);
                minimumLens = c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
            } catch (Exception e) {
                Log.e(TAG, "isHardwareLevelSupported Error", e);
            }
            if (minimumLens != null)
                return minimumLens;
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

        /**
         * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
         * This method should not to be called until the camera preview size is determined in
         * openCamera, or until the size of `mTextureView` is fixed.
         *
         * @param viewWidth  The width of `mTextureView`
         * @param viewHeight The height of `mTextureView`
         */
        private void configureTransform(int viewWidth, int viewHeight) {
            Activity activity = ActivityVideoRecording.this;
            if (null == mTextureView || null == mPreviewSize || null == activity) {
                return;
            }
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            Matrix matrix = new Matrix();
            RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
            RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
            float centerX = viewRect.centerX();
            float centerY = viewRect.centerY();
            if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
                bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
                matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
                float scale = Math.max(
                        (float) viewHeight / mPreviewSize.getHeight(),
                        (float) viewWidth / mPreviewSize.getWidth());
                matrix.postScale(scale, scale, centerX, centerY);
                matrix.postRotate(90 * (rotation - 2), centerX, centerY);
            }
            mTextureView.setTransform(matrix);
        }

        private void setUpMediaRecorder() throws IOException {
            final Activity activity = ActivityVideoRecording.this;
            if (null == activity) {
                return;
            }
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
                mNextVideoAbsolutePath = getVideoFilePath(ActivityVideoRecording.this);
            }
            mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
            mMediaRecorder.setVideoEncodingBitRate(9000000); //HD 15000000
            mMediaRecorder.setAudioEncodingBitRate(128000); //196608
            mMediaRecorder.setVideoFrameRate(16);
            mMediaRecorder.setAudioSamplingRate(44100);
            mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); //AMW_WB
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            Log.d("ActivityVideoRecording","Rotation: "+rotation);
            switch (mSensorOrientation) {
                case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                    mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                    break;
                case SENSOR_ORIENTATION_INVERSE_DEGREES:
                    mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                    break;
            }
            mMediaRecorder.prepare();
        }

        private String getVideoFilePath(Context context) {
            final File dir = context.getExternalFilesDir(null);
            return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                    + System.currentTimeMillis() + ".mp4";
        }

        private void startRecordingVideo() {
            Log.d("ActivityVideoRecording","Recording started!");
            if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
                Log.d("ActivityVideoRecording","Something isn't available");

                return;
            }
            try {
                closePreviewSession();
                setUpMediaRecorder();
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                assert texture != null;
                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                List<Surface> surfaces = new ArrayList<>();

                // Set up Surface for the camera preview
                Surface previewSurface = new Surface(texture);
                surfaces.add(previewSurface);
                mPreviewBuilder.addTarget(previewSurface);

                // Set up Surface for the MediaRecorder
                Surface recorderSurface = mMediaRecorder.getSurface();
                surfaces.add(recorderSurface);
                mPreviewBuilder.addTarget(recorderSurface);

                // Start a capture session
                // Once the session starts, we can update the UI and start recording
                mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        mPreviewSession = cameraCaptureSession;
                        updatePreview();
                        ActivityVideoRecording.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // UI
                                    //mStartWhite.setText(R.string.stop);
                                    mIsRecordingVideo = true;
                                    // Start recording
                                    mMediaRecorder.start();
                                } catch (Exception e){
                                    Log.d(TAG,"Error due to phone being horizontal");
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        Activity activity = ActivityVideoRecording.this;
                        if (null != activity) {
                            Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSurfacePrepared(@NonNull CameraCaptureSession session, @NonNull Surface surface) {
                        super.onSurfacePrepared(session, surface);
                        //tryAutoAsync();
                    }
                }, mBackgroundHandler);
            } catch (CameraAccessException | IOException e) {
                e.printStackTrace();
            }


        }

        private void closePreviewSession() {
            if (mPreviewSession != null) {
                mPreviewSession.close();
                mPreviewSession = null;
            }
        }

        private void stopRecordingVideo() {
            try {
                if(timer != null){
                    timer.cancel();
                }
                // UI
                mIsRecordingVideo = false;
                //mStartWhite.setText(R.string.record);
                // Stop recording
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                Activity activity = ActivityVideoRecording.this;
                if (null != activity) {
                    Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
                    videoURI = mNextVideoAbsolutePath;
                }
                mNextVideoAbsolutePath = null;
                startPreview();
            } catch (Exception e){
                e.printStackTrace();
                Activity activity = ActivityVideoRecording.this;
                if (null != activity) {
                    Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
                    videoURI = mNextVideoAbsolutePath;
                }
                mNextVideoAbsolutePath = null;
                startPreview();
            }
        }

        /**
         * Compares two {@code Size}s based on their areas.
         */
        static class CompareSizesByArea implements Comparator<Size> {

            @Override
            public int compare(Size lhs, Size rhs) {
                // We cast here to ensure the multiplications won't overflow
                return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                        (long) rhs.getWidth() * rhs.getHeight());
            }

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
    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
    }
    private static String getDuration(File file) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return durationStr;
    }
    public static String formateMilliSeccond(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        //      return  String.format("%02d Min, %02d Sec",
        //                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
        //                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
        //                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));

        // return timer string
        return finalTimerString;
    }
    public static String getStringSizeLengthFile(long size) {

        DecimalFormat df = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMo = sizeKb * sizeKb;
        float sizeGo = sizeMo * sizeKb;
        float sizeTerra = sizeGo * sizeKb;


        if(size < sizeMo)
            return df.format(size / sizeKb)+ " KB";
        else if(size < sizeGo)
            return df.format(size / sizeMo) + " MB";
        else if(size < sizeTerra)
            return df.format(size / sizeGo) + " GB";

        return "";
    }}