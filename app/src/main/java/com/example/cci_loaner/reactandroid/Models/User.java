package com.example.cci_loaner.reactandroid.Models;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by cci-loaner on 3/1/18.
 */

public class User {
    String name;
    String date;
    ArrayList<Bucket> buckets;
    ArrayList<String> speechContexts;
    ArrayList<Note> notes;
    ArrayList<Video> videos;
    boolean hideUser;
    String languageCode;

    public ArrayList<String> getSpeechContexts() {
        return speechContexts;
    }
    public void setSpeechContexts(ArrayList<String> bucketTerms) {
        this.speechContexts = bucketTerms;
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<Note> notes) {
        this.notes = notes;
    }

    public boolean isHideUser() {
        return hideUser;
    }


    public void setHideUser(boolean hideUser) {
        this.hideUser = hideUser;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

//    public String getParcelableSpeechContext(){
//        String parcelable = "";
//        try {
//            if (speechContexts.size() > 0) {
//                for (String parce : speechContexts
//                        ) {
//                    parcelable += parce + "-";
//                }
//                Log.d("SpeechContexts",parcelable);
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//        return parcelable;
//    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    int numBuckets;

    public User() {
        buckets = new ArrayList<>();
        notes = new ArrayList<>();
        speechContexts = new ArrayList<>();
        hideUser = true;
    }

    public void addBucket(Bucket bucket){
        buckets.add(bucket);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public ArrayList<Bucket> getBuckets() {
        return buckets;
    }

    public void setBuckets(ArrayList<Bucket> buckets) {
        this.buckets = buckets;
    }

    public int getNumBuckets() {
        return numBuckets;
    }

    public void setNumBuckets(int numBuckets) {
        this.numBuckets = numBuckets;
    }
}
