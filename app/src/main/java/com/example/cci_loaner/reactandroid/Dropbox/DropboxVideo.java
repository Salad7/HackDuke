package com.example.cci_loaner.reactandroid.Dropbox;

/**
 * Created by cci-loaner on 4/10/18.
 */
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v1.DbxClientV1;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetTemporaryLinkResult;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.example.cci_loaner.reactandroid.ActivityVideoRecording;
import com.example.cci_loaner.reactandroid.Auth.LoginActivity;
import com.example.cci_loaner.reactandroid.Models.MarketplaceVideo;
import com.example.cci_loaner.reactandroid.R;
import com.example.cci_loaner.reactandroid.VideoCompress.GiraffeCompressor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class DropboxVideo {

    private static final String ACCESS_TOKEN = "g6-Bz42HheAAAAAAAAAAHC6uKPC9jnoilXHKiyfwkKSUQXLwj8kw-cieaHhcKNmU";
    private String videoPath;
    private ContentResolver contentResolver;
    private static DbxClientV2 client;
    private String title;
    private ActivityVideoRecording context;
    String loggedInUser;
    String storagePath;
    String length;
    FirebaseStorage storage;
    StorageReference thumbRef;
    String videoURL;
    String thumbnailURL;


    public DropboxVideo(final String videoPath, ContentResolver contentResolver, String title, final ActivityVideoRecording context, String loggedInUser, String length){
        this.videoPath = videoPath;
        this.contentResolver = contentResolver;
        this.title = title;
        this.context = context;
        this.loggedInUser = loggedInUser;
        this.storagePath = storagePath;
        this.length = length;
        FirebaseApp.initializeApp(context);
        storage = FirebaseStorage.getInstance();
        thumbRef = storage.getReference("Thumbnails");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            loggedInUser = user.getEmail().replace("@","").replace(".","");
        } else {
            // No user is signed in
            Intent i = new Intent(context, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        }
        saveVideoThumbnail();
    }

    public DropboxVideo(){

    }

    public void saveVideoThumbnail(){
        final String storageURL = "gs://react-932c4.appspot.com/Thumbnails";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoPath,
                MediaStore.Images.Thumbnails.MINI_KIND);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = thumbRef.putBytes(data);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                thumbnailURL = downloadUrl.toString();
                Log.d("DropboxVideo","Thumbnail URL: "+thumbnailURL);
                new DropboxAsync().execute(ACCESS_TOKEN,videoPath);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DropboxVideo","Thumbnail Fail");
                e.printStackTrace();
                new DropboxAsync().execute(ACCESS_TOKEN,videoPath);

            }
        });

    }


    public class DropboxGetVideos extends AsyncTask<String,Void,ArrayList<MarketplaceVideo>>{

        FileMetadata fileMetadata;
        String videoPathInternal;

        @Override
        protected ArrayList<MarketplaceVideo> doInBackground(String... strings) {
            // Create Dropbox client
            try {
                DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("dropbox/java-tutorial")
                        .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                        .build();
                client = new DbxClientV2(requestConfig, strings[0]);
                ListFolderResult result = client.files().listFolder("");
                while (true) {
                    for (Metadata metadata : result.getEntries()) {
                        Log.d("DropboxVideo", "getPathLower " + metadata.getPathLower());
                        Log.d("DropboxVideo", "getPathDisplay " + metadata.getPathDisplay());
                        Log.d("DropboxVideo", "getName " + metadata.getName());

                    }

                    if (!result.getHasMore()) {
                        break;
                    }

                    result = client.files().listFolderContinue(result.getCursor());

                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
    public class DropboxAsync extends AsyncTask<String,Void,String>{

        FileMetadata fileMetadata;
        String videoPathInternal;

        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.d("DropboxVideo","Begin uploading video to Dropbox");
                // Create Dropbox client
                DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("dropbox/java-tutorial")
                        .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                        .build();
                videoPathInternal = strings[1];
                client = new DbxClientV2(requestConfig, strings[0]);
                FullAccount account = client.users().getCurrentAccount();
                // Upload "test.txt" to Dropbox
                InputStream is = new FileInputStream(strings[1]);
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                // Get files and folder metadata from Dropbox root directory

                ListFolderResult result = client.files().listFolder("");
                while (true) {
                    for (Metadata metadata : result.getEntries()) {
                        Log.d("DropboxVideo"," ");
                        Log.d("DropboxVideo","getPathLower "+metadata.getPathLower());
                        Log.d("DropboxVideo","getPathDisplay "+metadata.getPathDisplay());
                        Log.d("DropboxVideo","getName "+metadata.getName());
                    }

                    if (!result.getHasMore()) {
                        break;
                    }

                    result = client.files().listFolderContinue(result.getCursor());
                }
                fileMetadata = client.files().uploadBuilder("/"+title+"/"+loggedInUser+ts+".mp4")
                            .uploadAndFinish(is);
                saveVideoThumbnail();
                ///5min/seinab1yahoocom1523828601.mp4
                    return "/"+title+"/"+loggedInUser+ts+".mp4";

            }
            catch (Exception e){
                Log.d("DropboxVideo","Error: "+e.getMessage() + e.getCause());
                e.printStackTrace();
            }
            return null;
        }

        public boolean delete(File path) {
            boolean result = true;
            if (path.exists()) {
                if (path.isDirectory()) {
                    for (File child : path.listFiles()) {
                        result &= delete(child);
                    }
                    result &= path.delete(); // Delete empty directory.
                } else if (path.isFile()) {
                    result &= path.delete();
                }
                return result;
            } else {
                return false;
            }
        }

        public String getStringSizeLengthFile(long size) {

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
        }

        @Override
        protected void onPostExecute(String pathURL) {
            super.onPostExecute(pathURL);
            try {
                ((ActivityVideoRecording) context).updateUserVideo(pathURL, title, length, getStringSizeLengthFile(fileMetadata.getSize()), videoPath, thumbnailURL);
                //Log.d("DropboxVideo","Email: "+fullAccount.getEmail());
                if(delete(new File(videoPathInternal))){
                    Toast.makeText(context,"File deleted",Toast.LENGTH_SHORT).show();
                    Log.d("DropboxVideo","File path deleted: "+videoPathInternal);
                }
            }
            catch (Exception e){
                //Toast.makeText(context,"Error uploading file, please upload through gallery",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

}
