package com.example.cci_loaner.reactandroid;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.GetTemporaryLinkResult;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.net.URI;

/**
 * Created by cci-loaner on 4/14/18.
 */

public class ActivityVideoPlayer extends AppCompatActivity implements EasyVideoCallback {
    //private static final String TEST_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

    private EasyVideoPlayer player;
    private static final String ACCESS_TOKEN = "g6-Bz42HheAAAAAAAAAAHC6uKPC9jnoilXHKiyfwkKSUQXLwj8kw-cieaHhcKNmU";
    private static DbxClientV2 client;
    String URL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        URL = getIntent().getStringExtra("video_url");
        new DropboxAsyncStreamer().execute(ACCESS_TOKEN,URL);


        // Grabs a reference to the player view
        player = (EasyVideoPlayer) findViewById(R.id.player);

        }

        public void beginStreaming(String streamURL){

            // Sets the callback to this Activity, since it inherits EasyVideoCallback
            player.setCallback(this);

            // Sets the source to the HTTP URL held in the TEST_URL variable.
            // To play files, you can use Uri.fromFile(new File("..."))
            //player.setSource(Uri.parse(URL));
            player.setSource(Uri.parse(streamURL));
            Log.d("ActivityVideoPlayer","streamURL: "+streamURL);

            // From here, the player view will show a progress indicator until the player is prepared.
            // Once it's prepared, the progress indicator goes away and the controls become enabled for the user to begin playback.
        }


    public class DropboxAsyncStreamer extends AsyncTask<String,Void,GetTemporaryLinkResult> {
        @Override
        protected GetTemporaryLinkResult doInBackground(String... strings) {
            try {
                Log.d("ActivityVideoPlayer","Begin uploading video to Dropbox");
                // Create Dropbox client
                DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("dropbox/java-tutorial")
                        .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                        .build();
                client = new DbxClientV2(requestConfig, strings[0]);
                ListFolderResult result = client.files().listFolder("");
                while (true) {
                    for (Metadata metadata : result.getEntries()) {
                        Log.d("ActivityVideoPlayer"," ");
                        Log.d("ActivityVideoPlayer","getPathLower "+metadata.getPathLower());
                        Log.d("ActivityVideoPlayer","getPathDisplay "+metadata.getPathDisplay());
                        Log.d("ActivityVideoPlayer","getName "+metadata.getName());
                    }

                    if (!result.getHasMore()) {
                        break;
                    }

                    result = client.files().listFolderContinue(result.getCursor());
                }
                return client.files().getTemporaryLink(strings[1]);

            }
            catch (Exception e){
                Log.d("ActivityVideoPlayer","Error: "+e.getMessage() + e.getCause());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetTemporaryLinkResult getTemporaryLinkResult) {
            super.onPostExecute(getTemporaryLinkResult);
            beginStreaming(getTemporaryLinkResult.getLink());
            Toast.makeText(ActivityVideoPlayer.this,"Stream uploaded",Toast.LENGTH_SHORT).show();
        }
    }


    public class DropboxAsyncDownloader extends AsyncTask<String,Void,GetTemporaryLinkResult> {
        @Override
        protected GetTemporaryLinkResult doInBackground(String... strings) {
            try {
                Log.d("ActivityVideoPlayer","Begin uploading video to Dropbox");
                // Create Dropbox client
                DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("dropbox/java-tutorial")
                        .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                        .build();
                client = new DbxClientV2(requestConfig, strings[0]);
                ListFolderResult result = client.files().listFolder("");
                while (true) {
                    for (Metadata metadata : result.getEntries()) {
                        Log.d("ActivityVideoPlayer"," ");
                        Log.d("ActivityVideoPlayer","getPathLower "+metadata.getPathLower());
                        Log.d("ActivityVideoPlayer","getPathDisplay "+metadata.getPathDisplay());
                        Log.d("ActivityVideoPlayer","getName "+metadata.getName());
                    }

                    if (!result.getHasMore()) {
                        break;
                    }

                    result = client.files().listFolderContinue(result.getCursor());
                }
                return client.files().getTemporaryLink("/5min/seinab1yahoocom1523828601.mp4");

            }
            catch (Exception e){
                Log.d("ActivityVideoPlayer","Error: "+e.getMessage() + e.getCause());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetTemporaryLinkResult getTemporaryLinkResult) {
            super.onPostExecute(getTemporaryLinkResult);
            beginStreaming(getTemporaryLinkResult.getLink());
            Toast.makeText(ActivityVideoPlayer.this,"Stream uploaded",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Make sure the player stops playing if the user presses the home button.
        player.pause();
    }

    // Methods for the implemented EasyVideoCallback

    @Override
    public void onPreparing(EasyVideoPlayer player) {
        // TODO handle if needed
    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
        // TODO handle
    }

    @Override
    public void onBuffering(int percent) {
        // TODO handle if needed
    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {
        // TODO handle
    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {
        // TODO handle if needed
    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {
        // TODO handle if used
    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {
        // TODO handle if used
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {
        // TODO handle if needed
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
        // TODO handle if needed
    }
}
